package pl.llp.aircasting.repository;

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.model.AirCastingDB;
import pl.llp.aircasting.model.Any;
import pl.llp.aircasting.model.MeasurementStream;

import android.database.sqlite.SQLiteDatabase;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

@RunWith(InjectedTestRunner.class)
public class StreamRepositoryTest
{
  @Inject AirCastingDB acdb;

  private SQLiteDatabase db;
  StreamRepository streams;
  private MeasurementStream stream;

  @Before
  public void setUp() throws Exception
  {
    db = acdb.getWritableDatabase();
    streams = new StreamRepository(db);

    stream = Any.stream();
  }

  @Test
  public void should_save() throws Exception
  {
    // when
    streams.save(stream, 2L);

    // then
    assertNotNull(stream.getId());
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
