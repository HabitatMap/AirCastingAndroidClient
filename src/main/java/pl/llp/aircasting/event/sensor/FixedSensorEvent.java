package pl.llp.aircasting.event.sensor;

import pl.llp.aircasting.event.measurements.MeasurementEvent;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.Sensor;

public class FixedSensorEvent extends MeasurementEvent {
    public FixedSensorEvent(Measurement measurement, Sensor sensor) {
        super(measurement, sensor);
    }
}
