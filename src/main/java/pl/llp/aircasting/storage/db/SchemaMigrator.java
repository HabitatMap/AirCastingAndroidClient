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
package pl.llp.aircasting.storage.db;

import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;

import android.database.sqlite.SQLiteDatabase;

import static pl.llp.aircasting.storage.db.DBConstants.*;

public class SchemaMigrator
{
  MeasurementToStreamMigrator measurementsToStreams = new MeasurementToStreamMigrator();

  private void createStreamTable(SQLiteDatabase db, int revision) {
    String query = new SchemaCreator().streamTable().asSQL(revision);
    db.execSQL(query);
  }

  private void createRegressionTable(SQLiteDatabase db, int revision) {
      db.execSQL(new SchemaCreator().regressionTable().asSQL(revision));
  }

  public void migrate(SQLiteDatabase db, int oldVersion, int newVersion) { if (oldVersion < 19 && newVersion >= 19) {
      addColumn(db, NOTE_TABLE_NAME, NOTE_PHOTO, Datatype.TEXT);
    }

    if (oldVersion < 20 && newVersion >= 20) {
      addColumn(db, NOTE_TABLE_NAME, NOTE_NUMBER, Datatype.INTEGER);
    }

    if (oldVersion < 21 && newVersion >= 21) {
      addColumn(db, SESSION_TABLE_NAME, SESSION_SUBMITTED_FOR_REMOVAL, Datatype.BOOLEAN);
    }

    if (oldVersion < 22 && newVersion >= 22) {
      addColumn(db, MEASUREMENT_TABLE_NAME, MEASUREMENT_STREAM_ID, Datatype.INTEGER);

      createStreamTable(db, 22);
      measurementsToStreams.migrate(db);
    }

    if (oldVersion < 25 && newVersion >= 25) {
      addColumn(db, STREAM_TABLE_NAME, STREAM_SHORT_TYPE, Datatype.TEXT);
      db.execSQL("UPDATE " + STREAM_TABLE_NAME + " SET " + STREAM_SHORT_TYPE +
                     " = '" + SimpleAudioReader.SHORT_TYPE + "'");
    }

    if( oldVersion < 26 && newVersion >= 26)
    {
      addColumn(db, SESSION_TABLE_NAME, SESSION_CALIBRATED, Datatype.BOOLEAN);
    }

    if(oldVersion < 27 && newVersion >= 27)
    {
      addColumn(db, STREAM_TABLE_NAME, STREAM_SENSOR_PACKAGE_NAME, Datatype.TEXT);
      db.execSQL("UPDATE " + STREAM_TABLE_NAME + " SET " + STREAM_SENSOR_PACKAGE_NAME + " = 'builtin' " );
    }

    if(oldVersion < 28 && newVersion >= 28)
    {
      addColumn(db, STREAM_TABLE_NAME, STREAM_MARKED_FOR_REMOVAL, Datatype.BOOLEAN);
      addColumn(db, STREAM_TABLE_NAME, STREAM_SUBMITTED_FOR_REMOVAL, Datatype.BOOLEAN);
    }
    if(oldVersion < 29 && newVersion >= 29)
    {
      addColumn(db, SESSION_TABLE_NAME, SESSION_LOCAL_ONLY, Datatype.BOOLEAN);
    }

    if(oldVersion < 30 && newVersion >= 30)
    {
      addColumn(db, SESSION_TABLE_NAME, SESSION_INCOMPLETE, Datatype.BOOLEAN);
    }

    if (oldVersion < 31 && newVersion >= 31)
    {
      createRegressionTable(db, 31);
    }

//    sometime in the future
//    {
//      dropColumn(db, SESSION_TABLE_NAME, DEPRECATED_SESSION_PEAK);
//      dropColumn(db, SESSION_TABLE_NAME, DEPRECATED_SESSION_AVG);
//      dropColumn(db, SESSION_TABLE_NAME, DEPRECATED_SESSION_PEAK);
//      dropColumn(db, SESSION_TABLE_NAME, DEPRECATED_SESSION_AVG);
//    }
  }

  private void addColumn(SQLiteDatabase db, String tableName, String columnName, Datatype datatype) {
    StringBuilder q = new StringBuilder(50);

    q.append("ALTER TABLE ");
    q.append(tableName);
    q.append(" ADD COLUMN ").append(columnName);
    q.append(" ").append(datatype.getTypeName());

    db.execSQL(q.toString());
  }
}
