package pl.llp.aircasting.model.events;

public class FixedSessionsMeasurementEvent {
    private long sessionId;

    public FixedSessionsMeasurementEvent(long sessionId) {
        this.sessionId = sessionId;
    }

    public long getSessionId() {
        return sessionId;
    }
}
