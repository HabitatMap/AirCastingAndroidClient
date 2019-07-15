package pl.llp.aircasting.storage.repository;

import pl.llp.aircasting.util.Logger;
import pl.llp.aircasting.screens.common.helpers.NoOp;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.storage.db.AirCastingDB;
import pl.llp.aircasting.storage.db.ReadOnlyDatabaseTask;
import pl.llp.aircasting.storage.db.WritableDatabaseTask;
import pl.llp.aircasting.util.Constants;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import com.google.inject.Inject;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static pl.llp.aircasting.storage.DBHelper.*;
import static pl.llp.aircasting.storage.db.DBConstants.*;

public class StreamRepository {
    @Language("SQLite")
    private static final String STREAM_IS_SUBMITTED_FOR_DELETE = STREAM_SUBMITTED_FOR_REMOVAL + " = 1 ";

    @Inject
    AirCastingDB airCastingDB;

    MeasurementRepository measurements;

    public StreamRepository() {
        measurements = new MeasurementRepository(NoOp.progressListener());
    }

    @Internal
    List<MeasurementStream> findAllForSession(@NotNull final Long sessionId) {
        return airCastingDB.executeReadOnlyTask(new ReadOnlyDatabaseTask<List<MeasurementStream>>() {
            @Override
            public List<MeasurementStream> execute(SQLiteDatabase readOnlyDatabase) {
                List<MeasurementStream> result = newArrayList();

                Cursor c = readOnlyDatabase.rawQuery("SELECT * FROM " + STREAM_TABLE_NAME +
                        " WHERE " + STREAM_SESSION_ID + " = " + sessionId + "", null);

                c.moveToFirst();

                while (!c.isAfterLast()) {
                    String sensor = getString(c, STREAM_SENSOR_NAME);
                    String packageName = getString(c, STREAM_SENSOR_PACKAGE_NAME);
                    String symbol = getString(c, STREAM_MEASUREMENT_SYMBOL);
                    String unit = getString(c, STREAM_MEASUREMENT_UNIT);
                    String type = getString(c, STREAM_MEASUREMENT_TYPE);
                    String shortType = getString(c, STREAM_SHORT_TYPE);

                    int thresholdVeryLow = getInt(c, STREAM_THRESHOLD_VERY_LOW);
                    int thresholdLow = getInt(c, STREAM_THRESHOLD_LOW);
                    int thresholdMedium = getInt(c, STREAM_THRESHOLD_MEDIUM);
                    int thresholdHigh = getInt(c, STREAM_THRESHOLD_HIGH);
                    int thresholdVeryHigh = getInt(c, STREAM_THRESHOLD_VERY_HIGH);

                    MeasurementStream stream;

                    stream = new MeasurementStream(packageName, sensor, type, shortType, unit, symbol,
                            thresholdVeryLow,
                            thresholdLow,
                            thresholdMedium,
                            thresholdHigh,
                            thresholdVeryHigh);

                    double avg = getDouble(c, STREAM_AVG);
                    double peak = getDouble(c, STREAM_PEAK);
                    long id = getLong(c, STREAM_ID);
                    boolean markedForRemoval = getBool(c, STREAM_MARKED_FOR_REMOVAL);

                    stream.setId(id);
                    stream.setSessionId(sessionId);

                    stream.setMarkedForRemoval(markedForRemoval);
                    stream.setAvg(avg);
                    stream.setPeak(peak);
                    result.add(stream);

                    c.moveToNext();
                }
                c.close();
                return result;
            }
        });
    }

    @Internal
    private MeasurementStream saveOne(MeasurementStream stream, long sessionId, SQLiteDatabase writableDatabase) {
        ContentValues values = values(stream, sessionId);

        long streamId = writableDatabase.insertOrThrow(STREAM_TABLE_NAME, null, values);
        stream.setId(streamId);

        return stream;
    }

    @Internal
    private void saveMeasurements(MeasurementStream stream, long streamId, long sessionId, SQLiteDatabase writableDatabase) {
        List<Measurement> measurementsToSave = stream.getMeasurements();
        measurements.save(measurementsToSave, sessionId, streamId, writableDatabase);
        Logger.w("saved " + measurementsToSave.size() + " for session " + sessionId);
        Logger.w("saved " + String.valueOf(measurementsToSave) + " for sensor " + stream.getSensorName());
    }

