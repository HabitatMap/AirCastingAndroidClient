package pl.llp.aircasting.model;

import pl.llp.aircasting.event.sensor.SensorEvent;

public class Sensor {
    private String sensorName;
    private String measurementType;
    private String unit;
    private String symbol;
    private int veryLow;
    private int low;
    private int mid;
    private int high;
    private int veryHigh;

    private boolean enabled = true;

    public Sensor(SensorEvent event) {
        this.sensorName = event.getSensorName();
        this.measurementType = event.getMeasurementType();
        this.unit = event.getUnit();
        this.symbol = event.getSymbol();
        this.veryLow = event.getVeryLow();
        this.low = event.getLow();
        this.mid = event.getMid();
        this.high = event.getHigh();
        this.veryHigh = event.getVeryHigh();
    }

    public Sensor(String name) {
        sensorName = name;
    }

    public String getSensorName() {
        return sensorName;
    }

    public String getMeasurementType() {
        return measurementType;
    }

    public String getUnit() {
        return unit;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getVeryLow() {
        return veryLow;
    }

    public int getLow() {
        return low;
    }

    public int getMid() {
        return mid;
    }

    public int getHigh() {
        return high;
    }

    public int getVeryHigh() {
        return veryHigh;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Toggle the enabled/disabled status of this Sensor
     */
    public void toggle() {
        enabled = !enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sensor sensor = (Sensor) o;

        if (sensorName != null ? !sensorName.equals(sensor.sensorName) : sensor.sensorName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return sensorName != null ? sensorName.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Sensor{" +
                "sensorName='" + sensorName + '\'' +
                ", measurementType='" + measurementType + '\'' +
                ", unit='" + unit + '\'' +
                ", symbol='" + symbol + '\'' +
                ", veryLow=" + veryLow +
                ", low=" + low +
                ", mid=" + mid +
                ", high=" + high +
                ", veryHigh=" + veryHigh +
                '}';
    }
}
