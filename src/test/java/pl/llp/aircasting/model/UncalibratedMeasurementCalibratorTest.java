package pl.llp.aircasting.model;

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.screens.common.helpers.NoOp;
import pl.llp.aircasting.storage.ProgressListener;
import pl.llp.aircasting.storage.repository.SessionRepository;
import pl.llp.aircasting.storage.db.AirCastingDB;
import pl.llp.aircasting.storage.db.UncalibratedMeasurementCalibrator;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;

import android.database.sqlite.SQLiteDatabase;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static pl.llp.aircasting.storage.db.DBConstants.*;

@RunWith(InjectedTestRunner.class)
public class UncalibratedMeasurementCalibratorTest
{
  @Inject
  AirCastingDB acdb;
  @Inject
  UncalibratedMeasurementCalibrator c;
  private SQLiteDatabase db;

  @Inject SessionRepository sessions;
  ProgressListener PROGRESS;

  @Before
  public void setUp() throws Exception
  {
    db = acdb.getDatabaseDuringTests();
    PROGRESS = NoOp.progressListener();
  }

  @Test
  public void should_not_crash() throws Exception
  {
    c.calibrate(PROGRESS);
  }

  @Test
  public void should_work() throws Exception
  {
    db.execSQL(
        "INSERT INTO SESSIONS(_id, uuid, "
        + SESSION_CALIBRATED + ", "
        + SESSION_CALIBRATION + ", "
        + SESSION_OFFSET_60_DB + ", "
        + ") VALUES(1, '" + UUID.randomUUID() + "', false, 10, 20)");

    db.execSQL("INSERT INTO STREAMS(_id, "
                   + STREAM_SENSOR_NAME + ", "
                   + STREAM_SESSION_ID + ") " +
                   " VALUES(1, '" + SimpleAudioReader.SENSOR_NAME + "', 1)");


    db.execSQL("INSERT INTO " + MEASUREMENT_TABLE_NAME + "("
                   + MEASUREMENT_STREAM_ID + ", "
                   + MEASUREMENT_ID  + ", "
                   + MEASUREMENT_VALUE
               + ") VALUES(1, 1, 10)");


    c.calibrate(PROGRESS);
  }
}
