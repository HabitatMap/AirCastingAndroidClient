package pl.llp.aircasting.event.measurements;

import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.util.Constants;

public class MeasurementEvent {
    private Measurement measurement;
    private Sensor sensor;
    private long sessionId = Constants.CURRENT_SESSION_FAKE_ID;

    public MeasurementEvent(Measurement measurement, Sensor sensor) {
        this.measurement = measurement;
        this.sensor = sensor;
    }

    public Measurement getMeasurement() {
        return measurement;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long id) {
        sessionId = id;
    }
}
