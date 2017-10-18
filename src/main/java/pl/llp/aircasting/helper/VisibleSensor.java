package pl.llp.aircasting.helper;

import com.google.inject.Singleton;
import org.jetbrains.annotations.Nullable;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;

/**
 * Created by radek on 11/10/17.
 */
@Singleton
public class VisibleSensor {
    final Sensor AUDIO_SENSOR = SimpleAudioReader.getSensor();

    private Sensor sensor;

    public void set(Sensor sensor) {
        this.sensor = sensor;
    }

    public Sensor getSensor() {
        return sensor != null ? sensor : AUDIO_SENSOR;
    }
}
