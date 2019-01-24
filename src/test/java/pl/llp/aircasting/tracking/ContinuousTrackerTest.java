package pl.llp.aircasting.tracking;

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.storage.DatabaseTaskQueue;

import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static pl.llp.aircasting.storage.DatabaseTaskQueueAssertions.assertThat;

/**
 * Created by ags on 24/03/2013 at 22:58
 */
@RunWith(InjectedTestRunner.class)
public class ContinuousTrackerTest
{
  @Inject ContinuousTracker tracker;

  DatabaseTaskQueue queue;
  private Session session;

  @Before
  public void setUp() throws Exception
  {
    queue = tracker.taskQueue;
    session = new Session();
  }

  @Test
  public void should_start_tracking_and_insert_db_task_for_that() throws Exception
  {
    // given
    tracker.metadataHelper = mock(MetadataHelper.class);
    when(tracker.metadataHelper.getPhoneModel()).thenReturn("EyPhone");

    // when
    tracker.startTracking(session, false);

    // then
    assertThat(queue).hasSize(1);
  }
}
