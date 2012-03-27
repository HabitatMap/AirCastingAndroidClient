package pl.llp.aircasting.model;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.event.ui.ToggleStreamEvent;
import pl.llp.aircasting.event.ui.ViewStreamEvent;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

@Singleton
public class SensorManager {
    @Inject EventBus eventBus;

    @Inject
    public void init() {
        eventBus.register(this);
    }

    Set<String> disabledSensors = newHashSet();
    private String visibleSensor = SimpleAudioReader.SENSOR_NAME;

    /**
     * Check if the given sensor is enabled for recording
     *
     * @param sensorName the name of the sensor to check
     * @return true if the sensor is enabled, false otherwise
     */
    public boolean isEnabled(String sensorName) {
        return !disabledSensors.contains(sensorName);
    }

    @Subscribe
    public void onEvent(ToggleStreamEvent event) {
        String name = event.getSensorName();

        if (disabledSensors.contains(name)) {
            disabledSensors.remove(name);
        } else {
            disabledSensors.add(name);
        }
    }

    @Subscribe
    public void onEvent(ViewStreamEvent event) {
        visibleSensor = event.getSensorName();
    }

    /**
     * @return The name of the sensor that is currently selected for viewing
     */
    public String getVisibleSensor() {
        return visibleSensor;
    }
}
