package pl.llp.aircasting.tracking;

import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.storage.DatabaseTaskQueue;
import pl.llp.aircasting.storage.db.WritableDatabaseTask;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import static pl.llp.aircasting.storage.db.DBConstants.*;

/**
 * Created by ags on 27/03/2013 at 00:42
 */
class ActualMeasurementTracker implements MeasurementTracker
{
  final DatabaseTaskQueue dbQueue;

  ActualMeasurementTracker(DatabaseTaskQueue taskQueue)
  {
    this.dbQueue = taskQueue;
  }

  @Override
  public void add(final MeasurementStream stream, final Measurement measurement)
  {
    stream.add(measurement);
    dbQueue.add(new WritableDatabaseTask<Void>()
    {
      @Override
      public Void execute(SQLiteDatabase writableDatabase)
      {
        ContentValues values = new ContentValues(7);
        values.put(MEASUREMENT_SESSION_ID, stream.getSessionId());
        values.put(MEASUREMENT_STREAM_ID, stream.getId());
        values.put(MEASUREMENT_LONGITUDE, measurement.getLongitude());
        values.put(MEASUREMENT_LATITUDE, measurement.getLatitude());
        values.put(MEASUREMENT_VALUE, measurement.getValue());
        values.put(MEASUREMENT_MEASURED_VALUE, measurement.getMeasuredValue());
        values.put(MEASUREMENT_TIME, measurement.getTime().getTime());

        writableDatabase.insertOrThrow(MEASUREMENT_TABLE_NAME, null, values);

        return null;
      }
    });
  }
}
