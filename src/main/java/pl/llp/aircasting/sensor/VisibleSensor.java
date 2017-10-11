package pl.llp.aircasting.sensor;

import com.google.inject.Singleton;
import org.jetbrains.annotations.Nullable;
import pl.llp.aircasting.model.Sensor;

/**
 * Created by radek on 11/10/17.
 */
@Singleton
public class VisibleSensor {
    private Sensor sensor;
    private Long sessionId;

    public void set(Sensor sensor, @Nullable Long sessionId) {
        this.sensor = sensor;
        this.sessionId = sessionId;
    }

    public Sensor getSensor() { return sensor; }
    
    public Long getSessionId() { return sessionId; }
}
