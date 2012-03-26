package pl.llp.aircasting.helper;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.MeasurementLevel;
import pl.llp.aircasting.event.sensor.SensorEvent;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

@Singleton
public class ThresholdsHelper {
    @Inject SettingsHelper settingsHelper;
    @Inject EventBus eventBus;

    Map<String, Map<MeasurementLevel, Integer>> thresholds = newHashMap();

    @Inject
    public void init() {
        eventBus.register(this);
    }

    /**
     * Provides thresholds to qualify sensor measurements for a given sensor
     *
     * @param sensorName The name of the sensor
     * @param level      The level for which to provide a threshold
     * @return Threshold above which measurements are to be considered on the given level
     */
    public int getThreshold(String sensorName, MeasurementLevel level) {
        if (settingsHelper.hasThresholds(sensorName)) {
            return settingsHelper.getThreshold(level);
        } else if (thresholds.containsKey(sensorName) && thresholds.get(sensorName).containsKey(level)) {
            return thresholds.get(sensorName).get(level);
        } else {
            return 0;
        }
    }

    @Subscribe
    public void onEvent(SensorEvent event) {
        Map<MeasurementLevel, Integer> sensorThresholds = newHashMap();

        sensorThresholds.put(MeasurementLevel.VERY_LOW, event.getVeryLow());
        sensorThresholds.put(MeasurementLevel.LOW, event.getLow());
        sensorThresholds.put(MeasurementLevel.MID, event.getMid());
        sensorThresholds.put(MeasurementLevel.HIGH, event.getHigh());
        sensorThresholds.put(MeasurementLevel.VERY_HIGH, event.getVeryHigh());

        thresholds.put(event.getSensorName(), sensorThresholds);
    }
}
