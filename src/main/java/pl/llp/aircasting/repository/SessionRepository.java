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

import pl.llp.aircasting.model.DBConstants;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.SchemaMigrator;
import pl.llp.aircasting.model.Session;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.inject.Inject;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;

public class SessionRepository implements DBConstants {

    @Inject SchemaMigrator dbAccessor;
    SQLiteDatabase db;

    @Inject
    public void init() {
        db = dbAccessor.getWritableDatabase();
    }

    public void close() {
        db.close();
    }

    public void save(Session session) {
        save(session, null);
    }

    public void deleteNote(Session session, Note note) {
        db.delete(NOTE_TABLE_NAME, NOTE_SESSION_ID + " = " + session.getId() +
                " AND " + NOTE_NUMBER + " = " + note.getNumber(), null);
    }

    public void save(Session session, ProgressListener progressListener) {
        db.beginTransaction();

        if (progressListener != null) progressListener.onSizeCalculated(session.getMeasurements().size());

        ContentValues values = new ContentValues();

        prepareHeader(session, values);
        values.put(SESSION_AVG, session.getAvg());
        values.put(SESSION_PEAK, session.getPeak());
        values.put(SESSION_START, session.getStart().getTime());
        values.put(SESSION_END, session.getEnd().getTime());
        values.put(SESSION_UUID, session.getUUID().toString());
        values.put(SESSION_CALIBRATION, session.getCalibration());
        values.put(SESSION_OFFSET_60_DB, session.getOffset60DB());
        values.put(SESSION_CONTRIBUTE, session.getContribute());
        values.put(SESSION_DATA_TYPE, session.getDataType());
        values.put(SESSION_INSTRUMENT, session.getInstrument());
        values.put(SESSION_OS_VERSION, session.getOSVersion());
        values.put(SESSION_PHONE_MODEL, session.getPhoneModel());
        values.put(SESSION_MARKED_FOR_REMOVAL, session.isMarkedForRemoval());
        values.put(SESSION_SUBMITTED_FOR_REMOVAL, session.isSubmittedForRemoval());

        long key = db.insertOrThrow(SESSION_TABLE_NAME, null, values);
        session.setId(key);

        int count = 0;
        for (Measurement measurement : session.getMeasurements()) {
            values.clear();
            values.put(MEASUREMENT_SESSION_ID, key);
            values.put(MEASUREMENT_VALUE, measurement.getValue());
            values.put(MEASUREMENT_LATITUDE, measurement.getLatitude());
            values.put(MEASUREMENT_LONGITUDE, measurement.getLongitude());
            values.put(MEASUREMENT_TIME, measurement.getTime().getTime());

            db.insertOrThrow(MEASUREMENT_TABLE_NAME, null, values);

            count += 1;
            if (count % 100 == 0 && progressListener != null) {
                progressListener.onProgress(count);
            }
        }

        saveNotes(session.getNotes(), key);

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void saveNotes(Iterable<Note> notes, long sessionId) {
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

    private void prepareHeader(Session session, ContentValues values) {
        values.put(SESSION_TITLE, session.getTitle());
        values.put(SESSION_DESCRIPTION, session.getDescription());
        values.put(SESSION_TAGS, session.getTags());
        values.put(SESSION_LOCATION, session.getLocation());
    }

    public Session load(Cursor cursor) {
        Session session = new Session();

        session.setId(getLong(cursor, SESSION_ID));
        session.setTitle(getString(cursor, SESSION_TITLE));
        session.setDescription(getString(cursor, SESSION_DESCRIPTION));
        session.setTags(getString(cursor, SESSION_TAGS));
        session.setAvg(getDouble(cursor, SESSION_AVG));
        session.setPeak(getDouble(cursor, SESSION_PEAK));
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

        loadNotes(session);

        return session;
    }

    public Session load(long sessionID) {
        Cursor cursor = db.query(SESSION_TABLE_NAME, null, SESSION_ID + " = " + sessionID, null, null, null, null);
        try {
            cursor.moveToFirst();
            return load(cursor);
        } finally {
            cursor.close();
        }
    }

    private Session load(UUID uuid) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + SESSION_TABLE_NAME + " WHERE " + SESSION_UUID + " = ?",
                new String[]{uuid.toString()});

        try {
            if (cursor.getCount() == 0) return null;

            cursor.moveToFirst();
            return load(cursor);
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
            result.add(load(cursor));

            cursor.moveToNext();
        }

        cursor.close();

        return result;
    }

    public Cursor notDeletedCursor() {
        return db.query(SESSION_TABLE_NAME, null, SESSION_MARKED_FOR_REMOVAL + " = 0", null, null, null, SESSION_START + " DESC");
    }

    public void deleteSubmitted() {
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

    private void delete(Long id) {
        db.beginTransaction();

        db.delete(SESSION_TABLE_NAME, SESSION_ID + " = " + id, null);
        db.delete(MEASUREMENT_TABLE_NAME, MEASUREMENT_SESSION_ID + " = " + id, null);
        db.delete(NOTE_TABLE_NAME, NOTE_SESSION_ID + " = " + id, null);

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public Session loadEager(UUID uuid) {
        Session session = load(uuid);

        return fill(session, null);
    }

    public Session loadEager(long id, ProgressListener progressListener) {
        Session session = load(id);

        return fill(session, progressListener);
    }

    private Session fill(Session session, ProgressListener progressListener) {
        loadMeasurements(session, progressListener);

        return session;
    }

    private void loadNotes(Session session) {
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

            session.add(note);
            cursor.moveToNext();
        }

        cursor.close();
    }

    private void loadMeasurements(Session session, ProgressListener progressListener) {
        Cursor cursor = db.query(MEASUREMENT_TABLE_NAME, null, MEASUREMENT_SESSION_ID + " = " + session.getId(), null, null, null, MEASUREMENT_TIME);
        cursor.moveToFirst();

        if (progressListener != null) progressListener.onSizeCalculated(cursor.getCount());

        while (!cursor.isAfterLast()) {
            Measurement measurement = new Measurement();

            measurement.setLatitude(getDouble(cursor, MEASUREMENT_LATITUDE));
            measurement.setLongitude(getDouble(cursor, MEASUREMENT_LONGITUDE));
            measurement.setValue(getDouble(cursor, MEASUREMENT_VALUE));
            measurement.setTime(getDate(cursor, MEASUREMENT_TIME));

            if (progressListener != null && cursor.getPosition() % 100 == 0) {
                progressListener.onProgress(cursor.getPosition());
            }
            session.add(measurement);
            cursor.moveToNext();
        }

        cursor.close();
    }

    public void update(Session session) {
        ContentValues values = new ContentValues();
        prepareHeader(session, values);

        db.beginTransaction();

        saveNotes(session.getNotes(), session.getId());
        db.update(SESSION_TABLE_NAME, values, SESSION_ID + " = " + session.getId(), null);

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private double getDouble(Cursor cursor, String columnName) {
        return cursor.getDouble(cursor.getColumnIndex(columnName));
    }

    private Date getDate(Cursor cursor, String columnName) {
        return new Date(cursor.getLong(cursor.getColumnIndex(columnName)));
    }

    private String getString(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    private Long getLong(Cursor cursor, String columnName) {
        return cursor.getLong(cursor.getColumnIndex(columnName));
    }

    private int getInt(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }

    private boolean getBool(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName)) == 1;
    }
}
