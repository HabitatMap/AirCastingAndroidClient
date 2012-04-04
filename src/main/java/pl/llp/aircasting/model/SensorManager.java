package pl.llp.aircasting.model;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.event.session.SessionChangeEvent;
import pl.llp.aircasting.event.ui.ViewStreamEvent;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

@Singleton
public class SensorManager {
    @Inject EventBus eventBus;
    @Inject SessionManager sessionManager;

    @Inject
    public void init() {
        eventBus.register(this);
    }

    private Sensor visibleSensor = SimpleAudioReader.getSensor();
    private Map<String, Sensor> sensors = newHashMap();

    @Subscribe
    public void onEvent(SensorEvent event) {
        if (!sessionManager.isSessionSaved() && !sensors.containsKey(event.getSensorName())) {
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
    public List<Sensor> getSensors() {
        return newArrayList(sensors.values());
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

    @Subscribe
    public void onEvent(SessionChangeEvent event) {
        sensors = newHashMap();

        for (MeasurementStream stream : sessionManager.getMeasurementStreams()) {
            Sensor sensor = new Sensor(stream);
            String name = sensor.getSensorName();
            sensors.put(name, sensor);

            visibleSensor = sensor;
        }
    }
}
