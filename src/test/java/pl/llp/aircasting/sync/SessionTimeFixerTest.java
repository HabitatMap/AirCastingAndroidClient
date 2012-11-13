package pl.llp.aircasting.sync;

import pl.llp.aircasting.New;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.repository.RepositoryException;
import pl.llp.aircasting.sync.SessionTimeFixer;

import org.junit.Test;

import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created by ags on 12/07/12 at 12:20
 */
public class SessionTimeFixerTest
{
  Session session = new Session();
  SessionTimeFixer fixer = new SessionTimeFixer();
  private Measurement m2 = New.measurement(2);
  private Measurement m1 = New.measurement(1);
  private MeasurementStream stream = new MeasurementStream();

  @Test
  public void should_needFixForSessionWithoutDates() throws Exception
  {
    // then
    assertTrue(fixer.needsTimeFix(session));
  }

  @Test
  public void should_notNeedFixForSessionWithDates() throws Exception
  {
    // given
    session.setStart(new Date());
    session.setEnd(new Date());

    // then
    assertFalse(fixer.needsTimeFix(session));
  }

  @Test
  public void should_setDatesForSession() throws Exception
  {
    // given
    session.setStart(null);
    session.setEnd(null);
    stream.add(m1);
    stream.add(m2);
    session.add(stream);

    // when
    fixer.fixStartEndTimeFromMeasurements(session);

    // then
    assertEquals(m1.getTime(), session.getStart());
    assertEquals(m2.getTime(), session.getEnd());
  }

  @Test(expected = RepositoryException.class)
  public void should_setComplainAboutNullDatesInSession() throws Exception
  {
    // given
    session.setStart(null);
    session.setEnd(null);
    m1.setTime(null);
    stream.add(m1);
    session.add(stream);

    // when
    fixer.fixStartEndTimeFromMeasurements(session);

    // then
  }
}
