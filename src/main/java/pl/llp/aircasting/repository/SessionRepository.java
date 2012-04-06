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
package pl.llp.aircasting.repository;

import pl.llp.aircasting.model.AirCastingDB;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.Session;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.inject.Inject;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static pl.llp.aircasting.model.DBConstants.*;
import static pl.llp.aircasting.repository.DBHelper.*;

public class SessionRepository
{
  @Inject
  AirCastingDB dbAccessor;

  SQLiteDatabase db;

  NoteRepository notes;
  StreamRepository streams;

  @Inject
  public void init()
  {
    db = dbAccessor.getWritableDatabase();
    notes = new NoteRepository(db);
    streams = new StreamRepository(db);
  }

  public void close()
  {
    db.close();
  }

  public Session save(Session session)
  {
    save(session, new NullProgressListener());
    return session;
  }

  public void deleteNote(Session session, Note note)
  {
    notes.delete(session, note);
  }

  public void save(Session session, ProgressListener progressListener)
  {
    setupProgressListener(session, progressListener);

    db.beginTransaction();
    ContentValues values = new ContentValues();

    prepareHeader(session, values);
    values.put(SESSION_START, session.getStart().getTime());
    values.put(SESSION_END, session.getEnd().getTime());
    values.put(SESSION_UUID, session.getUUID().toString());

    values.put(SESSION_CALIBRATION, session.getCalibration());
    values.put(SESSION_CONTRIBUTE, session.getContribute());
    values.put(SESSION_PHONE_MODEL, session.getPhoneModel());
    values.put(SESSION_INSTRUMENT, session.getInstrument());
    values.put(SESSION_DATA_TYPE, session.getDataType());
    values.put(SESSION_OS_VERSION, session.getOSVersion());
    values.put(SESSION_OFFSET_60_DB, session.getOffset60DB());
    values.put(SESSION_MARKED_FOR_REMOVAL, session.isMarkedForRemoval());
    values.put(SESSION_SUBMITTED_FOR_REMOVAL, session.isSubmittedForRemoval());

    long sessionKey = db.insertOrThrow(SESSION_TABLE_NAME, null, values);
    session.setId(sessionKey);

    Collection<MeasurementStream> streamsToSave = session.getMeasurementStreams();

    streams.saveAll(streamsToSave, sessionKey);

    notes.save(session.getNotes(), sessionKey);

    db.setTransactionSuccessful();
    db.endTransaction();
  }

  private void setupProgressListener(Session session, ProgressListener progressListener)
  {
    if (progressListener == null)
    {
      return;
    }

    int size = 0;
    Collection<MeasurementStream> streams = session.getMeasurementStreams();
    for (MeasurementStream stream : streams)
    {
      size += stream.getMeasurements().size();
    }

    progressListener.onSizeCalculated(size);
  }

  private void prepareHeader(Session session, ContentValues values)
  {
    values.put(SESSION_TITLE, session.getTitle());
    values.put(SESSION_DESCRIPTION, session.getDescription());
    values.put(SESSION_TAGS, session.getTags());
    values.put(SESSION_LOCATION, session.getLocation());
  }

  public Session load(Cursor cursor)
  {
    Session session = new Session();

    session.setId(getLong(cursor, SESSION_ID));
    session.setTitle(getString(cursor, SESSION_TITLE));
    session.setDescription(getString(cursor, SESSION_DESCRIPTION));
    session.setTags(getString(cursor, SESSION_TAGS));
    session.setStart(getDate(cursor, SESSION_START));
    session.setEnd(getDate(cursor, SESSION_END));
    session.setUuid(UUID.fromString(getString(cursor, SESSION_UUID)));
    session.setLocation(getString(cursor, SESSION_LOCATION));
    session.setCalibration(getInt(cursor, SESSION_CALIBRATION));
    session.setOffset60DB(getInt(cursor, SESSION_OFFSET_60_DB));
    session.setContribute(getBool(cursor, SESSION_CONTRIBUTE));
    session.setDataType(getString(cursor, SESSION_DATA_TYPE));
    session.setOsVersion(getString(cursor, SESSION_OS_VERSION));
    session.setPhoneModel(getString(cursor, SESSION_PHONE_MODEL));
    session.setInstrument(getString(cursor, SESSION_INSTRUMENT));
    session.setMarkedForRemoval(getBool(cursor, SESSION_MARKED_FOR_REMOVAL));
    session.setSubmittedForRemoval(getBool(cursor, SESSION_SUBMITTED_FOR_REMOVAL));

    List<Note> loadedNotes = notes.load(session);
    session.addAll(loadedNotes);

    loadStreams(session);

    return session;
  }

