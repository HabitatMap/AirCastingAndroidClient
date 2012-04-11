package pl.llp.aircasting.model;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.event.session.SessionChangeEvent;
import pl.llp.aircasting.event.ui.ViewStreamEvent;

import java.util.ArrayList;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.*;

@RunWith(InjectedTestRunner.class)
public class SensorManagerTest {
  @Inject SensorManager manager;

  @Mock SessionManager sessionManager;

  private Sensor sensor;
  private SensorEvent sensorEvent;
  private SessionChangeEvent sessionChangeEvent;

  @Before
  public void setup() {
    manager.eventBus = mock(EventBus.class);
    manager.sessionManager = sessionManager;

    sensorEvent = new SensorEvent("LHC", "Hadrons", "H", "number", "#", 0, 10, 20, 30, 40, 12);
    sensor = new Sensor(sensorEvent);

    manager.onEvent(sensorEvent);
  }

  @Test
  public void shouldStoreSensorInformation() {
    assertThat(manager.getSensors(), hasItem(sensor));
  }

  @Test
  public void shouldNotOverwriteSensors() {
    manager.onEvent(new SensorEvent("LHC", "Muons", "M", "number", "#", 0, 10, 20, 30, 40, 12));
    sensor = manager.getSensor("LHC");

    assertThat(sensor.getMeasurementType(), equalTo("Hadrons"));
  }

  @Test
  public void shouldReturnSensorsByName() {
    assertThat(manager.getSensor(sensor.getSensorName()), equalTo(sensor));
  }

  @Test
  public void shouldStoreInformationAboutCurrentVisibleSensor() {
    manager.onEvent(new ViewStreamEvent(sensor));

    assertThat(manager.getVisibleSensor(), equalTo(sensor));
  }

  @Test
  public void should_clear_sensor_info_on_session_change() {
    when(sessionManager.getMeasurementStreams()).thenReturn(new ArrayList<MeasurementStream>());

    manager.onEvent(sessionChangeEvent);

    assertThat(manager.getSensors().isEmpty(), equalTo(true));
  }

  @Test
  public void should_read_sensor_info_from_new_session() {
    MeasurementStream stream1 = Any.stream();
    MeasurementStream stream2 = spy(Any.stream());
    when(stream2.getSensorName()).thenReturn("Some random name");
    when(sessionManager.getMeasurementStreams()).thenReturn(newArrayList(stream1, stream2));

    sessionChangeEvent = new SessionChangeEvent();
    manager.onEvent(sessionChangeEvent);

    Sensor expected1 = new Sensor(stream1);
    Sensor expected2 = new Sensor(stream2);
    assertThat(manager.getSensors(), hasItem(expected1));
    assertThat(manager.getSensors(), hasItem(expected2));
  }

  @Test
  public void should_not_update_sensors_when_viewing_a_session() {
    when(sessionManager.isSessionSaved()).thenReturn(true);
    when(sessionManager.getMeasurementStreams()).thenReturn(new ArrayList<MeasurementStream>());

    manager.onEvent(sessionChangeEvent);
    manager.onEvent(sensorEvent);

    assertThat(manager.getSensors().isEmpty(), equalTo(true));
  }

  @Test
  public void should_use_one_of_the_sensors_as_visible_when_viewing_a_session() {
    MeasurementStream stream = Any.stream();
    when(sessionManager.isSessionSaved()).thenReturn(true);
    when(sessionManager.getMeasurementStreams()).thenReturn(newArrayList(stream));

    manager.onEvent(sessionChangeEvent);

    Sensor expected = new Sensor(stream);
    assertThat(manager.getVisibleSensor(), equalTo(expected));
  }

  @Test
  public void should_not_lose_sensor_enabled_state() {
    manager.onEvent(sensorEvent);
    manager.onEvent(sessionChangeEvent);
    manager.onEvent(sensorEvent);

    Sensor sensor1 = manager.getSensor(sensor.getSensorName());
    assertThat(sensor1.isEnabled(), equalTo(true));
  }

  @Test
  public void should_not_lose_sensor_disabled_state() {
    manager.onEvent(sensorEvent);
    manager.toggleSensor(sensor);
    manager.onEvent(sessionChangeEvent);
    manager.onEvent(sensorEvent);

    Sensor sensor1 = manager.getSensor(sensor.getSensorName());
    assertThat(sensor1.isEnabled(), equalTo(false));
  }
}
