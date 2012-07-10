package pl.llp.aircasting.repository.db;

import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import org.intellij.lang.annotations.Language;

import java.util.Date;

import static pl.llp.aircasting.repository.DBHelper.getDate;
import static pl.llp.aircasting.repository.db.DBConstants.*;

public class MeasurementToStreamMigrator
{
  @Language("SQL")
  private static final String INSERT_QUERY =
      "INSERT INTO " + STREAM_TABLE_NAME + "(" +
          STREAM_SESSION_ID + ", " +
          STREAM_SENSOR_NAME + ", " +
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
          ") VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

  void migrate(SQLiteDatabase db)
    {
      @Language("SQL")
      String update = "UPDATE " + MEASUREMENT_TABLE_NAME + " " +
          "SET " + MEASUREMENT_STREAM_ID + " = ? " +
          "WHERE " + MEASUREMENT_SESSION_ID + " = ?";

      SQLiteStatement insertStreamStmt = db.compileStatement(update);

      @Language("SQL")
      String query = "SELECT "
          + SESSION_ID + ", "
          + DEPRECATED_SESSION_AVG + ", "
          + DEPRECATED_SESSION_PEAK + ", "
          + SESSION_START + ", " + SESSION_END
          + " FROM " + SESSION_TABLE_NAME;
      Cursor oldSessions  = db.rawQuery(query, null);

      try
      {
        db.beginTransaction();

        oldSessions.moveToFirst();
        while (!oldSessions.isAfterLast())
        {
          int sessionId = oldSessions.getInt(0);
          Date start = getDate(oldSessions, SESSION_START);
          Date end = getDate(oldSessions, SESSION_END);
          MeasurementStream s = readFrom(oldSessions);

          long streamId = save(db, s, start, end);
          insertStreamStmt.bindLong(1, streamId);
          insertStreamStmt.bindLong(2, sessionId);
          insertStreamStmt.execute();

          oldSessions.moveToNext();
        }
        oldSessions.close();

        db.setTransactionSuccessful();
      }
      finally
      {
        db.endTransaction();
      }
    }

  private MeasurementStream readFrom(Cursor c)
  {
    MeasurementStream existingAudioStream = new MeasurementStream(SimpleAudioReader.getSensor());

    existingAudioStream.setSessionId(c.getInt(0));
    existingAudioStream.setAvg(c.getDouble(1));
    existingAudioStream.setPeak(c.getDouble(2));

    return existingAudioStream;
  }

  long save(SQLiteDatabase db, MeasurementStream stream, Date start, Date end)
  {
    SQLiteStatement ps = db.compileStatement(INSERT_QUERY);

    ps.bindLong(1, stream.getSessionId());
    ps.bindString(2, SimpleAudioReader.SENSOR_NAME);
    ps.bindString(3, SimpleAudioReader.MEASUREMENT_TYPE);
    ps.bindString(4, SimpleAudioReader.UNIT);
    ps.bindString(5, SimpleAudioReader.SYMBOL);
    ps.bindDouble(6, stream.getAvg());
    ps.bindDouble(7, stream.getPeak());
    ps.bindDouble(8, SimpleAudioReader.VERY_LOW);
    ps.bindDouble(9, SimpleAudioReader.LOW);
    ps.bindDouble(10, SimpleAudioReader.MID);
    ps.bindDouble(11, SimpleAudioReader.HIGH);
    ps.bindDouble(12, SimpleAudioReader.VERY_HIGH);


    long key = ps.executeInsert();
    return key;
  }
}
