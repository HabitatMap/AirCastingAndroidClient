package pl.llp.aircasting.storage.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.gson.Gson;
import com.google.inject.Inject;
import pl.llp.aircasting.model.Regression;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.SessionManager;
import pl.llp.aircasting.storage.db.AirCastingDB;
import pl.llp.aircasting.storage.db.ReadOnlyDatabaseTask;
import pl.llp.aircasting.storage.db.WritableDatabaseTask;

import java.util.ArrayList;
import java.util.List;

import static pl.llp.aircasting.storage.db.DBConstants.*;
import static pl.llp.aircasting.storage.DBHelper.*;

/**
 * Created by marcin on 17/07/14.
 */
public class RegressionRepository {

    @Inject AirCastingDB airCastingDB;
    @Inject Gson gson;

    private Regression get(Cursor c) {
        return new Regression(getString(c, REGRESSION_SENSOR_NAME), getString(c, REGRESSION_SENSOR_PACKAGE_NAME),
                getString(c, REGRESSION_MEASUREMENT_TYPE), getString(c, REGRESSION_SHORT_TYPE),
                getString(c, REGRESSION_MEASUREMENT_SYMBOL), getString(c, REGRESSION_MEASUREMENT_UNIT),
                gson.fromJson(getString(c, REGRESSION_COEFFICIENTS), double[].class),
                getInt(c, REGRESSION_THRESHOLD_VERY_LOW), getInt(c, REGRESSION_THRESHOLD_LOW),
                getInt(c, REGRESSION_THRESHOLD_MEDIUM), getInt(c, REGRESSION_THRESHOLD_HIGH),
                getInt(c, REGRESSION_THRESHOLD_VERY_HIGH), getString(c, REGRESSION_REFERENCE_SENSOR_NAME),
                getString(c, REGRESSION_REFERENCE_SENSOR_PACKAGE_NAME), getBool(c, REGRESSION_IS_OWNER),
                getInt(c, REGRESSION_BACKEND_ID), getBool(c, REGRESSION_IS_ENABLED), getString(c, REGRESSION_CREATED_AT));
    }

