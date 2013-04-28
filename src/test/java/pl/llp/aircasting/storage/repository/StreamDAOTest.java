package pl.llp.aircasting.storage.repository;

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.storage.db.AirCastingDB;

import android.database.sqlite.SQLiteDatabase;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertEquals;

@RunWith(InjectedTestRunner.class)
public class StreamDAOTest
{
  @Inject
  AirCastingDB acdb;

  private SQLiteDatabase db;
  @Inject StreamDAO streams;

  @Before
  public void setUp() throws Exception
  {
    db = acdb.getDatabaseDuringTests();
  }

  @Test
  public void should_load() throws Exception
  {
    // given
    db.execSQL("INSERT INTO streams(_id, sensor_name, stream_session_id) VALUES(0, 'robolectric is fun', 2)");

    // when
    List<MeasurementStream> loaded = streams.findAllForSession(2L);

    // then
    assertEquals(1, loaded.size());
  }
}
