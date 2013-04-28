package pl.llp.aircasting.storage.repository;

import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.storage.db.AirCastingDB;
import pl.llp.aircasting.storage.db.DBConstants;
import pl.llp.aircasting.storage.db.ReadOnlyDatabaseTask;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.inject.Inject;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static pl.llp.aircasting.storage.DBHelper.getInt;
import static pl.llp.aircasting.storage.DBHelper.getString;

public class SensorRepository implements DBConstants
{
  public static final String[] SENSOR_FIELDS = new String[]
      {
          STREAM_SENSOR_NAME, STREAM_SENSOR_PACKAGE_NAME, STREAM_MEASUREMENT_TYPE, STREAM_SHORT_TYPE,
          STREAM_MEASUREMENT_UNIT, STREAM_MEASUREMENT_SYMBOL, STREAM_THRESHOLD_VERY_LOW, STREAM_THRESHOLD_LOW,
          STREAM_THRESHOLD_MEDIUM, STREAM_THRESHOLD_HIGH, STREAM_THRESHOLD_VERY_HIGH
      };

  @Inject AirCastingDB airCastingDB;

  @API
  public List<Sensor> getAll() {

    return airCastingDB.executeReadOnlyTask(readSensorsTask());
  }

  private ReadOnlyDatabaseTask<List<Sensor>> readSensorsTask()
  {
    return new ReadOnlyDatabaseTask<List<Sensor>>()
    {
      @Override
      public List<Sensor> execute(SQLiteDatabase readOnlyDatabase)
      {
        List<Sensor> result = newArrayList();

        boolean distinct = true;
        Cursor cursor = readOnlyDatabase.query(distinct, STREAM_TABLE_NAME, SENSOR_FIELDS, null, null, null, null, null, null);

        if(cursor.moveToFirst())
        {
          while (!cursor.isAfterLast()) {
            String name = getString(cursor, STREAM_SENSOR_NAME);
            String packageName = getString(cursor, STREAM_SENSOR_PACKAGE_NAME);
            String measurementType = getString(cursor, STREAM_MEASUREMENT_TYPE);
            String shortType = getString(cursor, STREAM_SHORT_TYPE);
            String unit = getString(cursor, STREAM_MEASUREMENT_UNIT);
            String symbol = getString(cursor, STREAM_MEASUREMENT_SYMBOL);
            int veryLow = getInt(cursor, STREAM_THRESHOLD_VERY_LOW);
            int low = getInt(cursor, STREAM_THRESHOLD_LOW);
            int mid = getInt(cursor, STREAM_THRESHOLD_MEDIUM);
            int high = getInt(cursor, STREAM_THRESHOLD_HIGH);
            int veryHigh = getInt(cursor, STREAM_THRESHOLD_VERY_HIGH);

            Sensor sensor = new Sensor(packageName, name, measurementType, shortType, unit, symbol, veryLow, low, mid, high, veryHigh);
            result.add(sensor);

            cursor.moveToNext();
          }
        }
        cursor.close();

        return result;
      }
    };
  }
}
