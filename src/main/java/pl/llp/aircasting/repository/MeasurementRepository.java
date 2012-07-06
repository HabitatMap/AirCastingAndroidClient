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
import static pl.llp.aircasting.repository.DBHelper.getDate;
import static pl.llp.aircasting.repository.DBHelper.getDouble;
import static pl.llp.aircasting.repository.DBHelper.getLong;
import static pl.llp.aircasting.repository.db.DBConstants.*;

class MeasurementRepository
{
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


  public MeasurementRepository(ProgressListener progressListener)
  {
    if(progressListener != null)
    {
      progress = progressListener;
    }
  }

  @Internal
  Map<Long, List<Measurement>> load(Session session, SQLiteDatabase readableDatabase)
  {
    Cursor measurements = readableDatabase.rawQuery("" +
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
    measurements.close();

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

  @Internal
  void save(List<Measurement> measurementsToSave, long sessionId, long streamId, SQLiteDatabase writableDatabase)
  {
    SQLiteStatement st = writableDatabase.compileStatement(INSERT_MEASUREMENTS);

    st.bindLong(1, streamId);
    st.bindLong(2, sessionId);

    for (Measurement measurement : measurementsToSave)
    {

      st.bindLong(3, measurement.getTime().getTime());
      st.bindDouble(4, measurement.getLatitude());
      st.bindDouble(5, measurement.getLongitude());
      st.bindDouble(6, measurement.getValue());
      st.executeInsert();
    }
  }

  @Internal
  void deleteAllFrom(Long streamId, SQLiteDatabase writableDatabase)
  {
    try
    {
      writableDatabase.delete(MEASUREMENT_TABLE_NAME, MEASUREMENT_STREAM_ID + " = " + streamId, null);
    }
    catch (SQLException e)
    {
      Log.e(Constants.TAG, "Error removing measurements from stream [" + streamId + "]", e);
    }
  }
}
