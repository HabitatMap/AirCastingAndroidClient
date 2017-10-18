package pl.llp.aircasting.model;

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.New;
import pl.llp.aircasting.activity.ApplicationState;
import pl.llp.aircasting.activity.events.SessionAddedEvent;
import pl.llp.aircasting.event.ui.ViewStreamEvent;
import pl.llp.aircasting.model.events.SensorEvent;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(InjectedTestRunner.class)
public class CurrentSessionSensorManagerTest
{
  @Inject
  CurrentSessionSensorManager currentSessionSensorManager;

  @Mock
  CurrentSessionManager currentSessionManager;

  private Sensor SOME_SENSOR;
  private SensorEvent sensorEvent;
  private SessionAddedEvent SESSION_CHANGED;

  @Before
  public void setup() {
    SESSION_CHANGED = new SessionAddedEvent(new Session());
    currentSessionSensorManager.eventBus = mock(EventBus.class);
    currentSessionSensorManager.currentSessionManager = currentSessionManager;
    currentSessionManager.state = new ApplicationState();

    sensorEvent = New.sensorEvent();
    SOME_SENSOR = new Sensor(sensorEvent);

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
