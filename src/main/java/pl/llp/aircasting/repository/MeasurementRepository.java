package pl.llp.aircasting.repository;

import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.Session;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static pl.llp.aircasting.model.DBConstants.*;
import static pl.llp.aircasting.repository.DBHelper.getDate;
import static pl.llp.aircasting.repository.DBHelper.getDouble;
import static pl.llp.aircasting.repository.DBHelper.getInt;

/**
 * Created by ags on 30/03/12 at 12:18
 */
class MeasurementRepository
{
  private SQLiteDatabase db;
  private ProgressListener progress = new NullProgressListener();
  private Map<Integer, List<Measurement>> result;

  public MeasurementRepository(SQLiteDatabase db, ProgressListener progressListener)
  {
    this.db = db;
    if(progressListener != null)
    {
      progress = progressListener;
    }
  }

  Map<Integer, List<Measurement>> load(Session session)
  {
    Cursor c = db.rawQuery("" +
                    "SELECT * FROM " + MEASUREMENT_TABLE_NAME + " " +
                    "WHERE " + MEASUREMENT_SESSION_ID  +" = " + session.getId(), null);

    progress.onSizeCalculated(c.getCount());

    c.moveToFirst();
    while(!c.isAfterLast())
    {
      Measurement measurement = new Measurement();
      measurement.setLatitude(getDouble(c, MEASUREMENT_LATITUDE));
      measurement.setLongitude(getDouble(c, MEASUREMENT_LONGITUDE));
      measurement.setValue(getDouble(c, MEASUREMENT_VALUE));
      measurement.setTime(getDate(c, MEASUREMENT_TIME));

      values(getInt(c, MEASUREMENT_STREAM_ID)).add(measurement);

      if(c.getPosition() % 100 == 0)
      {
        progress.onProgress(c.getPosition());
      }
      c.moveToNext();
    }

    return result;
  }

  private List<Measurement> values(int anInt)
  {
    if(!result.containsKey(anInt))
    {
      result.put(anInt, new ArrayList<Measurement>());
    }
    return result.get(anInt);
  }
}
