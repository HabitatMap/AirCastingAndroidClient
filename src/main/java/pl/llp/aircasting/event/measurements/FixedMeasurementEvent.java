package pl.llp.aircasting.event.measurements;

import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.Sensor;

public class FixedMeasurementEvent extends MeasurementEvent {
    public FixedMeasurementEvent(Measurement measurement, Sensor sensor) {
        super(measurement, sensor);
    }
}
