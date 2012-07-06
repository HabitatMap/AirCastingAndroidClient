package pl.llp.aircasting.repository.db;

import pl.llp.aircasting.helper.CalibrationHelper;
import pl.llp.aircasting.repository.ProgressListener;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import com.google.inject.Inject;
import org.intellij.lang.annotations.Language;

import static pl.llp.aircasting.repository.db.DBConstants.*;

public class UncalibratedMeasurementCalibrator
{
  @Language("SQL")
  public static final String FETCH_CALIBRATION = "SELECT "
      + SESSION_ID + ", "
      + SESSION_OFFSET_60_DB + ", "
      + SESSION_CALIBRATION
      + " FROM " + SESSION_TABLE_NAME;

  @Language("SQL")
  public static final String UPDATE_MEASUREMENT = "UPDATE " +
      MEASUREMENT_TABLE_NAME + " SET " +
      MEASUREMENT_VALUE + " = ? WHERE " +
      MEASUREMENT_ID + " = ?";

  @Inject AirCastingDB airCastingDB;

  @Inject CalibrationHelper calibrations;

  public void calibrate(final ProgressListener listener)
  {
    int workSize = sessionsToCalibrate();
    listener.onSizeCalculated(workSize);

    airCastingDB.executeWritableTask(new WritableDatabaseTask<Object>()
    {
      @Override
      public Object execute(SQLiteDatabase writableDatabase)
      {
        int step = 0;

        Cursor calibrations = writableDatabase.rawQuery(FETCH_CALIBRATION, null);

        calibrations.moveToFirst();

        while(!calibrations.isAfterLast())
        {
          long sessionId = calibrations.getLong(0);
          int offset60DB = calibrations.getInt(1);
          int calibration = calibrations.getInt(2);

          calibrateMeasurementsInSession(writableDatabase, sessionId, offset60DB, calibration);
          markSessionAsCalibrated(writableDatabase, sessionId);

          calibrations.moveToNext();
          listener.onProgress(step++);
        }
        calibrations.close();
        return null;
      }
    });
  }

  private void markSessionAsCalibrated(SQLiteDatabase db, Long sessionId)
  {
    @Language("SQL")
    String query = "UPDATE " + SESSION_TABLE_NAME + " SET " + SESSION_CALIBRATED + " = true " +
        " WHERE " + SESSION_ID + " = " + sessionId;
    db.execSQL(query);
  }

  private void calibrateMeasurementsInSession(SQLiteDatabase db, long sessionId, int offset60DB, int calibration)
  {
    @Language("SQL")
    String q = "" +
        "SELECT " + STREAM_ID + " " +
        "FROM " + STREAM_TABLE_NAME + " " +
        "WHERE " + STREAM_SENSOR_NAME + " = '" + SimpleAudioReader.SENSOR_NAME + "' " +
        "AND " + STREAM_SESSION_ID + " = " + sessionId;
    Cursor c = db.rawQuery(q, null);
    if(c.getCount() < 1)
    {
      c.close();
      return;
    }
    c.moveToFirst();
    long streamId = c.getLong(0);
    c.close();

    Cursor measurement = db.query(MEASUREMENT_TABLE_NAME,
                                  new String[]{MEASUREMENT_ID, MEASUREMENT_VALUE},
                                  MEASUREMENT_STREAM_ID + " = " + streamId,
                                  null, null, null, null);

    SQLiteStatement st = db.compileStatement(UPDATE_MEASUREMENT);

    measurement.moveToFirst();

    try
    {
      db.beginTransaction();

      while (!measurement.isAfterLast())
      {
        long id = measurement.getLong(0);
        double value = measurement.getDouble(1);

        double calibrated = calibrations.calibrate(value, calibration, offset60DB);

        st.bindDouble(1, calibrated);
        st.bindLong(2, id);
        st.execute();

        measurement.moveToNext();
      }
      measurement.close();
      st.close();

      db.setTransactionSuccessful();
    }
    finally
    {
      db.endTransaction();
    }
  }

  public int sessionsToCalibrate()
  {
    return airCastingDB.executeReadOnlyTask(new ReadOnlyDatabaseTask<Integer>()
    {
      @Override
      public Integer execute(SQLiteDatabase readOnlyDatabase)
      {
        Cursor c = readOnlyDatabase.rawQuery("SELECT COUNT(*) FROM " + SESSION_TABLE_NAME + " WHERE "
                                                 + SESSION_CALIBRATED + " = 0", null);
        c.moveToFirst();
        if(c.getCount() < 1)
        {
          return 0;
        }

        return c.getInt(0);
      }
    });
  }
}
