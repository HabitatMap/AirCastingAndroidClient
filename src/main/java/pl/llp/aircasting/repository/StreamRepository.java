package pl.llp.aircasting.repository;

import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Session;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static pl.llp.aircasting.model.DBConstants.*;
import static pl.llp.aircasting.repository.DBHelper.getDouble;
import static pl.llp.aircasting.repository.DBHelper.getInt;
import static pl.llp.aircasting.repository.DBHelper.getString;

/**
 * Created by ags on 30/03/12 at 12:18
 */
class StreamRepository
{
  private SQLiteDatabase db;

  public StreamRepository(SQLiteDatabase db)
  {
    this.db = db;
  }

  List<MeasurementStream> load(Session session)
  {
    List<MeasurementStream> result = newArrayList();

    Cursor c = db.rawQuery("" +
                        "SELECT * FROM " + STREAM_TABLE_NAME + " " +
                        "WHERE " + STREAM_SESSION_ID  +" = " + session.getId(), null);

    c.moveToFirst();
    while(!c.isAfterLast())
    {
      String sensor = getString(c, STREAM_SENSOR_NAME);
      String symbol = getString(c, STREAM_MEASUREMENT_SYMBOL);
      String unit = getString(c, STREAM_MEASUREMENT_UNIT);
      String type = getString(c, STREAM_MEASUREMENT_TYPE);

      MeasurementStream stream = new MeasurementStream(sensor, type, unit, symbol);
      stream.setAvg(getDouble(c, STREAM_AVG));
      stream.setPeak(getDouble(c, STREAM_PEAK));
      stream.setId(getInt(c, STREAM_ID));
      stream.setSessionId(session.getId());

      result.add(stream);

      c.moveToNext();
    }

    return result;
  }

}
