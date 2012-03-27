package pl.llp.aircasting.event.ui;

public class ToggleStreamEvent {
    private String sensorName;

    public ToggleStreamEvent(String sensorName) {
        this.sensorName = sensorName;
    }

    public String getSensorName() {
        return sensorName;
    }

    @Override
    public String toString() {
        return "ToggleStreamEvent{" +
                "sensorName='" + sensorName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ToggleStreamEvent event = (ToggleStreamEvent) o;

        if (sensorName != null ? !sensorName.equals(event.sensorName) : event.sensorName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return sensorName != null ? sensorName.hashCode() : 0;
    }
}
