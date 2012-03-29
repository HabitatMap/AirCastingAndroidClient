package pl.llp.aircasting.model;

import pl.llp.aircasting.MeasurementLevel;
import pl.llp.aircasting.event.sensor.SensorEvent;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class Sensor {
    private String sensorName;
    private String measurementType;
    private String unit;
    private String symbol;
    private Map<MeasurementLevel, Integer> thresholds = newHashMap();

    private boolean enabled = true;

    public Sensor(SensorEvent event) {
        this.sensorName = event.getSensorName();
        this.measurementType = event.getMeasurementType();
        this.unit = event.getUnit();
        this.symbol = event.getSymbol();
        
        thresholds.put(MeasurementLevel.VERY_HIGH, event.getVeryHigh());
        thresholds.put(MeasurementLevel.HIGH, event.getHigh());
        thresholds.put(MeasurementLevel.MID, event.getMid());
        thresholds.put(MeasurementLevel.LOW, event.getLow());
        thresholds.put(MeasurementLevel.VERY_LOW, event.getVeryLow());
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

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Toggle the enabled/disabled status of this Sensor
     */
    public void toggle() {
        enabled = !enabled;
    }

    public int getThreshold(MeasurementLevel level) {
        return thresholds.get(level);
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
                '}';
    }
}
