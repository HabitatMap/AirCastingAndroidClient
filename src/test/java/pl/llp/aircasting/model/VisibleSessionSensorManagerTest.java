package pl.llp.aircasting.model;

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.New;
import pl.llp.aircasting.screens.common.ApplicationState;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionManager;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionSensorManager;
import pl.llp.aircasting.event.session.VisibleSessionUpdatedEvent;
import pl.llp.aircasting.model.events.SensorEvent;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import pl.llp.aircasting.util.Constants;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.mock;

@RunWith(InjectedTestRunner.class)
public class VisibleSessionSensorManagerTest
{
  @Inject
  CurrentSessionSensorManager currentSessionSensorManager;

  @Mock
  CurrentSessionManager currentSessionManager;

  private Sensor SOME_SENSOR;
  private SensorEvent sensorEvent;
  private VisibleSessionUpdatedEvent SESSION_CHANGED;

  @Before
  public void setup() {
    SESSION_CHANGED = new VisibleSessionUpdatedEvent(new Session());
    currentSessionSensorManager.eventBus = mock(EventBus.class);
    currentSessionSensorManager.currentSessionManager = currentSessionManager;
    currentSessionManager.state = new ApplicationState();

    sensorEvent = New.sensorEvent();
    SOME_SENSOR = new Sensor(sensorEvent, Constants.CURRENT_SESSION_FAKE_ID);

    currentSessionSensorManager.onEvent(sensorEvent);
  }

  @Test
  public void shouldStoreSensorInformation()
  {
    assertThat(currentSessionSensorManager.getSensorsList(), hasItem(SOME_SENSOR));
  }

  @Test
  public void shouldNotOverwriteSensors()
  {
    currentSessionSensorManager.onEvent(sensorEvent);
    SOME_SENSOR = currentSessionSensorManager.getSensorByName("LHC");

    assertThat(SOME_SENSOR.getMeasurementType(), equalTo("Hadrons"));
  }

  @Test
  public void shouldReturnSensorsByName() {
    assertThat(currentSessionSensorManager.getSensorByName(SOME_SENSOR.getSensorName()), equalTo(SOME_SENSOR));
  }
}
