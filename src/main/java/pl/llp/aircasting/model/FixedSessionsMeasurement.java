package pl.llp.aircasting.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.UUID;

public class FixedSessionsMeasurement {
  @Expose @SerializedName("session_uuid") private UUID sessionUUID;

  @Expose @SerializedName("measurements")
  private transient List<Measurement> measurements = new CopyOnWriteArrayList<Measurement>();

  @Expose @SerializedName("sensor_name") private String sensorName;
  @Expose @SerializedName("sensor_package_name") private String packageName;
  @Expose @SerializedName("measurement_type") private String measurementType;
  @Expose @SerializedName("measurement_short_type") private String shortType;
  @Expose @SerializedName("unit_name") private String unit;
  @Expose @SerializedName("unit_symbol") private String symbol;
  @Expose @SerializedName("threshold_very_high") private int thresholdVeryHigh;
  @Expose @SerializedName("threshold_very_low") private int thresholdVeryLow;
  @Expose @SerializedName("threshold_low") private int thresholdLow;
  @Expose @SerializedName("threshold_medium") private int thresholdMedium;
  @Expose @SerializedName("threshold_high") private int thresholdHigh;

  public FixedSessionsMeasurement(UUID sessionUUID, MeasurementStream stream, Measurement measurement) {
    this.sessionUUID = sessionUUID;
    this.measurements.add(measurement);;
    this.sensorName = stream.getSensorName();
    this.packageName = stream.getPackageName();
    this.measurementType = stream.getMeasurementType();
    this.shortType = stream.getShortType();
    this.unit = stream.getUnit();
    this.symbol = stream.getSymbol();
    this.thresholdVeryHigh = stream.getThresholdVeryHigh();
    this.thresholdVeryLow = stream.getThresholdVeryLow();
    this.thresholdLow = stream.getThresholdLow();
    this.thresholdMedium = stream.getThresholdMedium();
    this.thresholdHigh = stream.getThresholdHigh();
  }
}
