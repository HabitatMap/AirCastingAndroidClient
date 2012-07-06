package pl.llp.aircasting.service;

import pl.llp.aircasting.api.SessionDriver;
import pl.llp.aircasting.api.SyncDriver;
import pl.llp.aircasting.api.data.CreateSessionResponse;
import pl.llp.aircasting.api.data.SyncResponse;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.repository.ProgressListener;
import pl.llp.aircasting.repository.RepositoryException;
import pl.llp.aircasting.repository.SessionRepository;
import pl.llp.aircasting.util.Constants;
import pl.llp.aircasting.util.http.HttpResult;
import pl.llp.aircasting.util.http.Status;

import android.util.Log;
import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.google.inject.Inject;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Iterables.find;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by ags on 05/07/12 at 22:23
 */
public class SessionSyncer
{
  @Inject SessionRepository sessionRepository;
  @Inject SessionDriver sessionDriver;
  @Inject SyncDriver syncDriver;

  public void performSync(ProgressListener listener)
  {
    List<Session> sessionsToUpdate = prepareSessions();
    listener.onSizeCalculated(sessionsToUpdate.size());

    HttpResult<SyncResponse> result = syncDriver.sync(sessionsToUpdate);
    SyncResponse syncResponse = result.getContent();
    if(syncResponse != null)
    {
      indicateSynced(sessionsToUpdate);

      UUID[] upload = syncResponse.getUpload();
      long[] download = syncResponse.getDownload();
      listener.onSizeCalculated(download.length + upload.length);

      uploadSessions(upload, listener);

      sessionRepository.deleteSubmitted();

      downloadSessions(download, listener, upload.length);
    }
  }

  private void indicateSynced(List<Session> sessionsToUpdate)
  {
    for (Session session : sessionsToUpdate) {
      if (session.isMarkedForRemoval()) {
        session.setSubmittedForRemoval(true);
      }
      else
      {
        Collection<MeasurementStream> streams = session.getMeasurementStreams();
        for (MeasurementStream stream : streams)
        {
          if(stream.isMarkedForRemoval())
          {
            stream.setSubmittedForRemoval(true);
          }
        }
      }
    }
    sessionRepository.markRemovedForRemovalAsSubmitted();
    sessionRepository.streams().markRemovedForRemovalAsSubmitted();
  }

  private List<Session> prepareSessions()
  {
      List<Session> sessions = newArrayList(sessionRepository.all());
      return sessions;
  }

  private void uploadSessions(UUID[] uuids, ProgressListener listener)
  {
    int step = 0;
    Stopwatch watch = new Stopwatch();
    for (UUID uuid : uuids)
    {
      watch.reset().start();
      Session session = sessionRepository.loadFully(uuid);
      Log.d(Constants.TAG, "loading session from db [" + uuid + "] took:" + watch.elapsedMillis());
      if (session != null && !session.isMarkedForRemoval())
      {
        HttpResult<CreateSessionResponse> result = sessionDriver.create(session);

        if (result.getStatus() == Status.SUCCESS)
        {
          updateSession(session, result.getContent());
        }
      }
      Log.d(Constants.TAG, "upload of session [" + uuid + "] took: " + watch.stop().elapsedMillis());
      step++;
      listener.onProgress(step);
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

    private void downloadSessions(long[] ids, ProgressListener listener, int length)
    {
      int step = 0;
      Stopwatch watch = new Stopwatch();
      Stopwatch dbCounter = new Stopwatch().start();

      for (long id : ids)
      {
        HttpResult<Session> result = sessionDriver.show(id);
        watch.reset().start();
        if (result.getStatus() == Status.SUCCESS) {
          Session session = result.getContent();
          if (session == null)
          {
            Log.w(Constants.TAG, "Session [" + id + "] couldn't be downloaded");
          }
          else
          {
            try
            {
              dbCounter.reset().start();
              sessionRepository.save(session);
              Log.d(Constants.TAG, "Saving session [" + id + "] took: " + dbCounter.elapsedMillis());
            }
            catch (RepositoryException e)
            {
              Log.e(Constants.TAG, "Error saving session [" + id + "]", e);
            }
          }
        }
        step++;
        Log.d(Constants.TAG, "download of session [" + id + "] took: " + watch.stop().elapsedMillis());
        listener.onProgress(length + step);
      }
    }
}
