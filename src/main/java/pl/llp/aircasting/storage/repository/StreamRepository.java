package pl.llp.aircasting.storage.repository;

import pl.llp.aircasting.android.Logger;
import pl.llp.aircasting.helper.NoOp;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.storage.db.AirCastingDB;
import pl.llp.aircasting.storage.db.ReadOnlyDatabaseTask;
import pl.llp.aircasting.storage.db.WritableDatabaseTask;
import pl.llp.aircasting.util.Constants;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.google.common.base.Stopwatch;
import com.google.inject.Inject;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Lists.newArrayList;
import static pl.llp.aircasting.storage.DBHelper.*;
import static pl.llp.aircasting.storage.db.DBConstants.*;

public class StreamRepository
{
  @Language("SQL")
  private static final String STREAM_IS_SUBMITTED_FOR_DELETE = STREAM_SUBMITTED_FOR_REMOVAL + " = 1 ";

  @Inject
  AirCastingDB airCastingDB;

  MeasurementRepository measurements;

  public StreamRepository()
  {
    measurements = new MeasurementRepository(NoOp.progressListener());
  }

  @Internal
  List<MeasurementStream> findAllForSession(@NotNull final Long sessionId)
  {
    return airCastingDB.executeReadOnlyTask(new ReadOnlyDatabaseTask<List<MeasurementStream>>()
    {
      @Override
      public List<MeasurementStream> execute(SQLiteDatabase readOnlyDatabase)
      {
        List<MeasurementStream> result = newArrayList();

        Cursor c = readOnlyDatabase.rawQuery("SELECT * FROM " + STREAM_TABLE_NAME +
                                                 " WHERE " + STREAM_SESSION_ID + " = " + sessionId + "", null);

        c.moveToFirst();
        while (!c.isAfterLast())
        {
          String sensor = getString(c, STREAM_SENSOR_NAME);
          String packageName = getString(c, STREAM_SENSOR_PACKAGE_NAME);
          String symbol = getString(c, STREAM_MEASUREMENT_SYMBOL);
          String unit = getString(c, STREAM_MEASUREMENT_UNIT);
          String type = getString(c, STREAM_MEASUREMENT_TYPE);
          String shortType = getString(c, STREAM_SHORT_TYPE);

          int thresholdVeryLow = getInt(c, STREAM_THRESHOLD_VERY_LOW);
          int thresholdLow = getInt(c, STREAM_THRESHOLD_LOW);
          int thresholdMedium = getInt(c, STREAM_THRESHOLD_MEDIUM);
          int thresholdHigh = getInt(c, STREAM_THRESHOLD_HIGH);
          int thresholdVeryHigh = getInt(c, STREAM_THRESHOLD_VERY_HIGH);

          MeasurementStream stream;

          stream = new MeasurementStream(packageName, sensor, type, shortType, unit, symbol,
                                         thresholdVeryLow,
                                         thresholdLow,
                                         thresholdMedium,
                                         thresholdHigh,
                                         thresholdVeryHigh);

          double avg = getDouble(c, STREAM_AVG);
          double peak = getDouble(c, STREAM_PEAK);
          long id = getLong(c, STREAM_ID);
          boolean markedForRemoval = getBool(c, STREAM_MARKED_FOR_REMOVAL);

          stream.setAvg(avg);
          stream.setPeak(peak);
          stream.setId(id);
          stream.setSessionId(sessionId);

          stream.setMarkedForRemoval(markedForRemoval);
          result.add(stream);

          c.moveToNext();
        }
        c.close();
        return result;
      }
    });
  }

  @Internal
  private long saveOne(MeasurementStream stream, long sessionId, SQLiteDatabase writableDatabase)
  {
    ContentValues values = values(stream);

    values.put(STREAM_SESSION_ID, sessionId);
    long streamId = writableDatabase.insertOrThrow(STREAM_TABLE_NAME, null, values);
    stream.setId(streamId);

    return streamId;
  }