    public List<Integer> disabledIds() {
        return airCastingDB.executeReadOnlyTask(new ReadOnlyDatabaseTask<List<Integer>>() {
            @Override
            public List<Integer> execute(SQLiteDatabase readOnlyDatabase) {
                List<Integer> result = new ArrayList<Integer>();
                Cursor c = readOnlyDatabase.query(REGRESSION_TABLE_NAME, null,
                        REGRESSION_IS_ENABLED + " = 0", null, null, null, null);
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    result.add(getInt(c, REGRESSION_BACKEND_ID));
                    c.moveToNext();
                }
                return result;
            }
        });
    }

    public void delete(final Regression regression) {
        airCastingDB.executeWritableTask(new WritableDatabaseTask<Void>() {
            @Override
            public Void execute(SQLiteDatabase writableDatabase) {
                writableDatabase.delete(REGRESSION_TABLE_NAME, REGRESSION_BACKEND_ID + " = ?",
                        new String[] {String.valueOf(regression.getBackendId())});
                return null;
            }
        });
    }

    public void setEnabled(final Regression regression, final boolean value) {
        airCastingDB.executeWritableTask(new WritableDatabaseTask<Object>() {
            @Override
            public Object execute(SQLiteDatabase writableDatabase) {
                regression.setEnabled(value);
                writableDatabase.update(REGRESSION_TABLE_NAME, isEnabledValues(value),
                        REGRESSION_BACKEND_ID + " = ?", new String[] {String.valueOf(regression.getBackendId())});
                return null;
            }
        });
    }

    public void enable(Regression regression) {
        setEnabled(regression, true);
    }

    public void disable(Regression regression) {
        setEnabled(regression, false);
    }

    public Regression getEnabledForSensor(final String sensorName, final String sensorPackageName) {

        return airCastingDB.executeReadOnlyTask(new ReadOnlyDatabaseTask<Regression>() {
            @Override
            public Regression execute(SQLiteDatabase readOnlyDatabase) {
                Cursor c = readOnlyDatabase.query(REGRESSION_TABLE_NAME, null,
                        REGRESSION_IS_ENABLED + " = 1 AND " + REGRESSION_SENSOR_NAME + " = ? AND " + REGRESSION_SENSOR_PACKAGE_NAME + " = ?",
                        new String[] {sensorName, sensorPackageName}, null, null, null);

                c.moveToFirst();
                if (!c.isAfterLast()) {
                    return get(c);
                }
                return null;
            }
        });
    }

    private Regression getForSensor(final String sensorName, final String sensorPackageName) {
        return airCastingDB.executeReadOnlyTask(new ReadOnlyDatabaseTask<Regression>() {
            @Override
            public Regression execute(SQLiteDatabase readOnlyDatabase) {
                Cursor c = readOnlyDatabase.query(REGRESSION_TABLE_NAME, null,
                        REGRESSION_SENSOR_NAME + " = ? AND " + REGRESSION_SENSOR_PACKAGE_NAME + " = ?",
                        new String[] {sensorName, sensorPackageName}, null, null, null);

                c.moveToFirst();
                if (!c.isAfterLast()) {
                    return get(c);
                }
                return null;
            }
        });
    }
    public List<Regression> fetchAll() {
        return airCastingDB.executeReadOnlyTask(new ReadOnlyDatabaseTask<List<Regression>>() {
            @Override
            public List<Regression> execute(SQLiteDatabase readOnlyDatabase) {
                List<Regression> result = new ArrayList<Regression>();
                Cursor c = readOnlyDatabase.query(REGRESSION_TABLE_NAME, null, null, null, null, null, null);
                c.moveToFirst();
                while (!c.isAfterLast()) {
                    result.add(get(c));
                    c.moveToNext();
                }
                return result;
            }
        });
    }

    public long save(Regression regression) {
        final ContentValues values = values(regression);
        return airCastingDB.executeWritableTask(new WritableDatabaseTask<Long>() {
            @Override
            public Long execute(SQLiteDatabase writableDatabase) {
                return writableDatabase.insertOrThrow(REGRESSION_TABLE_NAME, null, values);
            }
        });
    }

    private ContentValues isEnabledValues(boolean val) {
        ContentValues values = new ContentValues();
        values.put(REGRESSION_IS_ENABLED, val);
        return values;
    }

    private ContentValues values(Regression regression) {
        ContentValues values = new ContentValues();
        values.put(REGRESSION_SENSOR_NAME, regression.getSensorName());
        values.put(REGRESSION_SENSOR_PACKAGE_NAME, regression.getSensorPackageName());
        values.put(REGRESSION_MEASUREMENT_TYPE, regression.getMeasurementType());
        values.put(REGRESSION_MEASUREMENT_SYMBOL, regression.getMeasurementSymbol());
        values.put(REGRESSION_MEASUREMENT_UNIT, regression.getMeasurementUnit());
        values.put(REGRESSION_SHORT_TYPE, regression.getShortType());
        values.put(REGRESSION_COEFFICIENTS, gson.toJson(regression.getCoefficients()));
        values.put(REGRESSION_THRESHOLD_VERY_LOW, regression.getThresholdVeryLow());
        values.put(REGRESSION_THRESHOLD_LOW, regression.getThresholdLow());
        values.put(REGRESSION_THRESHOLD_MEDIUM, regression.getThresholdMedium());
        values.put(REGRESSION_THRESHOLD_HIGH, regression.getThresholdHigh());
        values.put(REGRESSION_THRESHOLD_VERY_HIGH, regression.getThresholdVeryHigh());
        values.put(REGRESSION_REFERENCE_SENSOR_NAME, regression.getReferenceSensorName());
        values.put(REGRESSION_REFERENCE_SENSOR_PACKAGE_NAME, regression.getReferenceSensorPackageName());
        values.put(REGRESSION_IS_OWNER, regression.isOwner());
        values.put(REGRESSION_IS_ENABLED, regression.isEnabled());
        values.put(REGRESSION_BACKEND_ID, regression.getBackendId());
        values.put(REGRESSION_CREATED_AT, regression.getCreatedAt());
        return values;
    }

    public void deleteAll() {
        airCastingDB.executeWritableTask(new WritableDatabaseTask<Object>() {
            @Override
            public Object execute(SQLiteDatabase writableDatabase) {
                writableDatabase.execSQL("DELETE FROM " + REGRESSION_TABLE_NAME);
                return null;
            }
        });
    }

    public List<Regression> forSensors(List<Sensor> sensors) {
        List<Regression> result = new ArrayList<Regression>();
        for (Sensor sensor : sensors) {
            Regression reg = getForSensor(sensor.getSensorName(), sensor.getPackageName());
            if (reg != null) {
                result.add(reg);
            }
        }
        return result;
    }
}
