package pl.llp.aircasting.model;

import pl.llp.aircasting.MeasurementLevel;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.event.session.SessionChangeEvent;
import pl.llp.aircasting.event.ui.ViewStreamEvent;
import pl.llp.aircasting.helper.ResourceHelper;
import pl.llp.aircasting.sensor.SensorStoppedEvent;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;
import pl.llp.aircasting.sensor.external.ExternalSensors;
import pl.llp.aircasting.util.Constants;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newConcurrentMap;
import static com.google.common.collect.Sets.newHashSet;

@Singleton
public class SensorManager
{
  @Inject ResourceHelper resourceHelper;
  @Inject ExternalSensors externalSensors;
  @Inject SessionManager sessionManager;
  @Inject EventBus eventBus;

  private Sensor visibleSensor = SimpleAudioReader.getSensor();
  private volatile Map<SensorName, ToggleableSensor> sensors = newConcurrentMap();
  private Set<Sensor> disabled = newHashSet();

  @Inject
  public void init()
  {
    eventBus.register(this);
  }

  @Subscribe
  public void onEvent(SensorEvent event)
  {
    Sensor visibleSensor = getVisibleSensor();
    if (visibleSensor != null && visibleSensor.matches(getSensorByName(event.getSensorName())))
    {
      MeasurementLevel level = null;
      if (sessionManager.isSessionSaved())
      {
        level = MeasurementLevel.TOO_LOW;
      }
      else
      {
        int now = (int) sessionManager.getNow(visibleSensor);
        level = resourceHelper.getLevel(visibleSensor, now);
      }
      eventBus.post(new MeasurementLevelEvent(visibleSensor, level));
    }

    if (sessionManager.isSessionSaved() || sensors.containsKey(SensorName.from(event.getSensorName())))
    {
      return;
    }
    if(externalSensors.knows(event.getAddress()) || Constants.isDevMode())
    {
      ToggleableSensor sensor = new ToggleableSensor(event);
      if (disabled.contains(sensor)) {
        sensor.toggle();
      }

      sensors.put(SensorName.from(sensor.getSensorName()), sensor);
    }
  }

  @Subscribe
  public void onEvent(ViewStreamEvent event) {
    String sensorName = event.getSensor().getSensorName();
    visibleSensor = sensors.get(SensorName.from(sensorName));
    if(visibleSensor == null) visibleSensor = SimpleAudioReader.getSensor();
  }

  @Subscribe
  public void onEvent(SensorStoppedEvent event)
  {
    disconnectSensors(event.getDescriptor());
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
  public Sensor getSensorByName(SensorName sensorName) {
    return sensors.get(sensorName);
  }

  public Sensor getSensorByName(String name) {
    SensorName sensorName = SensorName.from(name);
    return sensors.get(sensorName);
  }

  /**
   * @param sensor toggle enabled/disabled status of this Sensor
   */
  public void toggleSensor(Sensor sensor) {
    String name = sensor.getSensorName();
    ToggleableSensor actualSensor = sensors.get(SensorName.from(name));
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

    sensors = newConcurrentMap();

    for (MeasurementStream stream : sessionManager.getMeasurementStreams())
    {
      if (stream.isMarkedForRemoval())
      {
        continue;
      }

      ToggleableSensor sensor = new ToggleableSensor(stream);
      String name = sensor.getSensorName();
      sensors.put(SensorName.from(name), sensor);

      visibleSensor = sensor;
    }
  }

  public boolean hasRunningSession()
  {
    return sessionManager.isSessionStarted();
  }

  public void deleteSensorFromCurrentSession(Sensor sensor)
  {
    String sensorName = sensor.getSensorName();
    sensors.remove(SensorName.from(sensorName));
    sessionManager.deleteSensorStream(sensor);
  }

  public boolean hasBackingSession()
  {
    return sessionManager.isSessionSaved();
  }

  public void disconnectSensors(ExternalSensorDescriptor descriptor)
  {
    String address = descriptor.getAddress();
    Collection<MeasurementStream> streams = sessionManager.getMeasurementStreams();
    for (MeasurementStream stream : streams)
    {
      if(address.equals(stream.getAddress()))
      {
        stream.markAs(MeasurementStream.Visibility.INVISIBLE_DISCONNECTED);
      }
    }

    Set<SensorName> newSensorNames = newHashSet();
    for (Map.Entry<SensorName, ToggleableSensor> entry : sensors.entrySet())
    {
      if (!address.equals(entry.getValue().getAddress()))
      {
        newSensorNames.add(entry.getKey());
      }
    }

    Map<SensorName, ToggleableSensor> newSensors = newConcurrentMap();
    for (SensorName sensorName : newSensorNames)
    {
      ToggleableSensor sensor = sensors.get(sensorName);
      newSensors.put(sensorName, sensor);
    }
    sensors = newSensors;

    String sensorName = visibleSensor.getSensorName();
    if(!sensors.containsKey(SensorName.from(sensorName)))
    {
      eventBus.post(new ViewStreamEvent(SimpleAudioReader.getSensor()));
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