  private ContentValues values(MeasurementStream stream)
  {
    ContentValues values = new ContentValues();
    values.put(STREAM_SENSOR_PACKAGE_NAME, stream.getPackageName());
    values.put(STREAM_SENSOR_NAME, stream.getSensorName());
    values.put(STREAM_MEASUREMENT_SYMBOL, stream.getSymbol());
    values.put(STREAM_MEASUREMENT_UNIT, stream.getUnit());
    values.put(STREAM_MEASUREMENT_TYPE, stream.getMeasurementType());
    values.put(STREAM_SHORT_TYPE, stream.getShortType());
    values.put(STREAM_AVG, stream.getAvg());
    values.put(STREAM_PEAK, stream.getPeak());
    values.put(STREAM_THRESHOLD_VERY_LOW, stream.getThresholdVeryLow());
    values.put(STREAM_THRESHOLD_LOW, stream.getThresholdLow());
    values.put(STREAM_THRESHOLD_MEDIUM, stream.getThresholdMedium());
    values.put(STREAM_THRESHOLD_HIGH, stream.getThresholdHigh());
    values.put(STREAM_THRESHOLD_VERY_HIGH, stream.getThresholdVeryHigh());
    values.put(STREAM_MARKED_FOR_REMOVAL, stream.isMarkedForRemoval());
    values.put(STREAM_SUBMITTED_FOR_REMOVAL, stream.isSubmittedForRemoval());
    return values;
  }

  @Internal
  public void saveAll(Collection<MeasurementStream> streamsToSave, long sessionId, SQLiteDatabase writableDatabase)
  {
    for (MeasurementStream oneToSave : streamsToSave)
    {
      Stopwatch s = new Stopwatch().start();
      oneToSave.setSessionId(sessionId);
      long streamId = saveOne(oneToSave, sessionId, writableDatabase);
      Log.d(Constants.PERFORMANCE_TAG, "Saving stream took: " + s.elapsed(TimeUnit.MILLISECONDS));

      s.reset().start();
      List<Measurement> measurementsToSave = oneToSave.getMeasurements();
      measurements.save(measurementsToSave, sessionId, streamId, writableDatabase);
      Log.d(Constants.PERFORMANCE_TAG, "Saving " + measurementsToSave.size() + " measurements took: " + s.elapsed(TimeUnit.MILLISECONDS));
    }
  }

  @Internal
  void markForRemoval(MeasurementStream stream, long sessionId, SQLiteDatabase writableDatabase)
  {
    try
    {
      ContentValues values = new ContentValues();
      values.put(STREAM_MARKED_FOR_REMOVAL, true);
  
      writableDatabase.update(STREAM_TABLE_NAME, values, STREAM_ID + " = " + stream.getId(), null);
    }
    catch (SQLException e)
    {
      Logger.e("Unable to mark stream [" + stream.getId() + "] from session [" + sessionId + "] to be deleted", e);
    }
  }

  @API
  public void update(final MeasurementStream stream)
  {
    final ContentValues values = values(stream);
    values.put(STREAM_SESSION_ID, stream.getSessionId());

    airCastingDB.executeWritableTask(new WritableDatabaseTask<Void>()
    {
      @Override
      public Void execute(SQLiteDatabase writableDatabase)
      {
        try
        {
          writableDatabase.update(STREAM_TABLE_NAME, values, STREAM_ID + " = " + stream.getId(), null);
        }
        catch(SQLException e)
        {
          Logger.e("Error updating stream [" + stream.getId() + "]", e);
        }
        return null;
      }
    });
  }

  @Internal
  void deleteMeasurements(long streamId, SQLiteDatabase writableDatabase)
  {
    try
    {
      measurements.deleteAllFrom(streamId, writableDatabase);
    }
    catch (SQLException e)
    {
      Logger.e("Error deleting measurements from stream [" + streamId + "]", e);
    }
  }

  @Internal
  void deleteSubmitted(SQLiteDatabase writableDatabase)
  {
    try
    {
      Cursor cursor = writableDatabase.query(STREAM_TABLE_NAME, null, STREAM_IS_SUBMITTED_FOR_DELETE, null, null, null, null);
      cursor.moveToFirst();
      while(!cursor.isAfterLast())
      {
        Long streamId = getLong(cursor, STREAM_ID);
        deleteMeasurements(streamId, writableDatabase);
        cursor.moveToNext();
      }
      cursor.close();

      writableDatabase.execSQL("DELETE FROM " + STREAM_TABLE_NAME + " WHERE " + STREAM_IS_SUBMITTED_FOR_DELETE);
    }
    catch (SQLException e)
    {
      Logger.e("Error deleting streams submitted to be deleted", e);
    }
  }

  @API
  public void markRemovedForRemovalAsSubmitted()
  {
    airCastingDB.executeWritableTask(new WritableDatabaseTask<Void>()
    {
      @Override
      public Void execute(SQLiteDatabase writableDatabase)
      {
        @Language("SQL")
        String sql = "UPDATE " + STREAM_TABLE_NAME + " SET " + STREAM_SUBMITTED_FOR_REMOVAL + "= 1 WHERE " + STREAM_MARKED_FOR_REMOVAL + "=1";
        writableDatabase.execSQL(sql);
        return null;
      }
    });
  }
}
