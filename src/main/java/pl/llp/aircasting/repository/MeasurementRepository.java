package pl.llp.aircasting.repository;

import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.util.Constants;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import org.intellij.lang.annotations.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static pl.llp.aircasting.model.DBConstants.*;
import static pl.llp.aircasting.repository.DBHelper.getDate;
import static pl.llp.aircasting.repository.DBHelper.getDouble;
import static pl.llp.aircasting.repository.DBHelper.getLong;

class MeasurementRepository
{
  private SQLiteDatabase db;
  private ProgressListener progress = new NullProgressListener();

  @Language("SQL")
  private static final String INSERT_MEASUREMENTS = "" +
      "INSERT INTO " + MEASUREMENT_TABLE_NAME + "(" +
      MEASUREMENT_STREAM_ID + ", " +
      MEASUREMENT_SESSION_ID + ", " +
      MEASUREMENT_TIME + ", " +
      MEASUREMENT_LATITUDE + ", " +
      MEASUREMENT_LONGITUDE + ", " +
      MEASUREMENT_VALUE + ") " +
      " VALUES(?, ?, ?, ?, ?, ?)";


  public MeasurementRepository(SQLiteDatabase db, ProgressListener progressListener)
  {
    this.db = db;
    if(progressListener != null)
    {
      progress = progressListener;
    }
  }

  Map<Long, List<Measurement>> load(Session session)
  {
    Cursor measurements = db.rawQuery("" +
                    "SELECT * FROM " + MEASUREMENT_TABLE_NAME + " " +
                    "WHERE " + MEASUREMENT_SESSION_ID  +" = " + session.getId(), null);

    progress.onSizeCalculated(measurements.getCount());

    measurements.moveToFirst();
    Map<Long, List<Measurement>> results = newHashMap();
    while(!measurements.isAfterLast())
    {
      Measurement measurement = new Measurement();
      measurement.setLatitude(getDouble(measurements, MEASUREMENT_LATITUDE));
      measurement.setLongitude(getDouble(measurements, MEASUREMENT_LONGITUDE));
      measurement.setValue(getDouble(measurements, MEASUREMENT_VALUE));
      measurement.setTime(getDate(measurements, MEASUREMENT_TIME));

      long id = getLong(measurements, MEASUREMENT_STREAM_ID);
      stream(id, results).add(measurement);

      int position = measurements.getPosition();
      if(position % 100 == 0)
      {
        progress.onProgress(position);
      }
      measurements.moveToNext();
    }

    return results;
  }

  private List<Measurement> stream(long id, Map<Long, List<Measurement>> results)
  {
    if(!results.containsKey(id))
    {
      results.put(id, new ArrayList<Measurement>());
    }
    return results.get(id);
  }

  public void save(List<Measurement> measurementsToSave, long sessionId, long streamId)
  {
    SQLiteStatement st = db.compileStatement(INSERT_MEASUREMENTS);

    for (Measurement measurement : measurementsToSave)
    {
      int pos = 1;

      st.bindLong(pos++, streamId);
      st.bindLong(pos++, sessionId);
      st.bindLong(pos++, measurement.getTime().getTime());
      st.bindDouble(pos++, measurement.getLatitude());
      st.bindDouble(pos++, measurement.getLongitude());
      st.bindDouble(pos++, measurement.getValue());

      st.executeInsert();
      st.clearBindings();
    }

    st.close();
  }

  public void deleteAllFrom(Long streamId)
  {
    try
    {
      db.delete(MEASUREMENT_TABLE_NAME, MEASUREMENT_STREAM_ID + " = " + streamId, null);
    }
    catch (SQLException e)
    {
      Log.e(Constants.TAG, "Error removing measurements from stream [" + streamId + "]", e);
    }
  }
}
