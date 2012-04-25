package pl.llp.aircasting.repository;

import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.MeasurementStream;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static pl.llp.aircasting.model.DBConstants.*;
import static pl.llp.aircasting.repository.DBHelper.getDouble;
import static pl.llp.aircasting.repository.DBHelper.getInt;
import static pl.llp.aircasting.repository.DBHelper.getLong;
import static pl.llp.aircasting.repository.DBHelper.getString;

public class StreamRepository {
  private SQLiteDatabase db;

  MeasurementRepository measurements;

  public StreamRepository(SQLiteDatabase db) {
    this.db = db;
    measurements = new MeasurementRepository(db, new NullProgressListener());
  }

  List<MeasurementStream> findAllForSession(@NotNull Long sessionId)
  {
    List<MeasurementStream> result = newArrayList();

    Cursor c = db.rawQuery("SELECT * FROM " + STREAM_TABLE_NAME +
                               " WHERE " + STREAM_SESSION_ID + " = " + sessionId, null);

    c.moveToFirst();
    while (!c.isAfterLast()) {
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

      stream.setAvg(avg);
      stream.setPeak(peak);
      stream.setId(id);
      stream.setSessionId(sessionId);
      result.add(stream);

      c.moveToNext();
    }

    return result;
  }

  private long saveOne(MeasurementStream stream, long sessionId) {
    ContentValues values = new ContentValues();

    values.put(STREAM_SESSION_ID, sessionId);
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

    long streamId = db.insertOrThrow(STREAM_TABLE_NAME, null, values);
    stream.setId(streamId);

    return streamId;
  }

  public void saveAll(Collection<MeasurementStream> streamsToSave, long sessionId) {
    for (MeasurementStream oneToSave : streamsToSave) {
      oneToSave.setSessionId(sessionId);
      long streamId = saveOne(oneToSave, sessionId);

      List<Measurement> measurementsToSave = oneToSave.getMeasurements();
      measurements.save(measurementsToSave, sessionId, streamId);
    }
  }

  public void save(MeasurementStream stream, long sessionId) {
    saveAll(Collections.singletonList(stream), sessionId);
  }
}
