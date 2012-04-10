package pl.llp.aircasting.model;

import pl.llp.aircasting.MeasurementLevel;
import pl.llp.aircasting.event.sensor.SensorEvent;

import java.io.Serializable;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class Sensor implements Serializable {
  private String sensorName;
  private String measurementType;
  private String shortType;
  private String unit;
  private String symbol;
  private Map<MeasurementLevel, Integer> thresholds = newHashMap();

  private boolean enabled = true;

  public Sensor(SensorEvent event) {
    this(event.getSensorName(), event.getMeasurementType(), event.getShortType(), event.getUnit(), event.getSymbol(),
         event.getVeryLow(), event.getLow(), event.getMid(), event.getHigh(), event.getVeryHigh());
  }

  public Sensor(MeasurementStream stream) {
    this(stream.getSensorName(), stream.getMeasurementType(), stream.getShortType(), stream.getUnit(), stream.getSymbol(),
         stream.getThresholdVeryLow(), stream.getThresholdLow(), stream.getThresholdMedium(),
         stream.getThresholdHigh(), stream.getThresholdVeryHigh());
  }

  public Sensor(String name, String type, String shortType, String unit, String symbol,
                int veryLow, int low, int mid, int high, int veryHigh) {
    this.sensorName = name;
    this.measurementType = type;
    this.shortType = shortType;
    this.unit = unit;
    this.symbol = symbol;

    thresholds.put(MeasurementLevel.VERY_HIGH, veryHigh);
    thresholds.put(MeasurementLevel.HIGH, high);
    thresholds.put(MeasurementLevel.MID, mid);
    thresholds.put(MeasurementLevel.LOW, low);
    thresholds.put(MeasurementLevel.VERY_LOW, veryLow);
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

  public String getShortType() {
    return shortType;
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
    return new StringBuilder()
        .append(getMeasurementType())
        .append(" - ")
        .append(getSensorName())
        .append(" (")
        .append(getSymbol())
        .append(")")
        .toString();
  }
}
