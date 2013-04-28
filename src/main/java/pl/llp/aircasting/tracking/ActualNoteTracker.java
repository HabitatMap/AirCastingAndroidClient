package pl.llp.aircasting.tracking;

import pl.llp.aircasting.event.session.NoteCreatedEvent;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.storage.DatabaseTaskQueue;
import pl.llp.aircasting.storage.db.WritableDatabaseTask;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import com.google.common.eventbus.EventBus;

import static pl.llp.aircasting.storage.db.DBConstants.*;

/**
 * Created by ags on 27/03/2013 at 00:42
 */
class ActualNoteTracker implements NoteTracker
{
  final EventBus eventBus;
  final DatabaseTaskQueue dbQueue;

  ActualNoteTracker(EventBus eventBus, DatabaseTaskQueue taskQueue)
  {
    this.eventBus = eventBus;
    this.dbQueue = taskQueue;
  }

  public void addNote(final Session session, final Note note)
  {
    session.add(note);
    notifyNote(note);
    dbQueue.add(new WritableDatabaseTask<Void>()
    {
      @Override
      public Void execute(SQLiteDatabase writableDatabase)
      {
        ContentValues values = new ContentValues();
        values.put(NOTE_SESSION_ID, session.getId());
        values.put(NOTE_LATITUDE, note.getLatitude());
        values.put(NOTE_LONGITUDE, note.getLongitude());
        values.put(NOTE_TEXT, note.getText());
        values.put(NOTE_DATE, note.getDate().getTime());
        values.put(NOTE_PHOTO, note.getPhotoPath());
        values.put(NOTE_NUMBER, note.getNumber());

        writableDatabase.insertOrThrow(NOTE_TABLE_NAME, null, values);
        return null;
      }
    });
  }

  @Override
  public void deleteNote(final Session session, final Note note)
  {
    session.deleteNote(note);
    dbQueue.add(new WritableDatabaseTask()
    {
      @Override
      public Void execute(SQLiteDatabase writableDb)
      {
        writableDb.delete(NOTE_TABLE_NAME, NOTE_SESSION_ID + " = " + session.getId() + " " +
                "AND " + NOTE_NUMBER + " = " + note.getNumber(), null);
        return null;
      }
    });
  }

  private void notifyNote(Note note)
  {
    eventBus.post(new NoteCreatedEvent(note));
  }
}
