/**
 AirCasting - Share your Air!
 Copyright (C) 2011-2012 HabitatMap, Inc.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 You can contact the authors by email at <info@habitatmap.org>
 */
package pl.llp.aircasting.sync;

import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.android.Logger;
import pl.llp.aircasting.api.SessionDriver;
import pl.llp.aircasting.api.SyncDriver;
import pl.llp.aircasting.api.data.CreateSessionResponse;
import pl.llp.aircasting.api.data.DeleteSessionResponse;
import pl.llp.aircasting.api.data.SyncResponse;
import pl.llp.aircasting.event.SyncStateChangedEvent;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.storage.repository.RepositoryException;
import pl.llp.aircasting.storage.repository.SessionRepository;
import pl.llp.aircasting.util.SyncState;
import pl.llp.aircasting.util.http.HttpResult;
import pl.llp.aircasting.util.http.Status;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;
import com.google.common.base.Predicate;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import roboguice.inject.InjectResource;
import roboguice.service.RoboIntentService;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Lists.newArrayList;

public class SyncService extends RoboIntentService
{
  @Inject ConnectivityManager connectivityManager;
  @Inject SessionRepository sessionRepository;
  @Inject SyncDriver syncDriver;
  @Inject SettingsHelper settingsHelper;
  @Inject SessionDriver sessionDriver;
  @Inject SyncState syncState;
  @Inject Context context;
  @Inject EventBus events;

  @InjectResource(R.string.account_reminder) String accountReminder;

  @Inject
  SessionTimeFixer sessionTimes;

  public SyncService() {
    super(SyncService.class.getSimpleName());
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    try {
      syncState.startSync();
      events.post(new SyncStateChangedEvent());

      if (canUpload()) {
        sync();
      } else if (!settingsHelper.hasCredentials()) {
        Intents.notifySyncUpdate(context, accountReminder);
      }
    } catch (SessionSyncException exception)
    {
      Toast.makeText(getBaseContext(), R.string.session_sync_failed, Toast.LENGTH_LONG);
    }
    finally {
      syncState.markSyncComplete();
      events.post(new SyncStateChangedEvent());
    }
  }

  private void sync() throws SessionSyncException
  {
    Iterable<Session> sessions = prepareSessions(sessionRepository.allCompleteSessions());

    HttpResult<SyncResponse> result = syncDriver.sync(sessions);
    Status status = result.getStatus();
    if(status == Status.ERROR || status == Status.FAILURE)
    {
      throw new SessionSyncException("Initial sync failed");
    }
    SyncResponse syncResponse = result.getContent();

    if (syncResponse != null)
    {
      sessionRepository.deleteSubmitted();
      UUID[] upload = syncResponse.getUpload();
      UUID[] deleted = syncResponse.getDeleted();
      long[] download = syncResponse.getDownload();
      deleteMarked(deleted);
      uploadSessions(upload);
      downloadSessions(download);
    }
  }

    private void deleteMarked(UUID[] deleted)
  {
    if(deleted.length == 0)
      return;

    for (UUID uuid : deleted)
    {
      sessionRepository.markSessionForRemoval(uuid);
    }
    Intents.notifySyncUpdate(context);
    sessionRepository.deleteSubmitted();
  }

  List<Session> prepareSessions(List<Session> sessions)
  {
    List<Session> result = newArrayList();

    for (Session session : sessions)
    {
      if(session.isLocationless())
      {
        continue;
      }

      boolean ignoreSession = deletedAnything(session);
      if (ignoreSession)
      {
        continue;
      }

      result.add(session);
    }
    if(sessions.size() > result.size())
    {
      sessionRepository.deleteSubmitted();
    }

    return result;
  }

  private boolean deletedAnything(Session session)
  {
    if(session.isMarkedForRemoval())
    {
      DeleteSessionResponse response = sessionDriver.deleteSession(session).getContent();
      if(response != null && (response.isSuccess() || response.noSuchSession()))
      {
        markSessionSubmittedForRemoval(session);
      }
      return true;
    }
    else
    {
      Collection<MeasurementStream> streams = session.getMeasurementStreams();
      for (MeasurementStream stream : streams)
      {
        if(stream.isMarkedForRemoval())
        {
          DeleteSessionResponse response = sessionDriver.deleteStreams(session).getContent();
          if(response != null && (response.isSuccess() || response.noSuchSession()))
          {
            markStreamsSubmittedForRemoval(session);
          }
        }
      }
    }
    return false;
  }

  private void markStreamsSubmittedForRemoval(Session session)
  {
    Collection<MeasurementStream> streams = session.getMeasurementStreams();
    for (MeasurementStream stream : streams)
    {
      if (stream.isMarkedForRemoval())
      {
        stream.setSubmittedForRemoval(true);
        sessionRepository.updateStream(stream);
      }
    }
  }

  private void markSessionSubmittedForRemoval(Session session)
  {
    session.setSubmittedForRemoval(true);
    sessionRepository.update(session);
  }

  private boolean canUpload() {
    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

    return networkInfo != null
        && networkInfo.isConnected()
        && (!settingsHelper.isSyncOnlyWifi() || networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
        && settingsHelper.hasCredentials();
  }

  private void uploadSessions(UUID[] onServer)
  {
    for (UUID uuid : onServer)
    {
      Session session = sessionRepository.loadFully(uuid);
      if (session != null && skipUpload(session))
      {
        continue;
      }
      else
      {
        HttpResult<CreateSessionResponse> result = sessionDriver.create(session);

        if (result.getStatus() == Status.SUCCESS) {
          updateSession(session, result.getContent());
        }
      }

      Intents.notifySyncUpdate(context);
    }
  }

  private boolean skipUpload(Session session)
  {
    return session == null || session.isMarkedForRemoval() || session.isLocationless();
  }

  private void updateSession(Session session, CreateSessionResponse sessionResponse)
  {
    session.setLocation(sessionResponse.getLocation());

    for (CreateSessionResponse.Note responseNote : sessionResponse.getNotes()) {
      final int number = responseNote.getNumber();

      Note note = find(session.getNotes(), new Predicate<Note>() {
        @Override
        public boolean apply(Note note) {
          return note.getNumber() == number;
        }
      });

      note.setPhotoPath(responseNote.getPhotoLocation());
    }

    sessionRepository.update(session);
  }

  private void downloadSessions(long[] ids)
  {
    for (long id : ids) {
      HttpResult<Session> result = sessionDriver.show(id);

      if (result.getStatus() == Status.SUCCESS) {
        Session session = result.getContent();
        if (session == null) {
          Logger.w("Session [" + id + "] couldn't ");
        }
        else if (session.isIncomplete()) {
          Logger.w(String.format("Session [%s] lacks of some measurements.", id));
        }
        else {
          try
          {
            fixTimesFromUTC(session);
            sessionRepository.save(session);
          }
          catch (RepositoryException e)
          {
            Logger.e("Error saving session [" + id + "]", e);
          }
        }
      }

      Intents.notifySyncUpdate(context);
    }
  }



  private void fixTimesFromUTC(Session session)
  {
    sessionTimes.fromUTCtoLocal(session);
  }
}
