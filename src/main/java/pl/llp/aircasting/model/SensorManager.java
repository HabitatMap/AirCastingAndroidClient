package pl.llp.aircasting.model;

import pl.llp.aircasting.activity.ApplicationState;
import pl.llp.aircasting.activity.events.SessionChangeEvent;
import pl.llp.aircasting.event.ui.ViewStreamEvent;
import pl.llp.aircasting.helper.ResourceHelper;
import pl.llp.aircasting.model.events.MeasurementLevelEvent;
import pl.llp.aircasting.model.events.SensorEvent;
import pl.llp.aircasting.model.internal.MeasurementLevel;
import pl.llp.aircasting.model.internal.SensorName;
import pl.llp.aircasting.sensor.ExternalSensorDescriptor;
import pl.llp.aircasting.sensor.SensorStoppedEvent;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;
import pl.llp.aircasting.sensor.external.ExternalSensors;

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

  @Inject ApplicationState state;

  final Sensor AUDIO_SENSOR = SimpleAudioReader.getSensor();

  private volatile Sensor visibleSensor = AUDIO_SENSOR;
  private volatile Map<SensorName, Sensor> sensors = newConcurrentMap();
  private volatile Set<Sensor> disabled = newHashSet();

  @Inject
  public void init()
  {
    eventBus.register(this);
  }

  @Subscribe
  public void onEvent(SensorEvent event)
  {
    if(state.recording().isShowingOldSession())
    {
      return;
    }

    // IOIO
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
        double now = (int) sessionManager.getNow(visibleSensor);
        level = resourceHelper.getLevel(visibleSensor, now);
      }
      eventBus.post(new MeasurementLevelEvent(visibleSensor, level));
    }
    // end of IOIO

    if(sensors.containsKey(SensorName.from(event.getSensorName())))
    {
      return;
    }

    if(externalSensors.knows(event.getAddress()))
    {
      Sensor sensor = new Sensor(event);
      if (disabled.contains(sensor)) {
        sensor.toggle();
      }
      SensorName name = SensorName.from(sensor.getSensorName());
      if(!sensors.containsKey(name))
      {
        sensors.put(name, sensor);
      }
    }
  }

  @Subscribe
  public void onEvent(ViewStreamEvent event)
  {
    String sensorName = event.getSensor().getSensorName();
    visibleSensor = sensors.get(SensorName.from(sensorName));
    if(visibleSensor == null) visibleSensor = AUDIO_SENSOR;
  }

  @Subscribe
  public void onEvent(SensorStoppedEvent event)
  {
    disconnectSensors(event.getDescriptor());
  }

  public Sensor getVisibleSensor() {
    String sensorName = visibleSensor.getSensorName();
    Sensor sensor = sensors.get(SensorName.from(sensorName));
    return sensor != null ? sensor : AUDIO_SENSOR;
  }

  public List<Sensor> getSensors() {
    ArrayList<Sensor> result = newArrayList();
    result.addAll(sensors.values());
    return result;
  }

  public Sensor getSensorByName(String name) {
    SensorName sensorName = SensorName.from(name);
    Sensor sensor = sensors.get(sensorName);
    return sensor;
  }

  /**
   * @param sensor toggle enabled/disabled status of this Sensor
   */
  public void toggleSensor(Sensor sensor) {
    String name = sensor.getSensorName();
    Sensor actualSensor = sensors.get(SensorName.from(name));
    actualSensor.toggle();
  }

  @Subscribe
  public void onEvent(SessionChangeEvent event) {
    disabled = newHashSet();
    for (Sensor sensor : sensors.values()) {
      if (!sensor.isEnabled()) {
        disabled.add(sensor);
      }
    }

    sensors = newConcurrentMap();
    Session session = event.getSession();

    for (MeasurementStream stream : session.getMeasurementStreams())
    {
      if (stream.isMarkedForRemoval())
      {
        continue;
      }

      Sensor sensor = new Sensor(stream);
      String name = sensor.getSensorName();
      sensors.put(SensorName.from(name), sensor);

      visibleSensor = sensor;
    }
  }

  public boolean isSessionBeingRecorded()
  {
    return sessionManager.isSessionStarted();
  }

  public void deleteSensorFromCurrentSession(Sensor sensor)
  {
    String sensorName = sensor.getSensorName();
    sensors.remove(SensorName.from(sensorName));
    sessionManager.deleteSensorStream(sensor);
  }

  public boolean isSessionBeingViewed()
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
    for (Map.Entry<SensorName, Sensor> entry : sensors.entrySet())
    {
      if (!address.equals(entry.getValue().getAddress()))
      {
        newSensorNames.add(entry.getKey());
      }
    }

    Map<SensorName, Sensor> newSensors = newConcurrentMap();
    for (SensorName sensorName : newSensorNames)
    {
      Sensor sensor = sensors.get(sensorName);
      newSensors.put(sensorName, sensor);
    }
    sensors = newSensors;

    String sensorName = visibleSensor.getSensorName();
    if(!sensors.containsKey(SensorName.from(sensorName)))
    {
      eventBus.post(new ViewStreamEvent(SimpleAudioReader.getSensor()));
    }
  }
}
