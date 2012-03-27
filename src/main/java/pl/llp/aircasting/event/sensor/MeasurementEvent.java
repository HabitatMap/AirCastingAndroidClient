package pl.llp.aircasting.event.sensor;

import pl.llp.aircasting.model.Measurement;

public class MeasurementEvent {
    private Measurement measurement;
    private String sensorName;

    public MeasurementEvent(Measurement measurement, String sensorName) {
        this.measurement = measurement;
        this.sensorName = sensorName;
    }

    public Measurement getMeasurement() {
        return measurement;
    }

    public String getSensorName() {
        return sensorName;
    }
}
