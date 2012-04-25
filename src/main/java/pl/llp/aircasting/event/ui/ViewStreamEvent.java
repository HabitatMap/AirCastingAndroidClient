package pl.llp.aircasting.event.ui;

import pl.llp.aircasting.model.Sensor;

public class ViewStreamEvent {
    private Sensor sensor;

    public ViewStreamEvent(Sensor sensor) {
        this.sensor = sensor;
    }

    public Sensor getSensor() {
        return sensor;
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
