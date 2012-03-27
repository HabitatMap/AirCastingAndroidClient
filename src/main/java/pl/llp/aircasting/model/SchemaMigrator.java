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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

public class SchemaMigrator extends SQLiteOpenHelper implements DBConstants
{
  public SchemaMigrator(Context context) {
    super(context, DB_NAME, null, DB_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase sqLiteDatabase) {
    sqLiteDatabase.execSQL("create table " + SESSION_TABLE_NAME + " (" +
                               SESSION_ID + " integer primary key" +
                               ", " + SESSION_TITLE + " text" +
                               ", " + SESSION_DESCRIPTION + " text" +
                               ", " + SESSION_TAGS + " text" +
                               ", " + SESSION_AVG + " real" +
                               ", " + SESSION_PEAK + " real" +
                               ", " + SESSION_START + " integer" +
                               ", " + SESSION_END + " integer" +
                               ", " + SESSION_UUID + " text" +
                               ", " + SESSION_LOCATION + " text" +
                               ", " + SESSION_CALIBRATION + " integer" +
                               ", " + SESSION_CONTRIBUTE + " boolean" +
                               ", " + SESSION_OS_VERSION + " text" +
                               ", " + SESSION_PHONE_MODEL + " text" +
                               ", " + SESSION_DATA_TYPE + " text" +
                               ", " + SESSION_INSTRUMENT + " text" +
                               ", " + SESSION_OFFSET_60_DB + " integer" +
                               ", " + SESSION_MARKED_FOR_REMOVAL + " boolean" +
                               ", " + SESSION_SUBMITTED_FOR_REMOVAL + " boolean" +
                               ")"
                          );
    createMeasurementsTable(sqLiteDatabase);

    createSensorTable(sqLiteDatabase);

    createNotesTable(sqLiteDatabase);
  }

  private void createMeasurementsTable(SQLiteDatabase sqLiteDatabase)
  {
    sqLiteDatabase.execSQL("create table " + MEASUREMENT_TABLE_NAME + " (" +
                               MEASUREMENT_SESSION_ID + " integer" +
                               ", " + MEASUREMENT_ID + " integer primary key" +
                               ", " + MEASUREMENT_LATITUDE + " real" +
                               ", " + MEASUREMENT_LONGITUDE + " real" +
                               ", " + MEASUREMENT_VALUE + " real" +
                               ", " + MEASUREMENT_TIME + " integer" +
                               ", " + MEASUREMENT_SENSOR_ID + " text" +
                               ")"
                          );
  }

  private void createNotesTable(SQLiteDatabase sqLiteDatabase)
  {
    sqLiteDatabase.execSQL("create table " + NOTE_TABLE_NAME + "(" +
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

  private void createSensorTable(SQLiteDatabase db)
  {
    db.execSQL("CREATE TABLE " + SENSOR_TABLE_NAME + " ( " +
                   SENSOR_ID + " integer, " +
                   SENSOR_NAME + " text, " +
                   SENSOR_MEASUREMENT_TYPE + " text, " +
                   SENSOR_MEASUREMENT_UNIT + " text, " +
                   SENSOR_MEASUREMENT_SYMBOL + " text " +
                   ")"
              );
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    if (oldVersion < 19 && newVersion >= 19) {
      db.execSQL("alter table " + NOTE_TABLE_NAME + " " +
                     "add column " + NOTE_PHOTO + " text"
                );
    }
    if (oldVersion < 20 && newVersion >= 20)
    {
      db.execSQL("alter table " + NOTE_TABLE_NAME + " " +
                     "add column " + NOTE_NUMBER + " integer"
                );
    }
    if (oldVersion < 21 && newVersion >= 21)
    {
      db.execSQL("alter table " + SESSION_TABLE_NAME + " " +
                     "add column " + SESSION_SUBMITTED_FOR_REMOVAL + " boolean"
                );
    }

    if (oldVersion < 22 && newVersion >= 22)
    {
      db.execSQL("ALTER TABLE " + MEASUREMENT_TABLE_NAME + " " +
                     "ADD COLUMN " + MEASUREMENT_SENSOR_ID + " text");
          
      createSensorTable(db);
      initializeSensors(db);
    }
  }

  private void initializeSensors(SQLiteDatabase db)
  {
    SQLiteStatement ps = db.compileStatement("INSERT INTO SENSORS VALUES(?, ?, ?, ?, ?, ?) ");
    ps.bindLong(1, 1L);
    ps.bindString(2, SimpleAudioReader.SENSOR_NAME);
    ps.bindString(3, SimpleAudioReader.UNIT);
    ps.bindString(4, SimpleAudioReader.MEASUREMENT_TYPE);
    ps.bindString(5, SimpleAudioReader.SYMBOL);

    ps.executeInsert();

    db.execSQL("UPDATE " + MEASUREMENT_TABLE_NAME + " SET " + MEASUREMENT_SENSOR_ID + " 1");
  }
}
