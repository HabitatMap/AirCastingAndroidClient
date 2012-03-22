package pl.llp.aircasting.event.sensor;

import pl.llp.aircasting.model.Measurement;

public class MeasurementEvent {
    private Measurement measurement;

    public MeasurementEvent(Measurement measurement) {
        this.measurement = measurement;
    }

    public Measurement getMeasurement() {
        return measurement;
    }
}
