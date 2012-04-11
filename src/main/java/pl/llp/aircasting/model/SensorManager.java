package pl.llp.aircasting.model;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.event.session.SessionChangeEvent;
import pl.llp.aircasting.event.ui.ViewStreamEvent;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

@Singleton
public class SensorManager {
  @Inject SessionManager sessionManager;
  @Inject EventBus eventBus;

  @Inject
  public void init() {
    eventBus.register(this);
  }

  private Sensor visibleSensor = SimpleAudioReader.getSensor();
  private Map<String, ToggleableSensor> sensors = newHashMap();
  private Set<Sensor> disabled = newHashSet();

  @Subscribe
  public void onEvent(SensorEvent event) {
    if (!sessionManager.isSessionSaved() && !sensors.containsKey(event.getSensorName())) {
      ToggleableSensor sensor = new ToggleableSensor(event);
      if (disabled.contains(sensor)) {
        sensor.toggle();
      }

      sensors.put(sensor.getSensorName(), sensor);
    }
  }

  @Subscribe
  public void onEvent(ViewStreamEvent event) {
    visibleSensor = sensors.get(event.getSensor().getSensorName());
  }

  /**
   * @return The Sensor that is currently selected for viewing
   */
  public Sensor getVisibleSensor() {
    return visibleSensor;
  }

  /**
   * @return All currently known Sensors
   */
  public List<Sensor> getSensors() {
    ArrayList<Sensor> result = newArrayList();
    result.addAll(sensors.values());
    return result;
  }

  /**
   * @param sensorName the name of the Sensor
   * @return The Sensor with the given name if known, null otherwise
   */
  public Sensor getSensor(String sensorName) {
    return sensors.get(sensorName);
  }

  /**
   * @param sensor toggle enabled/disabled status of this Sensor
   */
  public void toggleSensor(Sensor sensor) {
    ToggleableSensor actualSensor = sensors.get(sensor.getSensorName());
    actualSensor.toggle();
  }

  @Subscribe
  public void onEvent(SessionChangeEvent event) {
    disabled = newHashSet();
    for (ToggleableSensor sensor : sensors.values()) {
      if (!sensor.isEnabled()) {
        disabled.add(sensor);
      }
    }

    sensors = newHashMap();

    for (MeasurementStream stream : sessionManager.getMeasurementStreams()) {
      ToggleableSensor sensor = new ToggleableSensor(stream);
      String name = sensor.getSensorName();
      sensors.put(name, sensor);

      visibleSensor = sensor;
    }
  }

  private static class ToggleableSensor extends Sensor {
    public ToggleableSensor(SensorEvent event) {
      super(event);
    }

    public ToggleableSensor(MeasurementStream stream) {
      super(stream);
    }

    /**
     * Toggle the enabled/disabled status of this Sensor
     */
    private void toggle() {
      enabled = !enabled;
    }
  }
}
