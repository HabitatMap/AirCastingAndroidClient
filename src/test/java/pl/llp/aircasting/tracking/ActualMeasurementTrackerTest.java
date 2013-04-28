package pl.llp.aircasting.tracking;

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.storage.DatabaseTaskQueue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static pl.llp.aircasting.storage.DatabaseTaskQueueAssertions.assertThat;

/**
 * Created by ags on 24/03/2013 at 22:45
 */
@RunWith(InjectedTestRunner.class)
public class ActualMeasurementTrackerTest
{
  private ActualMeasurementTracker tracker;
  private MeasurementStream stream;
  private Measurement measurement;
  private DatabaseTaskQueue queue;

  @Before
  public void setUp() throws Exception
  {
    queue = new DatabaseTaskQueue();
    tracker = new ActualMeasurementTracker(queue);

    stream = new MeasurementStream();
    measurement = mock(Measurement.class);
  }

  @Test
  public void should_add_database_task_for_added_measurement() throws Exception
  {
    // given

    // when
    tracker.add(stream, measurement);

    // then
    assertThat(queue).hasSize(1);
  }

  @Test
  public void should_add_measurement_to_stream() throws Exception
  {
    // given
    stream = spy(stream);

    // when
    tracker.add(stream, measurement);

    // then
    verify(stream).add(measurement);
  }
}
