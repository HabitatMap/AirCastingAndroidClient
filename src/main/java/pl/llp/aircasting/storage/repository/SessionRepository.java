/**
 * AirCasting - Share your Air!
 * Copyright (C) 2011-2012 HabitatMap, Inc.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * You can contact the authors by email at <info@habitatmap.org>
 */
package pl.llp.aircasting.storage.repository;

import com.google.common.eventbus.EventBus;
import pl.llp.aircasting.android.Logger;
import pl.llp.aircasting.helper.NoOp;
import pl.llp.aircasting.model.*;
import pl.llp.aircasting.model.events.FixedSessionsMeasurementEvent;
import pl.llp.aircasting.model.events.MeasurementEvent;
import pl.llp.aircasting.storage.ProgressListener;
import pl.llp.aircasting.storage.db.AirCastingDB;
import pl.llp.aircasting.storage.db.DBConstants;
import pl.llp.aircasting.storage.db.ReadOnlyDatabaseTask;
import pl.llp.aircasting.storage.db.WritableDatabaseTask;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static pl.llp.aircasting.storage.DBHelper.*;
import static pl.llp.aircasting.storage.db.DBConstants.*;

public class SessionRepository {
    @Inject AirCastingDB dbAccessor;

    @Inject NoteRepository notes;
    @Inject StreamRepository streams;

    @Inject SessionDAO sessionDAO;
    @Inject SessionTrackerDAO trackedSessionsDAO;
    @Inject ViewingSessionsManager viewingSessionsManager;
    @Inject EventBus eventBus;

    @API
    public Session save(@NotNull final Session session) {
        final ContentValues values = sessionDAO.asValues(session);

        dbAccessor.executeWritableTask(new WritableDatabaseTask<Object>() {
            @Override
            public Object execute(SQLiteDatabase writableDatabase) {
                long sessionKey = writableDatabase.insertOrThrow(SESSION_TABLE_NAME, null, values);
                session.setId(sessionKey);

                Collection<MeasurementStream> streamsToSave = session.getMeasurementStreams();

                streams.saveAll(streamsToSave, sessionKey, writableDatabase);
                notes.save(session.getNotes(), sessionKey, writableDatabase);

                return null;
            }
        });

        dbAccessor.executeReadOnlyTask(new ReadOnlyDatabaseTask<Object>() {
            @Override
            public Object execute(SQLiteDatabase readOnlyDatabase) {
                Cursor c;
                c = readOnlyDatabase.rawQuery("select count(*) from " + MEASUREMENT_TABLE_NAME + " WHERE " + MEASUREMENT_SESSION_ID + "=" + session.getId(), null);
                c.moveToFirst();
                long aLong = c.getLong(0);
                Logger.d("Actually written " + aLong);
                c.close();
                return null;
            }
        });

        return session;
    }

    @API
    public void saveNewData(final Session oldSession, @NotNull final Session downloadedSession, ProgressListener progressListener) {
        dbAccessor.executeWritableTask(new WritableDatabaseTask<Object>() {
            @Override
            public Object execute(SQLiteDatabase writableDatabase) {
                boolean streamsAdded = false;
                long sessionId = oldSession.getId();
                Collection<MeasurementStream> potentialStreams = downloadedSession.getMeasurementStreams();

                    for (MeasurementStream potentialStream : potentialStreams) {
                        MeasurementStream existingStream = oldSession.getStream(potentialStream.getSensorName());

                        if (existingStream == null) {
                            streams.saveNewStreamAndMeasurements(potentialStream, sessionId, writableDatabase);
                            streamsAdded = true;
                        } else {
                            Logger.w("saving only measurements for session " + sessionId);
                            streams.saveNewMeasurements(potentialStream, existingStream.getId(), sessionId, writableDatabase);
                        }
                    }

                if (streamsAdded) {
                    viewingSessionsManager.view(sessionId, NoOp.progressListener());
                }

                return null;
            }
        });

        fillNewData(oldSession, progressListener);
    }

