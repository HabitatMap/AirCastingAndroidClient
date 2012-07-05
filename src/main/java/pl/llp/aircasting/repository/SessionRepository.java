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

import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.repository.db.AirCastingDB;
import pl.llp.aircasting.repository.db.ReadOnlyDatabaseTask;
import pl.llp.aircasting.util.Constants;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.google.inject.Inject;
import com.google.inject.internal.Lists;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import static pl.llp.aircasting.repository.db.DBConstants.*;
import static pl.llp.aircasting.repository.DBHelper.*;

public class SessionRepository
{
  @Language("SQL")
  private static final String SESSIONS_BY_SENSOR_QUERY =
      "SELECT " + SESSION_TABLE_NAME + ".*" +
          " FROM " + SESSION_TABLE_NAME +
          " JOIN " + STREAM_TABLE_NAME +
          " ON " + SESSION_TABLE_NAME + "." + SESSION_ID + " = " + STREAM_SESSION_ID +
          " WHERE " + STREAM_SENSOR_NAME + " = ?" +
          " AND " + SESSION_MARKED_FOR_REMOVAL + " = 0" +
          " ORDER BY " + SESSION_START + " DESC";

  @Inject AirCastingDB dbAccessor;

  @Inject NoteRepository notes;
  @Inject StreamRepository streams;

  @API
  public Session save(@NotNull Session session)
  {
    save(session, new NullProgressListener());
    return session;
  }

  @API
  public void deleteNote(Session session, Note note)
  {
    SQLiteDatabase writableDatabase = dbAccessor.getWritableDatabase();
    try
    {
      notes.delete(session, note, writableDatabase);
    } finally {
      writableDatabase.close();
    }
  }

  @API
  public void save(@NotNull Session session, ProgressListener progressListener)
  {
    setupProgressListener(session, progressListener);

    ContentValues values = new ContentValues();

    Date start = session.getStart();
    Date end = session.getEnd();

    if (session.getStart() == null || session.getEnd() == null)
    {
      fixStartEndTimeFromMeasurements(session);
    }

    prepareHeader(session, values);

    values.put(SESSION_START, start.getTime());
    values.put(SESSION_END, end.getTime());
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
    values.put(SESSION_CALIBRATED, true);

    SQLiteDatabase writableDatabase = dbAccessor.getWritableDatabase();
    try
    {
      long sessionKey = writableDatabase.insertOrThrow(SESSION_TABLE_NAME, null, values);
      session.setId(sessionKey);

      Collection<MeasurementStream> streamsToSave = session.getMeasurementStreams();

      streams.saveAll(streamsToSave, sessionKey, writableDatabase);
      notes.save(session.getNotes(), sessionKey, writableDatabase);
    }
    finally
    {
      writableDatabase.close();
    }
  }

  private void fixStartEndTimeFromMeasurements(Session session)
  {
    Date start = session.getStart();
    Date end = session.getEnd();

    for (MeasurementStream stream : session.getMeasurementStreams())
    {
      for (Measurement m : stream.getMeasurements())
      {
        if(start == null)
          start = m.getTime();
        else
          start = start.before(m.getTime()) ? start : m.getTime();

        if(end == null)
          end = m.getTime();
        else
          end = end.before(m.getTime()) ? end : m.getTime();
      }
    }

    session.setStart(start);
    session.setEnd(end);

    if(start == null || end == null)
    {
      String message = "Session [" + session.getId() + "] has incorrect start/end date [" + start + "/" + end + "]";
      throw new RepositoryException(message);
    }
}

  private void setupProgressListener(Session session, ProgressListener progressListener)
  {
    if (progressListener == null) {
      return;
    }

    int size = 0;
    Iterable<MeasurementStream> streams = session.getActiveMeasurementStreams();
    for (MeasurementStream stream : streams) {
      size += stream.getMeasurements().size();
    }

    progressListener.onSizeCalculated(size);
  }

  private void prepareHeader(Session session, ContentValues values) {
    values.put(SESSION_TITLE, session.getTitle());
    values.put(SESSION_DESCRIPTION, session.getDescription());
    values.put(SESSION_TAGS, session.getTags());
    values.put(SESSION_LOCATION, session.getLocation());
  }

