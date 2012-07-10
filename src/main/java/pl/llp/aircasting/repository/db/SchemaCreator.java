package pl.llp.aircasting.repository.db;

import android.database.sqlite.SQLiteDatabase;
import org.intellij.lang.annotations.Language;

import static pl.llp.aircasting.repository.db.DBConstants.*;
import static pl.llp.aircasting.repository.db.DBConstants.NOTE_DATE;
import static pl.llp.aircasting.repository.db.DBConstants.NOTE_NUMBER;
import static pl.llp.aircasting.repository.db.DBConstants.NOTE_PHOTO;
import static pl.llp.aircasting.repository.db.DBConstants.NOTE_TEXT;

public class SchemaCreator
{
  @Language("SQL")
  public static final String CREATE_STREAMS_TABLE =
      "CREATE TABLE streams (\n  " +
          STREAM_ID + " INTEGER PRIMARY KEY,\n  " +
          STREAM_SESSION_ID + " INTEGER, \n" +
          STREAM_AVG + " REAL,\n  " +
          STREAM_PEAK + " REAL,\n  " +
          STREAM_SENSOR_NAME + " TEXT, \n  " +
          STREAM_SENSOR_PACKAGE_NAME + " TEXT, \n  " +
          STREAM_MEASUREMENT_UNIT + " TEXT, \n  " +
          STREAM_MEASUREMENT_TYPE + " TEXT, \n  " +
          STREAM_SHORT_TYPE + " TEXT, \n  " +
          STREAM_MEASUREMENT_SYMBOL + " TEXT,\n " +
          STREAM_THRESHOLD_VERY_LOW + " INTEGER, \n " +
          STREAM_THRESHOLD_LOW + " INTEGER, \n " +
          STREAM_THRESHOLD_MEDIUM + " INTEGER, \n " +
          STREAM_THRESHOLD_HIGH + " INTEGER, \n " +
          STREAM_THRESHOLD_VERY_HIGH + " INTEGER,\n " +
          STREAM_MARKED_FOR_REMOVAL + " BOOLEAN, \n " +
          STREAM_SUBMITTED_FOR_REMOVAL + " BOOLEAN " +
          ")";

  @Language("SQL")
  private static final String CREATE_SESSION_TABLE = "create table " + SESSION_TABLE_NAME + " (" +
      SESSION_ID + " integer primary key" +
      ", " + SESSION_TITLE + " text" +
      ", " + SESSION_DESCRIPTION + " text" +
      ", " + SESSION_TAGS + " text" +
      ", " + SESSION_START + " integer" +
      ", " + SESSION_END + " integer" +
      ", " + SESSION_UUID + " text" +
      ", " + SESSION_LOCATION + " text" +
      ", " + SESSION_CALIBRATION + " integer" +
      ", " + SESSION_CONTRIBUTE + " boolean" +
      ", " + SESSION_PHONE_MODEL + " text" +
      ", " + SESSION_INSTRUMENT + " text" +
      ", " + SESSION_DATA_TYPE + " text" +
      ", " + SESSION_OS_VERSION + " text" +
      ", " + SESSION_OFFSET_60_DB + " integer" +
      ", " + SESSION_MARKED_FOR_REMOVAL + " boolean" +
      ", " + SESSION_SUBMITTED_FOR_REMOVAL + " boolean" +
      ", " + SESSION_CALIBRATED + " boolean" +
      ")";

  @Language("SQL")
  private static final String CREATE_MEASUREMENT_TABLE = "create table " + MEASUREMENT_TABLE_NAME +
      " (" + MEASUREMENT_ID + " integer primary key" +
      ", " + MEASUREMENT_LATITUDE + " real" +
      ", " + MEASUREMENT_LONGITUDE + " real" +
      ", " + MEASUREMENT_VALUE + " real" +
      ", " + MEASUREMENT_TIME + " integer" +
      ", " + MEASUREMENT_STREAM_ID + " integer" +
      ", " + MEASUREMENT_SESSION_ID + " integer" +
      ")";

  @Language("SQL")
  private static final String CREATE_NOTE_TABLE = "create table " + NOTE_TABLE_NAME + "(" +
      NOTE_SESSION_ID + " integer " +
      ", " + NOTE_LATITUDE + " real " +
      ", " + NOTE_LONGITUDE + " real " +
      ", " + NOTE_TEXT + " text " +
      ", " + NOTE_DATE + " integer " +
      ", " + NOTE_PHOTO + " text" +
      ", " + NOTE_NUMBER + " integer" +
      ")";

  public void create(SQLiteDatabase db)
  {
    db.execSQL(CREATE_SESSION_TABLE);
    db.execSQL(CREATE_MEASUREMENT_TABLE);
    db.execSQL(CREATE_STREAMS_TABLE);
    db.execSQL(CREATE_NOTE_TABLE);
  }
}
