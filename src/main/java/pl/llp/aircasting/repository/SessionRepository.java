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
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.Session;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.google.inject.Inject;
import org.intellij.lang.annotations.Language;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import static com.google.common.collect.Lists.newArrayList;
import static pl.llp.aircasting.model.DBConstants.*;
import static pl.llp.aircasting.repository.DBHelper.*;

public class SessionRepository
{
  public static final String TAG = SessionRepository.class.getSimpleName();

  @Language("SQL")
  private static final String SESSIONS_BY_SENSOR_QUERY =
      "SELECT " + SESSION_TABLE_NAME + ".*" +
          " FROM " + SESSION_TABLE_NAME +
          " JOIN " + STREAM_TABLE_NAME +
          " ON " + SESSION_TABLE_NAME + "." + SESSION_ID + " = " + STREAM_SESSION_ID +
          " WHERE " + STREAM_SENSOR_NAME + " = ?" +
          " AND " + SESSION_MARKED_FOR_REMOVAL + " = 0" +
          " ORDER BY " + SESSION_START + " DESC";

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

  public void save(Session session, ProgressListener progressListener) {
    setupProgressListener(session, progressListener);


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
      values.put(SESSION_CALIBRATED, true);

      long sessionKey = db.insertOrThrow(SESSION_TABLE_NAME, null, values);
      session.setId(sessionKey);

      Collection<MeasurementStream> streamsToSave = session.getMeasurementStreams();

      streams.saveAll(streamsToSave, sessionKey);

      notes.save(session.getNotes(), sessionKey);
  }

  private void setupProgressListener(Session session, ProgressListener progressListener) {
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

  private Session loadStreams(Session session)
  {
    List<MeasurementStream> streams = streams().findAllForSession(session.getId());
    for (MeasurementStream stream : streams)
    {
      session.add(stream);
    }

    return session;
  }

  public Session loadShallow(long sessionID)
  {
    Cursor cursor = db.query(SESSION_TABLE_NAME, null, SESSION_ID + " = " + sessionID, null, null, null, null);
    try {
      if (cursor.getCount() > 0) {
        cursor.moveToFirst();
        return loadShallow(cursor);
      } else {
        return null;
      }
    } finally {
      cursor.close();
    }
  }

  private Session loadShallow(UUID uuid)
  {
    Cursor cursor = db.rawQuery("SELECT * FROM " + SESSION_TABLE_NAME + " WHERE " + SESSION_UUID + " = ?",
                                new String[]{uuid.toString()});

    try {
      if (cursor.getCount() == 0) return null;

      cursor.moveToFirst();
      return loadShallow(cursor);
    } finally {
      cursor.close();
    }
  }

  public void markSessionForRemoval(long id) {
    ContentValues values = new ContentValues();
    values.put(SESSION_MARKED_FOR_REMOVAL, true);

    db.update(SESSION_TABLE_NAME, values, SESSION_ID + " = " + id, null);
  }

  public Iterable<Session> all() {
    List<Session> result = newArrayList();
    Cursor cursor = db.query(SESSION_TABLE_NAME, null, null, null, null, null, SESSION_START + " DESC");

    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      Session load = loadShallow(cursor);
      result.add(load);
      cursor.moveToNext();
    }

    cursor.close();

    return result;
  }

  public Cursor notDeletedCursor(@Nullable Sensor sensor) {
    if (sensor == null) {
      return db.query(SESSION_TABLE_NAME, null, SESSION_MARKED_FOR_REMOVAL + " = 0", null, null, null, SESSION_START + " DESC");
    } else {
      return db.rawQuery(SESSIONS_BY_SENSOR_QUERY, new String[]{sensor.getSensorName()});
    }
  }

  public void deleteSubmitted()
  {
    String condition = SESSION_SUBMITTED_FOR_REMOVAL + " = 1";
    delete(condition);
  }

  private void delete(String condition) {
    Cursor cursor = db.query(SESSION_TABLE_NAME, null, condition, null, null, null, null);

    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      Long id = getLong(cursor, SESSION_ID);
      delete(id);

      cursor.moveToNext();
    }

    cursor.close();
  }

  public void deleteUploaded() {
    String condition = SESSION_LOCATION + " NOTNULL";
    delete(condition);
  }

  private void delete(Long sessionId)
  {
    try
    {
      db.delete(SESSION_TABLE_NAME, SESSION_ID + " = " + sessionId, null);
      db.delete(MEASUREMENT_TABLE_NAME, MEASUREMENT_SESSION_ID + " = " + sessionId, null);
      db.delete(STREAM_TABLE_NAME, STREAM_SESSION_ID + " = " + sessionId, null);
      db.delete(NOTE_TABLE_NAME, NOTE_SESSION_ID + " = " + sessionId, null);
    }
    catch (SQLException e)
    {
      Log.e(TAG, "Error deleting session [ " + sessionId + " ]", e);
    }
  }

  public Session loadFully(UUID uuid)
  {
    Session session = loadShallow(uuid);
    return fill(session, new NullProgressListener());
  }

  public Session loadFully(long id, ProgressListener progressListener)
  {
    Session session = loadShallow(id);
    return fill(session, progressListener);
  }

  private Session fill(Session session, ProgressListener progressListener)
  {
    Iterable<MeasurementStream> streams = session.getActiveMeasurementStreams();
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

  public void update(Session session) {
    ContentValues values = new ContentValues();
    prepareHeader(session, values);

    try
    {
      notes.save(session.getNotes(), session.getId());

      db.update(SESSION_TABLE_NAME, values, SESSION_ID + " = " + session.getId(), null);
    }
    catch (SQLException e)
    {
      Log.e(TAG, "Error updating session [ " + session.getId() + " ]", e);
    }
  }

  public void deleteStream(Long sessionId, MeasurementStream stream)
  {
    streams.markForRemoval(stream, sessionId);
  }

  public StreamRepository streams()
  {
    return streams;
  }
}
