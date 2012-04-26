package pl.llp.aircasting.model;

import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import org.intellij.lang.annotations.Language;

import static pl.llp.aircasting.model.DBConstants.*;

/**
 * Created by ags on 28/03/12 at 19:39
 */
public class MeasurementToStreamMigrator
{
  @Language("SQL")
  private static final String INSERT_QUERY =
      "INSERT INTO " + STREAM_TABLE_NAME + "(" +
          STREAM_SESSION_ID + ", " +
          STREAM_SENSOR_NAME + ", " +
          STREAM_SENSOR_PACKAGE_NAME + ", " +
          STREAM_MEASUREMENT_TYPE + ", " +
          STREAM_MEASUREMENT_UNIT + ", " +
          STREAM_MEASUREMENT_SYMBOL + ", " +
          STREAM_AVG + ", " +
          STREAM_PEAK + ", " +
          STREAM_THRESHOLD_VERY_LOW + ", " +
          STREAM_THRESHOLD_LOW + ", " +
          STREAM_THRESHOLD_MEDIUM + ", " +
          STREAM_THRESHOLD_HIGH + ", " +
          STREAM_THRESHOLD_VERY_HIGH +
          ") VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

  void migrate(SQLiteDatabase db)
    {
      SQLiteStatement insertStreamStmt = db
          .compileStatement("UPDATE " + MEASUREMENT_TABLE_NAME + " SET " + MEASUREMENT_STREAM_ID + " = ? " +
                                "WHERE " + STREAM_SESSION_ID + " = ?");

      Cursor oldSessions  = db.rawQuery("SELECT " + SESSION_ID + ", " + SESSION_AVG + ", " + SESSION_PEAK
                                      + SESSION_START + ", " + SESSION_END, null);

      db.beginTransaction();

      oldSessions.moveToFirst();
      while(!oldSessions.isAfterLast())
      {
        int sessionId = oldSessions.getInt(0);
        MeasurementStream s = readFrom(oldSessions);

        long streamId = save(db, s);
        insertStreamStmt.bindLong(1, streamId);
        insertStreamStmt.bindLong(2, sessionId);
        insertStreamStmt.execute();

        oldSessions.moveToNext();
      }
      oldSessions.close();

      db.setTransactionSuccessful();
      db.endTransaction();
    }

  private MeasurementStream readFrom(Cursor c)
  {
    MeasurementStream existingAudioStream = new MeasurementStream(SimpleAudioReader.getSensor());

    existingAudioStream.setSessionId(c.getInt(0));
    existingAudioStream.setAvg(c.getDouble(1));
    existingAudioStream.setPeak(c.getDouble(2));

    return existingAudioStream;
  }

  public long save(SQLiteDatabase db, MeasurementStream stream)
  {
    SQLiteStatement ps = db.compileStatement(INSERT_QUERY);

    ps.bindLong(1, stream.getSessionId());
    ps.bindString(2, SimpleAudioReader.SENSOR_NAME);
    ps.bindString(3, SimpleAudioReader.SENSOR_PACKAGE_NAME);
    ps.bindString(4, SimpleAudioReader.MEASUREMENT_TYPE);
    ps.bindString(5, SimpleAudioReader.UNIT);
    ps.bindString(6, SimpleAudioReader.SYMBOL);
    ps.bindDouble(7, stream.getAvg());
    ps.bindDouble(8, stream.getPeak());

    long key = ps.executeInsert();
    return key;
  }
}
