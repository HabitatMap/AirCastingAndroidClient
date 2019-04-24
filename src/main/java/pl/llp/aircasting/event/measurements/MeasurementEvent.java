package pl.llp.aircasting.event.measurements;

import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.Sensor;

public class MeasurementEvent {
    private Measurement measurement;
    private Sensor sensor;

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
}
