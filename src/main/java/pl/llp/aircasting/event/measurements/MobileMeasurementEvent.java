package pl.llp.aircasting.event.measurements;

import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.Sensor;

public class MobileMeasurementEvent extends MeasurementEvent {
    public MobileMeasurementEvent(Measurement measurement, Sensor sensor, long sessionId) {
        super(measurement, sensor, sessionId);
    }
}