    @API
    private void fillNewData(final Session oldSession, final ProgressListener progressListener) {
        final Iterable<MeasurementStream> streams = oldSession.getActiveMeasurementStreams();
        final MeasurementRepository r = new MeasurementRepository(progressListener);

        dbAccessor.executeReadOnlyTask(new ReadOnlyDatabaseTask<Session>() {
            @Override
            public Session execute(SQLiteDatabase readOnlyDatabase) {
                boolean measurementsAdded = false;
                Map<Long, List<Measurement>> load = r.loadNew(oldSession, oldSession.getEnd(), readOnlyDatabase);

                for (MeasurementStream stream : streams) {
                    if (load.containsKey(stream.getId())) {
                        List<Measurement> measurements = load.get(stream.getId());
                        if (measurements == null || measurements.isEmpty()) {

                        } else {
                            stream.addMeasurements(measurements);
                            oldSession.add(stream);
                            oldSession.setEnd(stream.getLastMeasurementTime());
                            updateSessionEndDate(oldSession);
                            measurementsAdded = true;
                            MeasurementEvent event = new MeasurementEvent(measurements.get(measurements.size() -1 ), new Sensor(stream.getSensorName()));
                            event.setSessionId(oldSession.getId());
                            eventBus.post(event);
                        }
                    }
                }

                if (measurementsAdded) {
                    notifyOfNewData(oldSession.getId());
                }

                return null;
            }
        });
    }

    private void notifyOfNewData(Long sessionId) {
        FixedSessionsMeasurementEvent event = new FixedSessionsMeasurementEvent(sessionId);

        eventBus.post(event);
    }

    private void prepareHeader(Session session, ContentValues values) {
        values.put(SESSION_TITLE, session.getTitle());
        values.put(SESSION_DESCRIPTION, session.getDescription());
        values.put(SESSION_TAGS, session.getTags());
        values.put(SESSION_LOCATION, session.getLocation());
    }

    public Session loadShallow(Cursor cursor) {
        Session session = new Session();

        sessionDAO.loadDetails(cursor, session);

        List<Note> loadedNotes = notes.load(session);
        session.addAll(loadedNotes);

        loadStreams(session);

        return session;
    }

    @Internal
    private Session loadStreams(final Session session) {
        Long id = session.getId();
        List<MeasurementStream> streams = streams().findAllForSession(id);

        for (MeasurementStream stream : streams) {
            session.add(stream);
        }

        return session;
    }

    @API
    public List<Session> loadShallow(List<Long> sessionIds) {
        List<Session> result = newArrayList();

        for (Long sessionId : sessionIds) {
            result.add(loadShallow(sessionId));
        }

        return result;
    }

