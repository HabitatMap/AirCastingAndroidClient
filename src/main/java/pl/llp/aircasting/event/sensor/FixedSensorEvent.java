package pl.llp.aircasting.event.sensor;

import pl.llp.aircasting.event.measurements.MeasurementEvent;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.Sensor;

public class FixedSensorEvent extends MeasurementEvent {
    private long measurementsAdded;

    public FixedSensorEvent(Measurement measurement, Sensor sensor, long sessionId, long measurementAdded) {
        super(measurement, sensor, sessionId);

        this.measurementsAdded = measurementAdded;
    }

    public long getMeasurementsAdded() {
        return measurementsAdded;
    }
}
