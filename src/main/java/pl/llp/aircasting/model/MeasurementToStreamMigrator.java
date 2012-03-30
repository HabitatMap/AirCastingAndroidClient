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
          STREAM_MEASUREMENT_TYPE + ", " +
          STREAM_MEASUREMENT_UNIT + ", " +
          STREAM_MEASUREMENT_SYMBOL + ", " +
          STREAM_AVG + ", " +
          STREAM_PEAK +
          ") VALUES(?, ?, ?, ?, ?, ?, ?)";

  void migrate(SQLiteDatabase db)
    {
      SQLiteStatement st = db
          .compileStatement("UPDATE " + MEASUREMENT_TABLE_NAME + " SET " + MEASUREMENT_STREAM_ID + " = ? " +
                                "WHERE " + STREAM_SESSION_ID + " = ?");

      Cursor c  = db.rawQuery("SELECT " + SESSION_ID + ", " + SESSION_AVG + ", " + SESSION_PEAK
                                      + SESSION_START + ", " + SESSION_END, null);

      db.beginTransaction();

      c.moveToFirst();
      while(!c.isAfterLast())
      {
        int sessionId = c.getInt(0);
        MeasurementStream s = readFrom(c);
        long streamId = save(db, s);
        st.bindLong(1, streamId);
        st.bindLong(2, sessionId);
        st.execute();

        c.moveToNext();
      }

      db.setTransactionSuccessful();
      db.endTransaction();
    }

  private MeasurementStream readFrom(Cursor c)
  {
    MeasurementStream s = new MeasurementStream(SimpleAudioReader.SENSOR_NAME,
                                                                SimpleAudioReader.MEASUREMENT_TYPE,
                                                                SimpleAudioReader.UNIT,
                                                                SimpleAudioReader.SYMBOL);
    s.setSessionId(c.getInt(0));
    s.setAvg(c.getDouble(1));
    s.setPeak(c.getDouble(2));

    return s;
  }

  public long save(SQLiteDatabase db, MeasurementStream stream)
  {
    SQLiteStatement ps = db.compileStatement(INSERT_QUERY);

    ps.bindLong(1, stream.getSessionId());
    ps.bindString(2, SimpleAudioReader.SENSOR_NAME);
    ps.bindString(3, SimpleAudioReader.MEASUREMENT_TYPE);
    ps.bindString(4, SimpleAudioReader.UNIT);
    ps.bindString(5, SimpleAudioReader.SYMBOL);
    ps.bindDouble(6, stream.getAvg());
    ps.bindDouble(7, stream.getPeak());

    long key = ps.executeInsert();
    return key;
  }
}