  public Session loadShallow(Cursor cursor) {
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

  @Internal
  private Session loadStreams(Session session)
  {
    SQLiteDatabase readableDatabase = dbAccessor.getReadableDatabase();
    try
    {
      List<MeasurementStream> streams = streams().findAllForSession(session.getId(), readableDatabase);
      for (MeasurementStream stream : streams)
      {
        session.add(stream);
      }
    }
    finally
    {
      readableDatabase.close();
    }

    return session;
  }

  @API
  public Session loadShallow(final long sessionID)
  {
    return dbAccessor.executeReadOnlyDbTask(
        new ReadOnlyDatabaseTask<Session>()
        {
          @Override
          public Session execute(SQLiteDatabase readOnlyDatabase)
          {
            Cursor cursor = readOnlyDatabase
                .query(SESSION_TABLE_NAME, null, SESSION_ID + " = " + sessionID, null, null, null, null);
            try
            {
              if (cursor.getCount() > 0)
              {
                cursor.moveToFirst();
                return loadShallow(cursor);
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
        });
  }

  @Internal
  private Session loadShallow(final UUID uuid)
  {
    return dbAccessor.executeReadOnlyDbTask(new ReadOnlyDatabaseTask<Session>()
    {
      @Override
      public Session execute(SQLiteDatabase readOnlyDatabase)
      {
        Cursor cursor = readOnlyDatabase
            .rawQuery("SELECT * FROM " + SESSION_TABLE_NAME + " WHERE " + SESSION_UUID + " = ?",
                      new String[]{uuid.toString()});

        try
        {
          if (cursor.getCount() == 0) return null;

          cursor.moveToFirst();
          return loadShallow(cursor);
        }
        finally
        {
          cursor.close();
        }
      }
    });
  }

  @API
  public void markSessionForRemoval(long id)
  {
    ContentValues values = new ContentValues();
    values.put(SESSION_MARKED_FOR_REMOVAL, true);

    SQLiteDatabase writableDatabase = dbAccessor.getWritableDatabase();
    try
    {
      writableDatabase.update(SESSION_TABLE_NAME, values, SESSION_ID + " = " + id, null);
    }
    finally
    {
      writableDatabase.close();
    }
  }

  @API
  public Iterable<Session> all()
  {
    return dbAccessor.executeReadOnlyDbTask(new ReadOnlyDatabaseTask<Iterable<Session>>()
    {
      @Override
      public Iterable<Session> execute(SQLiteDatabase readOnlyDatabase)
      {
        List<Session> result = Lists.newArrayList();
        Cursor cursor = readOnlyDatabase.query(SESSION_TABLE_NAME, null, null, null, null, null, SESSION_START + " DESC");

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
          Session load = loadShallow(cursor);
          result.add(load);
          cursor.moveToNext();
        }

        cursor.close();

        return result;
      }
    });
  }

  @API
  public CursorWrapper notDeletedCursor(@Nullable Sensor sensor) {
    SQLiteDatabase readableDatabase = dbAccessor.getReadableDatabase();
    Cursor result = null;
    if (sensor == null) {
      result = readableDatabase.query(SESSION_TABLE_NAME, null, SESSION_MARKED_FOR_REMOVAL + " = 0", null, null, null, SESSION_START + " DESC");
    } else {
      result = readableDatabase.rawQuery(SESSIONS_BY_SENSOR_QUERY, new String[]{sensor.getSensorName()});
    }
    return new CursorWrapper(result, readableDatabase);
  }

  @API
  public void deleteSubmitted()
  {
    String condition = SESSION_SUBMITTED_FOR_REMOVAL + " = 1";
    SQLiteDatabase writableDatabase = dbAccessor.getWritableDatabase();
    delete(condition, writableDatabase);
    writableDatabase.close();
  }

  @Internal
  private void delete(String condition, SQLiteDatabase writableDb) {
    Cursor cursor = writableDb.query(SESSION_TABLE_NAME, null, condition, null, null, null, null);

    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      Long id = getLong(cursor, SESSION_ID);
      delete(id, writableDb);

      cursor.moveToNext();
    }

    cursor.close();
  }

  @API
  public void deleteUploaded() {
    String condition = SESSION_LOCATION + " NOTNULL";
    SQLiteDatabase writableDatabase = dbAccessor.getWritableDatabase();
    delete(condition, writableDatabase);
    writableDatabase.close();
  }

  private void delete(Long sessionId, SQLiteDatabase writableDb)
  {
    try
    {
      writableDb.delete(SESSION_TABLE_NAME, SESSION_ID + " = " + sessionId, null);
      writableDb.delete(MEASUREMENT_TABLE_NAME, MEASUREMENT_SESSION_ID + " = " + sessionId, null);
      writableDb.delete(STREAM_TABLE_NAME, STREAM_SESSION_ID + " = " + sessionId, null);
      writableDb.delete(NOTE_TABLE_NAME, NOTE_SESSION_ID + " = " + sessionId, null);
    }
    catch (SQLException e)
    {
      Log.e(Constants.TAG, "Error deleting session [ " + sessionId + " ]", e);
    }
  }

  @API
  public Session loadFully(UUID uuid)
  {
    Session session = loadShallow(uuid);
    return fill(session, new NullProgressListener());
  }

  @API
  public Session loadFully(long id, ProgressListener progressListener)
  {
    Session session = loadShallow(id);
    return fill(session, progressListener);
  }

  private Session fill(Session session, ProgressListener progressListener)
  {
    Iterable<MeasurementStream> streams = session.getActiveMeasurementStreams();
    MeasurementRepository r = new MeasurementRepository(progressListener);

    SQLiteDatabase readableDatabase = dbAccessor.getReadableDatabase();
    Map<Long, List<Measurement>> load = r.load(session, readableDatabase);

    for (MeasurementStream stream : streams)
    {
      if(load.containsKey(stream.getId()))
      {
        stream.setMeasurements(load.get(stream.getId()));
        session.add(stream);
      }
    }
    readableDatabase.close();

    return session;
  }

  @API
  public void update(Session session)
  {
    ContentValues values = new ContentValues();
    prepareHeader(session, values);

    SQLiteDatabase writableDb = dbAccessor.getWritableDatabase();
    try
    {
      notes.save(session.getNotes(), session.getId(), writableDb);
      writableDb.update(SESSION_TABLE_NAME, values, SESSION_ID + " = " + session.getId(), null);
    }
    catch (SQLException e)
    {
      Log.e(Constants.TAG, "Error updating session [ " + session.getId() + " ]", e);
    }
    finally {
      writableDb.close();
    }
  }

  @API
  public void deleteStream(Session session, MeasurementStream stream)
  {
    SQLiteDatabase writableDatabase = dbAccessor.getWritableDatabase();
    try
    {
      streams.markForRemoval(stream, session.getId(), writableDatabase);
    }
    finally
    {
      writableDatabase.close();
    }
  }

  @API
  public StreamRepository streams()
  {
    return streams;
  }
}