    @API
    public Session loadShallow(final long sessionID) {
        return dbAccessor.executeReadOnlyTask(
                new ReadOnlyDatabaseTask<Session>() {
                    @Override
                    public Session execute(SQLiteDatabase readOnlyDB) {
                        Cursor cursor = readOnlyDB.query(SESSION_TABLE_NAME, null, SESSION_ID + " = " + sessionID,
                                null, null, null, null);
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
                });
    }

    @Internal
    private Session loadShallow(final UUID uuid) {
        return dbAccessor.executeReadOnlyTask(new ReadOnlyDatabaseTask<Session>() {
            @Override
            public Session execute(SQLiteDatabase readOnlyDatabase) {
                Cursor cursor = readOnlyDatabase
                        .rawQuery("SELECT * FROM " + SESSION_TABLE_NAME + " WHERE " + SESSION_UUID + " = ?",
                                new String[]{uuid.toString()});

                try {
                    if (cursor.getCount() == 0) return null;

                    cursor.moveToFirst();
                    return loadShallow(cursor);
                } finally {
                    cursor.close();
                }
            }
        });
    }

    @API
    public void markSessionForRemoval(UUID uuid) {
        final ContentValues values = new ContentValues();
        values.put(SESSION_MARKED_FOR_REMOVAL, 1);

        final String whereClause = SESSION_UUID + " = '" + uuid.toString() + "'";
        dbAccessor.executeWritableTask(new WritableDatabaseTask<Void>() {
            @Override
            public Void execute(SQLiteDatabase writableDatabase) {
                writableDatabase.update(SESSION_TABLE_NAME, values, whereClause, null);
                return null;
            }
        });
        logSessionDeletion(whereClause);
    }

    @API
    public void markSessionForRemoval(final long id) {
        final ContentValues values = new ContentValues();
        values.put(SESSION_MARKED_FOR_REMOVAL, 1);

        final String whereClause = SESSION_ID + " = " + id;
        dbAccessor.executeWritableTask(new WritableDatabaseTask<Void>() {
            @Override
            public Void execute(SQLiteDatabase writableDatabase) {
                writableDatabase.update(SESSION_TABLE_NAME, values, whereClause, null);
                return null;
            }
        });
        logSessionDeletion(whereClause);
    }

    @API
    public List<Session> allCompleteSessions() {
        return dbAccessor.executeReadOnlyTask(new ReadOnlyDatabaseTask<List<Session>>() {
            @Override
            public List<Session> execute(SQLiteDatabase readOnlyDatabase) {
                List<Session> result = Lists.newArrayList();

                String condition = DBConstants.SESSION_INCOMPLETE + " = 0 OR " + DBConstants.SESSION_TYPE + " = 'FixedSession'";

                Cursor cursor = readOnlyDatabase
                        .query(SESSION_TABLE_NAME, null, condition, null, null, null, SESSION_START + " DESC");

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

    @Internal
    Cursor notDeletedCursor(SQLiteDatabase readOnlyDatabase) {
        return sessionDAO.notDeletedCursor(readOnlyDatabase);
    }

    @API
    public void deleteSubmitted() {
        dbAccessor.executeWritableTask(new WritableDatabaseTask<Object>() {
            @Override
            public Object execute(SQLiteDatabase writableDatabase) {
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
        while (!cursor.isAfterLast()) {
            Long id = getLong(cursor, SESSION_ID);
            delete(id, writableDb);

            cursor.moveToNext();
        }

        cursor.close();
    }

    @API
    public void deleteUploaded() {
        dbAccessor.executeWritableTask(new WritableDatabaseTask<Object>() {
            @Override
            public Object execute(SQLiteDatabase writableDatabase) {
                String condition = SESSION_LOCATION + " NOT NULL";
                delete(condition, writableDatabase);
                return null;
            }
        });
    }

    private void delete(Long sessionId, SQLiteDatabase writableDb) {
        try {
            writableDb.delete(SESSION_TABLE_NAME, SESSION_ID + " = " + sessionId, null);
            writableDb.delete(MEASUREMENT_TABLE_NAME, MEASUREMENT_SESSION_ID + " = " + sessionId, null);
            writableDb.delete(STREAM_TABLE_NAME, STREAM_SESSION_ID + " = " + sessionId, null);
            writableDb.delete(NOTE_TABLE_NAME, NOTE_SESSION_ID + " = " + sessionId, null);
        } catch (SQLException e) {
            Logger.e("Error deleting session [ " + sessionId + " ]", e);
        }
    }

    @API
    public Session loadFully(UUID uuid) {
        Session session = loadShallow(uuid);
        if (session != null) {
            fill(session, NoOp.progressListener());
        }

        return session;
    }

    @API
    public Session loadFully(long id, ProgressListener progressListener) {
        Session session = loadShallow(id);
        if (session != null) {
            fill(session, progressListener);
        }
        return session;
    }

    private Session fill(final Session session, ProgressListener progressListener) {
        if (session == null) {
            return session;
        }

        final Iterable<MeasurementStream> streams = session.getActiveMeasurementStreams();
        final MeasurementRepository r = new MeasurementRepository(progressListener);

        return dbAccessor.executeReadOnlyTask(new ReadOnlyDatabaseTask<Session>() {
            @Override
            public Session execute(SQLiteDatabase readOnlyDatabase) {
                Map<Long, List<Measurement>> load = r.load(session, readOnlyDatabase);

                for (MeasurementStream stream : streams) {
                    if (load.containsKey(stream.getId())) {
                        List<Measurement> measurements = load.get(stream.getId());
                        if (measurements == null || measurements.isEmpty()) {

                        } else {
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
    public void updateSessionEndDate(final Session session) {
        long endDate = session.getEnd().getTime();
        final ContentValues values = new ContentValues();

        values.put(SESSION_END, endDate);

        final String whereClause = SESSION_ID + " = " + session.getId();

        dbAccessor.executeWritableTask(new WritableDatabaseTask() {
            @Override
            public Object execute(SQLiteDatabase writableDatabase) {
                try {
                    writableDatabase.update(SESSION_TABLE_NAME, values, whereClause, null);
                } catch (SQLException e) {
                    Logger.e("Error updating session [ " + session.getId() + " ]", e);
                }
                return null;
            }
        });
    }

    @API
    public void update(final Session session) {
        final ContentValues values = new ContentValues();
        prepareHeader(session, values);

        final String whereClause = SESSION_ID + " = " + session.getId();

        dbAccessor.executeWritableTask(new WritableDatabaseTask() {
            @Override
            public Object execute(SQLiteDatabase writableDatabase) {
                try {
                    notes.save(session.getNotes(), session.getId(), writableDatabase);

                    writableDatabase.update(SESSION_TABLE_NAME, values, whereClause, null);
                } catch (SQLException e) {
                    Logger.e("Error updating session [ " + session.getId() + " ]", e);
                }
                return null;
            }
        });

        logSessionDeletion(whereClause);
    }

    private void logSessionDeletion(final String whereClause) {
        dbAccessor.executeReadOnlyTask(new ReadOnlyDatabaseTask<Object>() {
            @Override
            public Object execute(SQLiteDatabase readOnlyDatabase) {
                Cursor c;
                c = readOnlyDatabase.query(SESSION_TABLE_NAME, new String[]{SESSION_ID, SESSION_MARKED_FOR_REMOVAL, SESSION_SUBMITTED_FOR_REMOVAL}, whereClause, null, null, null, null);
                if (c.moveToFirst()) {
                    String string = c.getString(0);
                    String string1 = c.getString(1);
                    String string2 = c.getString(2);
                    Logger.d("Session " + string + ", marked " + string1 + ", submitted " + string2);
                } else {
                    Logger.e("Session [" + whereClause + "] not found in database");
                }
                c.close();
                return "";
            }
        });
    }

    @API
    public void deleteStream(final Session session, final MeasurementStream stream) {
        dbAccessor.executeWritableTask(new WritableDatabaseTask<Object>() {
            @Override
            public Object execute(SQLiteDatabase writableDatabase) {
                streams.markForRemoval(stream, session.getId(), writableDatabase);
                return null;
            }
        });
    }

    @API
    public StreamRepository streams() {
        return streams;
    }

    public List<Session> notDeletedSessions() {
        return dbAccessor.executeReadOnlyTask(new ReadOnlyDatabaseTask<List<Session>>() {
            @Override
            public List<Session> execute(SQLiteDatabase readOnlyDatabase) {
                List<Session> result = newArrayList();

                Cursor cursor = notDeletedCursor(readOnlyDatabase);
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    Session session = loadShallow(cursor);
                    result.add(session);
                    cursor.moveToNext();
                }
                cursor.close();

                return result;
            }
        });
    }

    public void deleteLocationless() {
        dbAccessor.executeWritableTask(new WritableDatabaseTask<Object>() {
            @Override
            public Object execute(SQLiteDatabase writableDatabase) {
                String condition = DBConstants.SESSION_LOCAL_ONLY + " = 1";
                delete(condition, writableDatabase);
                streams().deleteSubmitted(writableDatabase);
                return null;
            }
        });
    }

    public void deleteCompletely(final long sessionId) {
        dbAccessor.executeWritableTask(new WritableDatabaseTask<Object>() {
            @Override
            public Object execute(SQLiteDatabase writableDatabase) {
                String condition = SESSION_ID + " = " + sessionId;
                delete(condition, writableDatabase);
                streams().deleteSubmitted(writableDatabase);
                return null;
            }
        });
    }

    public List<Session> unfinishedSessions() {
        return trackedSessionsDAO.unfinishedSessions();
    }

    public WritableDatabaseTask<Void> addStreamTask(final MeasurementStream stream, final Session session) {
        WritableDatabaseTask<Void> writableDatabaseTask = new WritableDatabaseTask<Void>() {
            @Override
            public Void execute(SQLiteDatabase writableDatabase) {
                ContentValues values = StreamRepository.values(stream);
                values.put(STREAM_SESSION_ID, session.getId());
                long streamId = writableDatabase.insertOrThrow(STREAM_TABLE_NAME, null, values);
                stream.setId(streamId);
                stream.setSessionId(session.getId());
                return null;
            }
        };
        return writableDatabaseTask;
    }

    public void updateStream(MeasurementStream stream) {
        streams().update(stream);
    }

    public void complete(long sessionId) {
        trackedSessionsDAO.complete(sessionId);
    }

    public void completeFixedSession(long sessionId) {
        trackedSessionsDAO.completeFixedSession(sessionId);
    }
}

