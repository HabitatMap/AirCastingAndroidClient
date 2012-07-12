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
import pl.llp.aircasting.repository.db.WritableDatabaseTask;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import static com.google.common.collect.Lists.newArrayList;
import static pl.llp.aircasting.repository.DBHelper.*;
import static pl.llp.aircasting.repository.db.DBConstants.*;

public class SessionRepository
{
  @Language("SQL")
  private static final String SESSIONS_BY_SENSOR_QUERY =
      "SELECT " + SESSION_TABLE_NAME + ".*" +
          " FROM " + SESSION_TABLE_NAME +
          " JOIN " + STREAM_TABLE_NAME +
          " ON " + SESSION_TABLE_NAME + "." + SESSION_ID + " = " + STREAM_TABLE_NAME + "." + STREAM_SESSION_ID +
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
  public void deleteNote(final Session session, final Note note)
  {
    dbAccessor.executeWritableTask(new WritableDatabaseTask<Object>()
    {
      @Override
      public Object execute(SQLiteDatabase writableDatabase)
      {
        notes.delete(session, note, writableDatabase);
        return null;
      }
    });
  }

  @API
  public void save(@NotNull final Session session, ProgressListener progressListener)
  {
    setupProgressListener(session, progressListener);

    final ContentValues values = new ContentValues();

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
    values.put(SESSION_MARKED_FOR_REMOVAL, session.isMarkedForRemoval() ? 1 : 0);
    values.put(SESSION_SUBMITTED_FOR_REMOVAL, session.isSubmittedForRemoval() ? 1 : 0);
    values.put(SESSION_CALIBRATED, 1);

    dbAccessor.executeWritableTask(new WritableDatabaseTask<Object>()
    {
      @Override
      public Object execute(SQLiteDatabase writableDatabase)
      {
        long sessionKey = writableDatabase.insertOrThrow(SESSION_TABLE_NAME, null, values);
        session.setId(sessionKey);

        Collection<MeasurementStream> streamsToSave = session.getMeasurementStreams();

        streams.saveAll(streamsToSave, sessionKey, writableDatabase);
        notes.save(session.getNotes(), sessionKey, writableDatabase);

        return null;
      }
    });

    dbAccessor.executeReadOnlyTask(new ReadOnlyDatabaseTask<Object>()
    {
      @Override
      public Object execute(SQLiteDatabase readOnlyDatabase)
      {
        Cursor c;
        c = readOnlyDatabase.rawQuery("select count(*) from " + MEASUREMENT_TABLE_NAME + " WHERE " + MEASUREMENT_SESSION_ID + "=" + session.getId(), null);
        c.moveToFirst();
        long aLong = c.getLong(0);
        Log.d(Constants.TAG, "Actually written " + aLong);
        c.close();
        return null;
      }
    });
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
  private Session loadStreams(final Session session)
  {
    Long id = session.getId();
    List<MeasurementStream> streams = streams().findAllForSession(id);

    for (MeasurementStream stream : streams)
    {
      session.add(stream);
    }

    return session;
  }

  @API
  public Session loadShallow(final long sessionID)
  {
    return dbAccessor.executeReadOnlyTask(
        new ReadOnlyDatabaseTask<Session>()
        {
          @Override
          public Session execute(SQLiteDatabase readOnlyDB)
          {
            Cursor cursor = readOnlyDB.query(SESSION_TABLE_NAME, null, SESSION_ID + " = " + sessionID,
                                             null, null, null, null);
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
    return dbAccessor.executeReadOnlyTask(new ReadOnlyDatabaseTask<Session>()
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
  public void markSessionForRemoval(final long id)
  {
    final ContentValues values = new ContentValues();
    values.put(SESSION_MARKED_FOR_REMOVAL, 1);

    final String whereClause = SESSION_ID + " = " + id;
    dbAccessor.executeWritableTask(new WritableDatabaseTask<Void>()
    {
      @Override
      public Void execute(SQLiteDatabase writableDatabase)
      {
        writableDatabase.update(SESSION_TABLE_NAME, values, whereClause, null);
        return null;
      }
    });
    logSessionDeletion(whereClause);
  }

  @API
  public Iterable<Session> all()
  {
    return dbAccessor.executeReadOnlyTask(new ReadOnlyDatabaseTask<Iterable<Session>>()
    {
      @Override
      public Iterable<Session> execute(SQLiteDatabase readOnlyDatabase)
      {
        List<Session> result = Lists.newArrayList();
        Cursor cursor = readOnlyDatabase
            .query(SESSION_TABLE_NAME, null, null, null, null, null, SESSION_START + " DESC");

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
          Session load = loadShallow(cursor);
          result.add(load);
          cursor.moveToNext();
        }

        cursor.close();

        return result;
      }
    });
  }

  @Internal
  Cursor notDeletedCursor(@Nullable Sensor sensor, SQLiteDatabase readOnlyDatabase)
  {
    Cursor result;
    if (sensor == null)
    {
      result = readOnlyDatabase.query(SESSION_TABLE_NAME, null, SESSION_MARKED_FOR_REMOVAL + " = 0", null, null, null, SESSION_START + " DESC");
    }
    else
    {
      result = readOnlyDatabase.rawQuery(SESSIONS_BY_SENSOR_QUERY, new String[]{sensor.getSensorName()});
    }
    return result;
  }

  @API
  public void deleteSubmitted()
  {
    dbAccessor.executeWritableTask(new WritableDatabaseTask<Object>()
    {
      @Override
      public Object execute(SQLiteDatabase writableDatabase)
      {
        String condition = SESSION_SUBMITTED_FOR_REMOVAL + " = 1";
        delete(condition, writableDatabase);
        streams().deleteSubmitted(writableDatabase);
        return null;
      }
    });
  }

  @Internal
  private void delete(String condition, SQLiteDatabase writableDb) {
    Cursor cursor = writableDb.query(SESSION_TABLE_NAME, null, condition, null, null, null, null);

    cursor.moveToFirst();
    while (!cursor.isAfterLast())
    {
      Long id = getLong(cursor, SESSION_ID);
      delete(id, writableDb);

      cursor.moveToNext();
    }

    cursor.close();
  }

  @API
  public void deleteUploaded() {
    dbAccessor.executeWritableTask(new WritableDatabaseTask<Object>()
    {
      @Override
      public Object execute(SQLiteDatabase writableDatabase)
      {
        String condition = SESSION_LOCATION + " NOT NULL";
        delete(condition, writableDatabase);
        return null;
      }
    });
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

  private Session fill(final Session session, ProgressListener progressListener)
  {
    final Iterable<MeasurementStream> streams = session.getActiveMeasurementStreams();
    final MeasurementRepository r = new MeasurementRepository(progressListener);

    return dbAccessor.executeReadOnlyTask(new ReadOnlyDatabaseTask<Session>()
    {
      @Override
      public Session execute(SQLiteDatabase readOnlyDatabase)
      {
        Map<Long, List<Measurement>> load = r.load(session, readOnlyDatabase);

        for (MeasurementStream stream : streams)
        {
          if (load.containsKey(stream.getId()))
          {
            List<Measurement> measurements = load.get(stream.getId());
            if(measurements == null || measurements.isEmpty())
            {

            }
            else
            {
              stream.setMeasurements(measurements);
              session.add(stream);
            }
          }
        }

        return session;
      }
    });
  }

  @API
  public void update(final Session session)
  {
    final ContentValues values = new ContentValues();
    prepareHeader(session, values);

    final String whereClause = SESSION_ID + " = " + session.getId();

    dbAccessor.executeWritableTask(new WritableDatabaseTask()
    {
      @Override
      public Object execute(SQLiteDatabase writableDatabase)
      {
        try
        {
          notes.save(session.getNotes(), session.getId(), writableDatabase);

          writableDatabase.update(SESSION_TABLE_NAME, values, whereClause, null);
        }
        catch (SQLException e)
        {
          Log.e(Constants.TAG, "Error updating session [ " + session.getId() + " ]", e);
        }
        return null;
      }
    });

    logSessionDeletion(whereClause);
  }

  private void logSessionDeletion(final String whereClause)
  {
    dbAccessor.executeReadOnlyTask(new ReadOnlyDatabaseTask<Object>()
    {
      @Override
      public Object execute(SQLiteDatabase readOnlyDatabase)
      {
        Cursor c;
        c = readOnlyDatabase.query(SESSION_TABLE_NAME, new String[]{SESSION_ID, SESSION_MARKED_FOR_REMOVAL, SESSION_SUBMITTED_FOR_REMOVAL}, whereClause, null, null, null, null);
        if(c.moveToFirst())
        {
          String string = c.getString(0);
          String string1 = c.getString(1);
          String string2 = c.getString(2);
          Log.d(Constants.TAG, "Session " + string + ", marked " + string1 + ", submitted " + string2);
        }
        else
        {
          Log.e(Constants.TAG, "Session [" + whereClause + "] not found in database");
        }
        return "";
      }
    });
  }

  @API
  public void deleteStream(final Session session, final MeasurementStream stream)
  {
    dbAccessor.executeWritableTask(new WritableDatabaseTask<Object>()
    {
      @Override
      public Object execute(SQLiteDatabase writableDatabase)
      {
        streams.markForRemoval(stream, session.getId(), writableDatabase);
        return null;
      }
    });
  }

  @API
  public StreamRepository streams()
  {
    return streams;
  }

  public List<Session> notDeletedSessions(final Sensor selectedSensor)
  {
    return dbAccessor.executeReadOnlyTask(new ReadOnlyDatabaseTask<List<Session>>()
    {
      @Override
      public List<Session> execute(SQLiteDatabase readOnlyDatabase)
      {
        List<Session> result = newArrayList();

        Cursor cursor = notDeletedCursor(selectedSensor, readOnlyDatabase);
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
          Session session = loadShallow(cursor);
          result.add(session);
          cursor.moveToNext();
        }
        cursor.close();

        return result;
      }
    });
  }

  public void markRemovedForRemovalAsSubmitted()
  {
    dbAccessor.executeWritableTask(new WritableDatabaseTask<Object>()
    {
      @Override
      public Object execute(SQLiteDatabase writableDatabase)
      {
        @Language("SQL")
        String sql = "UPDATE " + SESSION_TABLE_NAME + " SET " + SESSION_SUBMITTED_FOR_REMOVAL + "=1 WHERE " + SESSION_MARKED_FOR_REMOVAL + "=1";
        writableDatabase.execSQL(sql);
        return null;
      }
    });
  }
}


