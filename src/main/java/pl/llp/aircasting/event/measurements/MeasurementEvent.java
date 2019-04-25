package pl.llp.aircasting.event.measurements;

import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.Sensor;

public class MeasurementEvent {
    private Measurement measurement;
    private long sessionId;
    private Sensor sensor;

    public MeasurementEvent(Measurement measurement, Sensor sensor, long sessionId) {
        this.measurement = measurement;
        this.sensor = sensor;
        this.sessionId = sessionId;
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
}
