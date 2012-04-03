package pl.llp.aircasting.repository;

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.model.AirCastingDB;
import pl.llp.aircasting.model.Any;
import pl.llp.aircasting.model.Session;

import android.database.sqlite.SQLiteDatabase;
import com.google.inject.Inject;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;

@RunWith(InjectedTestRunner.class)
public class SessionRepositoryTest
{
  @Inject AirCastingDB acdb;
  @Inject SessionRepository sessions;

  private SQLiteDatabase db;
  private Session session;

  @Before
  public void setUp() throws Exception
  {
    db = acdb.getWritableDatabase();
    session = Any.session();
  }

  @Test
  public void should_set_id_during_save() throws Exception
  {
    // given
    assertThat(session.getId(), IsNull.nullValue());

    // when
    sessions.save(session);

    // then
    assertThat(session.getId(), IsNull.notNullValue());
  }

  @Test
  public void test_shallow_load() throws Exception
  {
    db.execSQL("INSERT INTO SESSIONS(_id, uuid) VALUES(1, '" + UUID.randomUUID() + "')");

    // when
    sessions.save(session);
    Session loaded = sessions.load(1);

    // then
    assertNotSame(loaded, session);
    assertEquals(0, loaded.getStart().getTime());
  }
}
