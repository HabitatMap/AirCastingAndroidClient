/**
 AirCasting - Share your Air!
 Copyright (C) 2011-2012 HabitatMap, Inc.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 You can contact the authors by email at <info@habitatmap.org>
 */
package pl.llp.aircasting.model;

import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;

import android.database.sqlite.SQLiteDatabase;
import org.intellij.lang.annotations.Language;

import static pl.llp.aircasting.model.DBConstants.*;

public class SchemaMigrator
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
          STREAM_THRESHOLD_VERY_HIGH + " INTEGER " +
          ")";

  MeasurementToStreamMigrator measurementsToStreams = new MeasurementToStreamMigrator();

  public void create(SQLiteDatabase db)
  {
    createSessionsTable(db);
    createMeasurementsTable(db);
    createStreamTable(db);
    createNotesTable(db);
  }

  private void createSessionsTable(SQLiteDatabase db) {
    db.execSQL("create table " + SESSION_TABLE_NAME + " (" +
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
                   ")"
              );
  }

  private void createMeasurementsTable(SQLiteDatabase db) {
    db.execSQL("create table " + MEASUREMENT_TABLE_NAME +
                   " (" + MEASUREMENT_ID + " integer primary key" +
                   ", " + MEASUREMENT_LATITUDE + " real" +
                   ", " + MEASUREMENT_LONGITUDE + " real" +
                   ", " + MEASUREMENT_VALUE + " real" +
                   ", " + MEASUREMENT_TIME + " integer" +
                   ", " + MEASUREMENT_STREAM_ID + " integer" +
                   ", " + MEASUREMENT_SESSION_ID + " integer" +
                   ")"
              );
  }

  private void createNotesTable(SQLiteDatabase db) {
    db.execSQL("create table " + NOTE_TABLE_NAME + "(" +
                   NOTE_SESSION_ID + " integer " +
                   ", " + NOTE_LATITUDE + " real " +
                   ", " + NOTE_LONGITUDE + " real " +
                   ", " + NOTE_TEXT + " text " +
                   ", " + NOTE_DATE + " integer " +
                   ", " + NOTE_PHOTO + " text" +
                   ", " + NOTE_NUMBER + " integer" +
                   ")"
              );
  }

  private void createStreamTable(SQLiteDatabase db) {
    db.execSQL(CREATE_STREAMS_TABLE);
  }

  public void migrate(SQLiteDatabase db, int oldVersion, int newVersion) {
    if (oldVersion < 19 && newVersion >= 19) {
      addColumn(db, NOTE_TABLE_NAME, NOTE_PHOTO, "text");
    }

    if (oldVersion < 20 && newVersion >= 20) {
      addColumn(db, NOTE_TABLE_NAME, NOTE_NUMBER, "integer");
    }

    if (oldVersion < 21 && newVersion >= 21) {
      addColumn(db, SESSION_TABLE_NAME, SESSION_SUBMITTED_FOR_REMOVAL, "boolean");
    }

    if (oldVersion < 22 && newVersion >= 22) {
      addColumn(db, MEASUREMENT_TABLE_NAME, MEASUREMENT_STREAM_ID, "integer");

      createStreamTable(db);
      measurementsToStreams.migrate(db);

      dropColumn(db, SESSION_TABLE_NAME, SESSION_PEAK);
      dropColumn(db, SESSION_TABLE_NAME, SESSION_AVG);
    }

    if (oldVersion < 25 && newVersion >= 25) {
      addColumn(db, STREAM_TABLE_NAME, STREAM_SHORT_TYPE, "text");
      db.execSQL("UPDATE " + STREAM_TABLE_NAME + " SET " + STREAM_SHORT_TYPE +
                     " = '" + SimpleAudioReader.SHORT_TYPE + "'");
    }

    if( oldVersion < 26 && newVersion >= 26)
    {
      addColumn(db, SESSION_TABLE_NAME, SESSION_CALIBRATED, "boolean");
    }

    if(oldVersion < 27 && newVersion >= 27)
    {
      addColumn(db, STREAM_TABLE_NAME, STREAM_SENSOR_PACKAGE_NAME, "text");
    }
  }

  private void dropColumn(SQLiteDatabase db, String tableName, String column) {
    StringBuilder q = new StringBuilder(50);

    q.append("ALTER TABLE ").append(tableName);
    q.append(" DROP COLUMN ").append(column);

    db.execSQL(q.toString());
  }

  private void addColumn(SQLiteDatabase db, String tableName, String columnName, String datatype) {
    StringBuilder q = new StringBuilder(50);

    q.append("ALTER TABLE ");
    q.append(tableName);
    q.append(" ADD COLUMN ").append(columnName);
    q.append(" ").append(datatype);

    db.execSQL(q.toString());
  }
}