    static ContentValues values(MeasurementStream stream, long sessionId) {
        ContentValues values = new ContentValues();
        values.put(STREAM_SESSION_ID, sessionId);
        values.put(STREAM_SENSOR_PACKAGE_NAME, stream.getPackageName());
        values.put(STREAM_SENSOR_NAME, stream.getSensorName());
        values.put(STREAM_MEASUREMENT_SYMBOL, stream.getSymbol());
        values.put(STREAM_MEASUREMENT_UNIT, stream.getUnit());
        values.put(STREAM_MEASUREMENT_TYPE, stream.getMeasurementType());
        values.put(STREAM_SHORT_TYPE, stream.getShortType());
        values.put(STREAM_AVG, stream.getAvg());
        values.put(STREAM_PEAK, stream.getPeak());
        values.put(STREAM_THRESHOLD_VERY_LOW, stream.getThresholdVeryLow());
        values.put(STREAM_THRESHOLD_LOW, stream.getThresholdLow());
        values.put(STREAM_THRESHOLD_MEDIUM, stream.getThresholdMedium());
        values.put(STREAM_THRESHOLD_HIGH, stream.getThresholdHigh());
        values.put(STREAM_THRESHOLD_VERY_HIGH, stream.getThresholdVeryHigh());
        values.put(STREAM_MARKED_FOR_REMOVAL, stream.isMarkedForRemoval());
        values.put(STREAM_SUBMITTED_FOR_REMOVAL, stream.isSubmittedForRemoval());
        return values;
    }

    @Internal
    void saveAll(Collection<MeasurementStream> streamsToSave, long sessionId, SQLiteDatabase writableDatabase) {
        for (MeasurementStream oneToSave : streamsToSave) {
            saveOne(oneToSave, sessionId, writableDatabase);
        }
    }

    public void deleteAllForSession(long sessionId, SQLiteDatabase writableDatabase) {
        final String whereClause = STREAM_SESSION_ID + " = '" + sessionId + "'";
        writableDatabase.delete(STREAM_TABLE_NAME, whereClause, null);
    }

    public void saveNewStreamAndMeasurements(final MeasurementStream toSave, final long sessionId, final SQLiteDatabase writableDatabase) {
        toSave.setSessionId(sessionId);
        final MeasurementStream stream = saveOne(toSave, sessionId, writableDatabase);
        saveMeasurements(stream, stream.getId(), sessionId, writableDatabase);
    }

    public void saveNewMeasurements(MeasurementStream stream, long streamId, long sessionId, SQLiteDatabase writableDatabase) {
        saveMeasurements(stream, streamId, sessionId, writableDatabase);
    }

    @Internal
    void markForRemoval(MeasurementStream stream, long sessionId, SQLiteDatabase writableDatabase) {
        try {
            ContentValues values = new ContentValues();
            values.put(STREAM_MARKED_FOR_REMOVAL, true);

            writableDatabase.update(STREAM_TABLE_NAME, values, STREAM_ID + " = " + stream.getId(), null);
        } catch (SQLException e) {
            Log.e(Constants.TAG, "Unable to mark stream [" + stream.getId() + "] from session [" + sessionId + "] to be deleted", e);
        }
    }

    @API
    public void update(final MeasurementStream stream) {
        final ContentValues values = values(stream, stream.getSessionId());
        values.put(STREAM_SESSION_ID, stream.getSessionId());

        airCastingDB.executeWritableTask(new WritableDatabaseTask<Void>() {
            @Override
            public Void execute(SQLiteDatabase writableDatabase) {
                try {
                    writableDatabase.update(STREAM_TABLE_NAME, values, STREAM_ID + " = " + stream.getId(), null);
                } catch (SQLException e) {
                    Log.e(Constants.TAG, "Error updating stream [" + stream.getId() + "]", e);
                }
                return null;
            }
        });
    }

    @Internal
    void deleteMeasurements(long streamId, SQLiteDatabase writableDatabase) {
        try {
            measurements.deleteAllFrom(streamId, writableDatabase);
        } catch (SQLException e) {
            Log.e(Constants.TAG, "Error deleting measurements from stream [" + streamId + "]", e);
        }
    }

    @Internal
    void deleteSubmitted(SQLiteDatabase writableDatabase) {
        try {
            Cursor cursor = writableDatabase.query(STREAM_TABLE_NAME, null, STREAM_IS_SUBMITTED_FOR_DELETE, null, null, null, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Long streamId = getLong(cursor, STREAM_ID);
                deleteMeasurements(streamId, writableDatabase);
                cursor.moveToNext();
            }
            cursor.close();

            writableDatabase.execSQL("DELETE FROM " + STREAM_TABLE_NAME + " WHERE " + STREAM_IS_SUBMITTED_FOR_DELETE);
        } catch (SQLException e) {
            Log.e(Constants.TAG, "Error deleting streams submitted to be deleted", e);
        }
    }

    @API
    public void markRemovedForRemovalAsSubmitted() {
        airCastingDB.executeWritableTask(new WritableDatabaseTask<Void>() {
            @Override
            public Void execute(SQLiteDatabase writableDatabase) {
                @Language("SQLite")
                String sql = "UPDATE " + STREAM_TABLE_NAME + " SET " + STREAM_SUBMITTED_FOR_REMOVAL + "=1 WHERE " + STREAM_MARKED_FOR_REMOVAL + "=1";
                writableDatabase.execSQL(sql);
                return null;
            }
        });
    }
}
