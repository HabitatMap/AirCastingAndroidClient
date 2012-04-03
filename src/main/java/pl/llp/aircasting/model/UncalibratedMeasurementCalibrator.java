package pl.llp.aircasting.model;

import pl.llp.aircasting.helper.CalibrationHelper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import org.intellij.lang.annotations.Language;

import static pl.llp.aircasting.model.DBConstants.*;

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


  CalibrationHelper calibrations = new CalibrationHelper();

  SQLiteDatabase db;


  public UncalibratedMeasurementCalibrator(SQLiteDatabase db)
  {
    this.db = db;
  }

  public void saveAsCalibrated()
  {
    Cursor calibrations = db.rawQuery(FETCH_CALIBRATION, null);

    calibrations.moveToFirst();
    while(!calibrations.isAfterLast())
    {
      long sessionId = calibrations.getLong(0);
      int offset60DB = calibrations.getInt(1);
      int calibration = calibrations.getInt(2);

      calibrateRecordsInSession(sessionId, offset60DB, calibration);

      calibrations.moveToNext();
    }
    calibrations.close();
  }

  private void calibrateRecordsInSession(long sessionId, int offset60DB, int calibration)
  {
    Cursor measurement = db.query(MEASUREMENT_TABLE_NAME,
                                  new String[]{MEASUREMENT_ID, MEASUREMENT_VALUE},
                                  MEASUREMENT_SESSION_ID + " = " + sessionId,
                                  null, null, null, null);

    SQLiteStatement st = db.compileStatement(UPDATE_MEASUREMENT);

    measurement.moveToFirst();

    db.beginTransaction();

    while(!measurement.isAfterLast())
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
    db.endTransaction();
  }
}
