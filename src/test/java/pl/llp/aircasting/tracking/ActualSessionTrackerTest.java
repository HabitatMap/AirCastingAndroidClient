package pl.llp.aircasting.tracking;

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.New;
import pl.llp.aircasting.helper.MetadataHelper;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.storage.DatabaseTaskQueue;
import pl.llp.aircasting.storage.repository.SessionRepository;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static pl.llp.aircasting.storage.DatabaseTaskQueueAssertions.assertThat;

/**
 * Created by ags on 24/03/2013 at 22:23
 */
@RunWith(InjectedTestRunner.class)
public class ActualSessionTrackerTest
{
  public static final String NEW_TITLE = "New title";

  ActualSessionTracker tracker;
  MeasurementStream stream;
  Session session;

  private DatabaseTaskQueue dbQueue;
  @Inject EventBus eventBus;
  private Measurement m;
  @Inject SessionRepository sessions;

  @Before
  public void setUp() throws Exception
  {
    session = new Session();
    session.setId(1234);
    dbQueue = new DatabaseTaskQueue();
    SettingsHelper settings = mock(SettingsHelper.class);
    MetadataHelper metadata = mock(MetadataHelper.class);
    m = mock(Measurement.class);
    stream = New.stream();
    tracker = new ActualSessionTracker(eventBus, session, dbQueue, settings, metadata, sessions, false);
    tracker.save(session);
  }

  @Test
  public void should_delegate_adding_measurement() throws Exception
  {
    // given
    tracker.measurementTracker = spy(tracker.measurementTracker);

    // when
    tracker.addMeasurement(stream, m);

    // then
    verify(tracker.measurementTracker).add(stream, m);
  }

  @Test
  public void should_delegate_adding_stream() throws Exception
  {
    // given


    // when
    tracker.addStream(stream);

    // then

  }

  @Test
  public void should_not_add_same_stream_twice() throws Exception
  {
    // given
    tracker.addStream(stream);

    // when
    tracker.addStream(stream);

    // then
    assertThat(session.getMeasurementStreams()).hasSize(1);
  }

  @Test
  public void should_dump_session_in_db() throws Exception
  {
    // given
    tracker.addStream(stream);

    // when
    tracker.addMeasurement(stream, m);

    // then
    assertThat(dbQueue).hasSize(3);
    assertThat(stream.getMeasurements()).hasSize(1);
  }

  @Test
  public void should_add_stream_to_session() throws Exception
  {
    // given

    // when
    tracker.addStream(stream);

    // then
    assertThat(session.getMeasurementStreams()).contains(stream);
  }

  @Test
  public void adding_stream_should_create_a_database_task() throws Exception
  {
    // given

    // when
    tracker.addStream(stream);

    // then
    assertThat(dbQueue).hasSize(2);
  }

  @Test
  public void should_updateTitle() throws Exception
  {
    // given
    tracker.setTitle(session.getId(), NEW_TITLE);

    // when

    // then
    assertThat(session.getTitle()).isEqualTo(NEW_TITLE);
  }
}
