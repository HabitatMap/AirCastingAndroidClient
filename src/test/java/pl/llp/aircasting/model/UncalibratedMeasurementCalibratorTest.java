package pl.llp.aircasting.model;

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.repository.SessionRepository;

import android.database.sqlite.SQLiteDatabase;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;
import java.util.UUID;

@RunWith(InjectedTestRunner.class)
public class UncalibratedMeasurementCalibratorTest
{
  @Inject AirCastingDB acdb;
  private UncalibratedMeasurementCalibrator c;
  private SQLiteDatabase db;

  @Inject SessionRepository sessions;

  @Before
  public void setUp() throws Exception
  {
    db = acdb.getWritableDatabase();
    c = new UncalibratedMeasurementCalibrator(db);
  }

  @Test
  public void should_not_crash() throws Exception
  {
    c.saveAsCalibrated();
  }

  @Test
  public void should_work() throws Exception
  {
    Session sess = new Session();
    sess.setId(1L);
    sess.setOffset60DB(10);
    sess.setCalibration(20);
    sess.setStart(new Date());
    sess.setEnd(new Date());

    UUID uuid = sess.getUUID();
    sessions.save(sess);
  }
}
