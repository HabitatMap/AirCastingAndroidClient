package pl.llp.aircasting.helper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.MeasurementLevel;
import pl.llp.aircasting.model.Sensor;

@Singleton
public class ThresholdsHelper {
    @Inject SettingsHelper settingsHelper;

    /**
     * Provides thresholds to qualify sensor measurements for a given sensor
     *
     * @param sensor The Sensor
     * @param level  The level for which to provide a threshold
     * @return Threshold above which measurements are to be considered on the given level
     */
    public int getThreshold(Sensor sensor, MeasurementLevel level) {
        if (settingsHelper.hasThresholds(sensor)) {
            return settingsHelper.getThreshold(level);
        } else {
            return sensor.getThreshold(level);
        }
    }
}
