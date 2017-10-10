package pl.llp.aircasting.model;

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.New;
import pl.llp.aircasting.activity.ApplicationState;
import pl.llp.aircasting.activity.events.SessionChangeEvent;
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
public class SensorManagerTest
{
  @Inject SensorManager sensorManager;

  @Mock
  CurrentSessionManager currentSessionManager;

  private Sensor SOME_SENSOR;
  private SensorEvent sensorEvent;
  private SessionChangeEvent SESSION_CHANGED;

  @Before
  public void setup() {
    SESSION_CHANGED = new SessionChangeEvent(new Session());
    sensorManager.eventBus = mock(EventBus.class);
    sensorManager.currentSessionManager = currentSessionManager;
    currentSessionManager.state = new ApplicationState();

    sensorEvent = New.sensorEvent();
    SOME_SENSOR = new Sensor(sensorEvent);

    sensorManager.onEvent(sensorEvent);
  }

  @Test
  public void shouldStoreSensorInformation()
  {
    assertThat(sensorManager.getSensors(), hasItem(SOME_SENSOR));
  }

  @Test
  public void shouldNotOverwriteSensors()
  {
    sensorManager.onEvent(sensorEvent);
    SOME_SENSOR = sensorManager.getSensorByName("LHC");

    assertThat(SOME_SENSOR.getMeasurementType(), equalTo("Hadrons"));
  }

  @Test
  public void shouldReturnSensorsByName() {
    assertThat(sensorManager.getSensorByName(SOME_SENSOR.getSensorName()), equalTo(SOME_SENSOR));
  }

  @Test
  public void shouldStoreInformationAboutCurrentVisibleSensor() {
    sensorManager.onEvent(new ViewStreamEvent(SOME_SENSOR));

    assertThat(sensorManager.getVisibleSensor(), equalTo(SOME_SENSOR));
  }

  @Test
  public void should_clear_sensor_info_on_session_change() {
    when(currentSessionManager.getMeasurementStreams()).thenReturn(new ArrayList<MeasurementStream>());

    sensorManager.onEvent(SESSION_CHANGED);

    org.fest.assertions.Assertions.assertThat(sensorManager.getSensors()).isEmpty();
  }

  @Test
  public void should_read_sensor_info_from_new_session() {
    MeasurementStream stream1 = New.stream();
    MeasurementStream stream2 = spy(New.stream());
    when(stream2.getSensorName()).thenReturn("Some random name");
    SESSION_CHANGED.getSession().add(stream1);
    SESSION_CHANGED.getSession().add(stream2);

    sensorManager.onEvent(SESSION_CHANGED);

    Sensor expected1 = new Sensor(stream1);
    Sensor expected2 = new Sensor(stream2);

    List<Sensor> sensors = sensorManager.getSensors();
    org.fest.assertions.Assertions.assertThat(sensors).contains(expected1, expected2);
  }

  @Test
  public void should_not_update_sensors_when_viewing_a_session()
  {
    currentSessionManager.state = sensorManager.state;
    currentSessionManager.state.recording().startShowingOldSession();

    when(currentSessionManager.getMeasurementStreams()).thenReturn(new ArrayList<MeasurementStream>());
    sensorManager.onEvent(SESSION_CHANGED);

    sensorManager.onEvent(sensorEvent);

    org.fest.assertions.Assertions.assertThat(sensorManager.getSensors()).isEmpty();
  }

  @Test
  public void should_use_one_of_the_sensors_as_visible_when_viewing_a_session()
  {
    currentSessionManager.state.recording().startShowingOldSession();
    MeasurementStream stream = New.stream();
    when(currentSessionManager.getMeasurementStreams()).thenReturn(newArrayList(stream));

    sensorManager.onEvent(SESSION_CHANGED);

    org.fest.assertions.Assertions.assertThat(sensorManager.getVisibleSensor()).isEqualTo(SimpleAudioReader.getSensor());
  }

  @Test
  public void should_not_lose_sensor_enabled_state() {
    sensorManager.onEvent(sensorEvent);
    sensorManager.onEvent(SESSION_CHANGED);
    sensorManager.onEvent(sensorEvent);

    Sensor sensor1 = sensorManager.getSensorByName(SOME_SENSOR.getSensorName());
    org.fest.assertions.Assertions.assertThat(sensor1.isEnabled()).isTrue();
  }

  @Test
  public void should_not_lose_sensor_disabled_state() {
    sensorManager.onEvent(sensorEvent);
    sensorManager.toggleSensor(SOME_SENSOR);
    sensorManager.onEvent(SESSION_CHANGED);
    sensorManager.onEvent(sensorEvent);

    Sensor sensor1 = sensorManager.getSensorByName(SOME_SENSOR.getSensorName());
    org.fest.assertions.Assertions.assertThat(sensor1.isEnabled()).isFalse();
  }
}
