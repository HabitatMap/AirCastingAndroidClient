package pl.llp.aircasting.storage.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.gson.Gson;
import com.google.inject.Inject;
import pl.llp.aircasting.model.Regression;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.storage.db.AirCastingDB;
import pl.llp.aircasting.storage.db.ReadOnlyDatabaseTask;
import pl.llp.aircasting.storage.db.WritableDatabaseTask;

import static pl.llp.aircasting.storage.db.DBConstants.*;
import static pl.llp.aircasting.storage.DBHelper.*;

/**
 * Created by marcin on 17/07/14.
 */
public class RegressionRepository {

    @Inject AirCastingDB airCastingDB;
    @Inject Gson gson;

    public Regression getForSensor(final String sensorName, final String sensorPackageName) {

        return airCastingDB.executeReadOnlyTask(new ReadOnlyDatabaseTask<Regression>() {
            @Override
            public Regression execute(SQLiteDatabase readOnlyDatabase) {
                Cursor c = readOnlyDatabase.query(REGRESSION_TABLE_NAME, null,
                        REGRESSION_SENSOR_NAME + " = ? AND " + REGRESSION_SENSOR_PACKAGE_NAME + " = ?",
                        new String[] {sensorName, sensorPackageName}, null, null, null);

                c.moveToFirst();
                if (!c.isAfterLast()) {
                    return new Regression(getString(c, REGRESSION_SENSOR_NAME), getString(c, REGRESSION_SENSOR_PACKAGE_NAME),
                            getString(c, REGRESSION_MEASUREMENT_TYPE), getString(c, REGRESSION_MEASUREMENT_SYMBOL),
                            getString(c, REGRESSION_MEASUREMENT_UNIT), gson.fromJson(getString(c, REGRESSION_COEFFICIENTS), double[].class),
                            getInt(c, REGRESSION_THRESHOLD_VERY_LOW), getInt(c, REGRESSION_THRESHOLD_LOW),
                            getInt(c, REGRESSION_THRESHOLD_MEDIUM), getInt(c, REGRESSION_THRESHOLD_HIGH),
                            getInt(c, REGRESSION_THRESHOLD_VERY_HIGH));
                }
                return null;
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

    private ContentValues values(Regression regression) {
        ContentValues values = new ContentValues();
        values.put(REGRESSION_SENSOR_NAME, regression.getSensorName());
        values.put(REGRESSION_SENSOR_PACKAGE_NAME, regression.getSensorPackageName());
        values.put(REGRESSION_MEASUREMENT_TYPE, regression.getMeasurementType());
        values.put(REGRESSION_MEASUREMENT_SYMBOL, regression.getMeasurementSymbol());
        values.put(REGRESSION_MEASUREMENT_UNIT, regression.getMeasurementUnit());
        values.put(REGRESSION_COEFFICIENTS, gson.toJson(regression.getCoefficients()));
        values.put(REGRESSION_THRESHOLD_VERY_LOW, regression.getThresholdVeryLow());
        values.put(REGRESSION_THRESHOLD_LOW, regression.getThresholdLow());
        values.put(REGRESSION_THRESHOLD_MEDIUM, regression.getThresholdMedium());
        values.put(REGRESSION_THRESHOLD_HIGH, regression.getThresholdHigh());
        values.put(REGRESSION_THRESHOLD_VERY_HIGH, regression.getThresholdVeryHigh());
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

}
