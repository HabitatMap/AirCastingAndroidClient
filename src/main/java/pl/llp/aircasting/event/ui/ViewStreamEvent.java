package pl.llp.aircasting.event.ui;

import org.jetbrains.annotations.Nullable;
import pl.llp.aircasting.model.Sensor;

public class ViewStreamEvent {
    private Sensor sensor;
    @Nullable Long sessionId;

    public ViewStreamEvent(Sensor sensor, @Nullable Long sessionId) {
        this.sensor = sensor;
        this.sessionId = sessionId;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public Long getSessionId() {
        return sessionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ViewStreamEvent event = (ViewStreamEvent) o;

        if (sensor != null ? !sensor.equals(event.sensor) : event.sensor != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return sensor != null ? sensor.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ViewStreamEvent{" +
                "sensor=" + sensor +
                '}';
    }
}
