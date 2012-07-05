package pl.llp.aircasting.repository;

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.model.Any;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.repository.db.AirCastingDB;

import android.database.sqlite.SQLiteDatabase;
import com.google.inject.Inject;
import org.hamcrest.core.IsNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertThat;

@RunWith(InjectedTestRunner.class)
public class SessionRepositoryTest
{
  @Inject
  AirCastingDB acdb;
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
}
