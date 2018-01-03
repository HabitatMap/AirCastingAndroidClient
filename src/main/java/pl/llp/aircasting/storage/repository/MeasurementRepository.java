package pl.llp.aircasting.storage.repository;

import com.google.gson.Gson;
import pl.llp.aircasting.helper.NoOp;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.storage.ProgressListener;
import pl.llp.aircasting.util.Constants;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static pl.llp.aircasting.storage.DBHelper.getDate;
import static pl.llp.aircasting.storage.DBHelper.getDouble;
import static pl.llp.aircasting.storage.DBHelper.getLong;
import static pl.llp.aircasting.storage.db.DBConstants.*;

public class MeasurementRepository {
    private ProgressListener progress = NoOp.progressListener();

    public MeasurementRepository(ProgressListener progressListener) {
        if (progressListener != null) {
            progress = progressListener;
        }
    }

    @Internal
    Map<Long, List<Measurement>> load(Session session, SQLiteDatabase readableDatabase) {
        Cursor measurements = readableDatabase.rawQuery("" +
                "SELECT * FROM " + MEASUREMENT_TABLE_NAME + " " +
                "WHERE " + MEASUREMENT_SESSION_ID + " = " + session.getId(), null);

        progress.onSizeCalculated(measurements.getCount());

        measurements.moveToFirst();
        Map<Long, List<Measurement>> results = newHashMap();
        while (!measurements.isAfterLast()) {
            Measurement measurement = new Measurement();
            measurement.setLatitude(getDouble(measurements, MEASUREMENT_LATITUDE));
            measurement.setLongitude(getDouble(measurements, MEASUREMENT_LONGITUDE));
            measurement.setValue(getDouble(measurements, MEASUREMENT_VALUE));
            measurement.setTime(getDate(measurements, MEASUREMENT_TIME));
            measurement.setMeasuredValue(getDouble(measurements, MEASUREMENT_MEASURED_VALUE));

            long id = getLong(measurements, MEASUREMENT_STREAM_ID);
            stream(id, results).add(measurement);

            int position = measurements.getPosition();
            if (position % 100 == 0) {
                progress.onProgress(position);
            }
            measurements.moveToNext();
        }
        measurements.close();

        return results;
    }

    private List<Measurement> stream(long id, Map<Long, List<Measurement>> results) {
        if (!results.containsKey(id)) {
            results.put(id, new ArrayList<Measurement>());
        }
        return results.get(id);
    }

    @Internal
    void save(List<Measurement> measurementsToSave, long sessionId, long streamId, SQLiteDatabase writableDatabase) {
        ContentValues values = new ContentValues(6);
        values.put(MEASUREMENT_SESSION_ID, sessionId);
        values.put(MEASUREMENT_STREAM_ID, streamId);

        for (Measurement measurement : measurementsToSave) {
            values.put(MEASUREMENT_LONGITUDE, measurement.getLongitude());
            values.put(MEASUREMENT_LATITUDE, measurement.getLatitude());
            values.put(MEASUREMENT_VALUE, measurement.getValue());
            values.put(MEASUREMENT_MEASURED_VALUE, measurement.getMeasuredValue());
            values.put(MEASUREMENT_TIME, measurement.getTime().getTime());

            writableDatabase.insertOrThrow(MEASUREMENT_TABLE_NAME, null, values);
        }
    }

    @Internal
    void deleteAllFrom(Long streamId, SQLiteDatabase writableDatabase) {
        try {
            writableDatabase.delete(MEASUREMENT_TABLE_NAME, MEASUREMENT_STREAM_ID + " = " + streamId, null);
        } catch (SQLException e) {
            Log.e(Constants.TAG, "Error removing measurements from stream [" + streamId + "]", e);
        }
    }
}
