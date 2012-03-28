package pl.llp.aircasting.model;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.event.ui.ViewStreamEvent;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

@Singleton
public class SensorManager {
    @Inject EventBus eventBus;

    @Inject
    public void init() {
        eventBus.register(this);
    }

    private Sensor visibleSensor = new Sensor(SimpleAudioReader.SENSOR_NAME);
    private Map<String, Sensor> sensors = newHashMap();

    @Subscribe
    public void onEvent(SensorEvent event) {
        if (!sensors.containsKey(event.getSensorName())) {
            Sensor sensor = new Sensor(event);
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
    public Iterable<Sensor> getSensors() {
        return sensors.values();
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
        Sensor actualSensor = sensors.get(sensor.getSensorName());
        actualSensor.toggle();
    }
}
