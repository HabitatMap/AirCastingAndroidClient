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
package pl.llp.aircasting.service;

import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.api.SessionDriver;
import pl.llp.aircasting.api.SyncDriver;
import pl.llp.aircasting.api.data.CreateSessionResponse;
import pl.llp.aircasting.api.data.SyncResponse;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.repository.RepositoryException;
import pl.llp.aircasting.repository.SessionRepository;
import pl.llp.aircasting.util.Constants;
import pl.llp.aircasting.util.SyncState;
import pl.llp.aircasting.util.http.HttpResult;
import pl.llp.aircasting.util.http.Status;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;
import com.google.common.base.Predicate;
import com.google.inject.Inject;
import roboguice.inject.InjectResource;
import roboguice.service.RoboIntentService;

import java.util.Collection;
import java.util.UUID;

import static com.google.common.collect.Iterables.find;

public class SyncService extends RoboIntentService
{
  @Inject ConnectivityManager connectivityManager;
  @Inject SessionRepository sessionRepository;
  @Inject SyncDriver syncDriver;
  @Inject SettingsHelper settingsHelper;
  @Inject SessionDriver sessionDriver;
  @Inject SyncState syncState;
  @Inject Context context;

  @InjectResource(R.string.account_reminder) String accountReminder;

  @Inject SessionTimeFixer sessionTimes;

  public SyncService() {
    super(SyncService.class.getSimpleName());
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    try {
      syncState.startSync();

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
    }
  }

  private void sync() throws SessionSyncException
  {
    Iterable<Session> sessions = prepareSessions();

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
      uploadSessions(syncResponse.getUpload());

      downloadSessions(syncResponse.getDownload());
    }
  }

  private Iterable<Session> prepareSessions()
  {
    Iterable<Session> sessions = sessionRepository.all();

    for (Session session : sessions) {
      if (session.isMarkedForRemoval()) {
        session.setSubmittedForRemoval(true);
        sessionRepository.update(session);
      }
      else
      {
        Collection<MeasurementStream> streams = session.getMeasurementStreams();
        for (MeasurementStream stream : streams)
        {
          if(stream.isMarkedForRemoval())
          {
            stream.setSubmittedForRemoval(true);
            sessionRepository.streams().update(stream);
          }
        }
      }
    }

    return sessions;
  }

  private boolean canUpload() {
    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

    return networkInfo != null
        && networkInfo.isConnected()
        && (!settingsHelper.isSyncOnlyWifi() || networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
        && settingsHelper.hasCredentials();
  }

  private void uploadSessions(UUID[] uuids) {
    for (UUID uuid : uuids) {
      Session session = sessionRepository.loadFully(uuid);
      if (session != null && !session.isMarkedForRemoval()) {
        HttpResult<CreateSessionResponse> result = sessionDriver.create(session);

        if (result.getStatus() == Status.SUCCESS) {
          updateSession(session, result.getContent());
        }
      }

      Intents.notifySyncUpdate(context);
    }
  }

  private void updateSession(Session session, CreateSessionResponse sessionResponse) {
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
        if (session == null)
        {
          Log.w(Constants.TAG, "Session [" + id + "] couldn't ");
        }
        else
        {
          try
          {
            fixTimesFromUTC(session);
            sessionRepository.save(session);
          }
          catch (RepositoryException e)
          {
            Log.e(Constants.TAG, "Error saving session [" + id + "]", e);
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
