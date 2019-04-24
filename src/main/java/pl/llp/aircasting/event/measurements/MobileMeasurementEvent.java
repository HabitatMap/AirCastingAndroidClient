package pl.llp.aircasting.event.measurements;

import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.util.Constants;

public class MobileMeasurementEvent extends MeasurementEvent {
    private long sessionId = Constants.CURRENT_SESSION_FAKE_ID;

    public MobileMeasurementEvent(Measurement measurement, Sensor sensor) {
        super(measurement, sensor);
    }

    public long getSessionId() {

        return sessionId;
    }

    public void setSessionId(Long id) {
        sessionId = id;
    }
}
