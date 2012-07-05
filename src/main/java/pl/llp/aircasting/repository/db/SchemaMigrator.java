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
package pl.llp.aircasting.repository.db;

import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;

import android.database.sqlite.SQLiteDatabase;

import static pl.llp.aircasting.repository.db.DBConstants.*;

public class SchemaMigrator
{
  MeasurementToStreamMigrator measurementsToStreams = new MeasurementToStreamMigrator();

  private void createStreamTable(SQLiteDatabase db) {
    db.execSQL(SchemaCreator.CREATE_STREAMS_TABLE);
  }

  public void migrate(SQLiteDatabase db, int oldVersion, int newVersion) { if (oldVersion < 19 && newVersion >= 19) {
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
      db.execSQL("UPDATE " + STREAM_TABLE_NAME + " SET " + STREAM_SENSOR_PACKAGE_NAME + " = 'builtin' " );
    }

    if(oldVersion < 28 && newVersion >= 28)
    {
      addColumn(db, STREAM_TABLE_NAME, STREAM_MARKED_FOR_REMOVAL, "boolean");
      addColumn(db, STREAM_TABLE_NAME, STREAM_SUBMITTED_FOR_REMOVAL, "boolean");
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
