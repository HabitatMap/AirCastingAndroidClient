package pl.llp.aircasting.repository;

import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.Session;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static pl.llp.aircasting.model.DBConstants.*;
import static pl.llp.aircasting.repository.DBHelper.getDate;
import static pl.llp.aircasting.repository.DBHelper.getDouble;
import static pl.llp.aircasting.repository.DBHelper.getInt;
import static pl.llp.aircasting.repository.DBHelper.getString;

public class NoteRepository
{
  private SQLiteDatabase db;

  public NoteRepository(SQLiteDatabase db)
  {
    this.db = db;
  }

  void save(Iterable<Note> notes, long sessionId) {
    db.delete(NOTE_TABLE_NAME, NOTE_SESSION_ID + " = " + sessionId, null);

    ContentValues values = new ContentValues();

    for (Note note : notes) {
      values.clear();
      values.put(NOTE_SESSION_ID, sessionId);
      values.put(NOTE_LATITUDE, note.getLatitude());
      values.put(NOTE_LONGITUDE, note.getLongitude());
      values.put(NOTE_TEXT, note.getText());
      values.put(NOTE_DATE, note.getDate().getTime());
      values.put(NOTE_PHOTO, note.getPhotoPath());
      values.put(NOTE_NUMBER, note.getNumber());

      db.insertOrThrow(NOTE_TABLE_NAME, null, values);
    }
  }

  List<Note> load(Session session) {

    List<Note> result = newArrayList();

    Cursor cursor = db.query(NOTE_TABLE_NAME, null, NOTE_SESSION_ID + " = " + session.getId(), null, null, null, null);
    cursor.moveToFirst();

    while (!cursor.isAfterLast()) {
      Note note = new Note();

      note.setLatitude(getDouble(cursor, NOTE_LATITUDE));
      note.setLongitude(getDouble(cursor, NOTE_LONGITUDE));
      note.setDate(getDate(cursor, NOTE_DATE));
      note.setText(getString(cursor, NOTE_TEXT));
      note.setPhotoPath(getString(cursor, NOTE_PHOTO));
      note.setNumber(getInt(cursor, NOTE_NUMBER));

      result.add(note);
      cursor.moveToNext();
    }

    cursor.close();
    return result;
  }

  public void delete(Session session, Note note) {
      db.delete(NOTE_TABLE_NAME, NOTE_SESSION_ID + " = " + session.getId() +
              " AND " + NOTE_NUMBER + " = " + note.getNumber(), null);
  }
}

