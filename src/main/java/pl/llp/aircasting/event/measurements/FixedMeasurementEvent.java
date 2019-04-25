package pl.llp.aircasting.event.measurements;

import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.Sensor;

public class FixedMeasurementEvent extends MeasurementEvent {
    private long measurementsAdded;

    public FixedMeasurementEvent(Measurement measurement, Sensor sensor, long sessionId, long measurementsAdded) {
        super(measurement, sensor, sessionId);

        this.measurementsAdded = measurementsAdded;
    }

    public long getMeasurementsAdded() {
        return measurementsAdded;
    }
}