  private Session loadStreams(Session session)
  {
    List<MeasurementStream> streams = new StreamRepository(db).findAllForSession(session.getId());
    for (MeasurementStream stream : streams)
    {
      session.add(stream);
    }

    return session;
  }

  public Session loadShallow(long sessionID)
  {
    Cursor cursor = db.query(SESSION_TABLE_NAME, null, SESSION_ID + " = " + sessionID, null, null, null, null);
    try
    {
      if(cursor.getCount() > 0)
      {
        cursor.moveToFirst();
        return load(cursor);
      }
      else
      {
        return null;
      }
    }
    finally
    {
      cursor.close();
    }
  }

  private Session loadShallow(UUID uuid)
  {
    Cursor cursor = db.rawQuery("SELECT * FROM " + SESSION_TABLE_NAME + " WHERE " + SESSION_UUID + " = ?",
                                new String[]{uuid.toString()});

    try
    {
      if (cursor.getCount() == 0) return null;

      cursor.moveToFirst();
      return load(cursor);
    } finally
    {
      cursor.close();
    }
  }

  public void markSessionForRemoval(long id)
  {
    ContentValues values = new ContentValues();
    values.put(SESSION_MARKED_FOR_REMOVAL, true);

    db.update(SESSION_TABLE_NAME, values, SESSION_ID + " = " + id, null);
  }

  public Iterable<Session> all()
  {
    List<Session> result = newArrayList();
    Cursor cursor = db.query(SESSION_TABLE_NAME, null, null, null, null, null, SESSION_START + " DESC");

    cursor.moveToFirst();
    while (!cursor.isAfterLast())
    {
      Session load = load(cursor);
      result.add(load);
      cursor.moveToNext();
    }

    cursor.close();

    return result;
  }

  public Cursor notDeletedCursor()
  {
    return db.query(SESSION_TABLE_NAME, null, SESSION_MARKED_FOR_REMOVAL + " = 0", null, null, null, SESSION_START + " DESC");
  }

  public void deleteSubmitted()
  {
    String condition = SESSION_SUBMITTED_FOR_REMOVAL + " = 1";
    delete(condition);
  }

  private void delete(String condition)
  {
    Cursor cursor = db.query(SESSION_TABLE_NAME, null, condition, null, null, null, null);

    cursor.moveToFirst();
    while (!cursor.isAfterLast())
    {
      Long id = getLong(cursor, SESSION_ID);
      delete(id);

      cursor.moveToNext();
    }

    cursor.close();
  }

  public void deleteUploaded()
  {
    String condition = SESSION_LOCATION + " NOTNULL";
    delete(condition);
  }

  private void delete(Long id)
  {
    db.beginTransaction();

    db.delete(SESSION_TABLE_NAME, SESSION_ID + " = " + id, null);
    db.delete(MEASUREMENT_TABLE_NAME, MEASUREMENT_SESSION_ID + " = " + id, null);
    db.delete(NOTE_TABLE_NAME, NOTE_SESSION_ID + " = " + id, null);

    db.setTransactionSuccessful();
    db.endTransaction();
  }

  public Session loadFully(UUID uuid)
  {
    Session session = loadShallow(uuid);

    return fill(session, null);
  }

  public Session loadFully(long id, ProgressListener progressListener)
  {
    Session session = loadShallow(id);
    return fill(session, progressListener);
  }

  private Session fill(Session session, ProgressListener progressListener)
  {
    Collection<MeasurementStream> streams = session.getMeasurementStreams();
    MeasurementRepository r = new MeasurementRepository(db, progressListener);
    Map<Long, List<Measurement>> load = r.load(session);

    for (MeasurementStream stream : streams)
    {
      if(load.containsKey(stream.getId()))
      {
        stream.setMeasurements(load.get(stream.getId()));
        session.add(stream);
      }
    }

    return session;
  }

  public void update(Session session)
  {
    ContentValues values = new ContentValues();
    prepareHeader(session, values);

    db.beginTransaction();

    notes.save(session.getNotes(), session.getId());
    db.update(SESSION_TABLE_NAME, values, SESSION_ID + " = " + session.getId(), null);

    db.setTransactionSuccessful();
    db.endTransaction();
  }
}
