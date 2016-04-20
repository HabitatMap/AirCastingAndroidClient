package pl.llp.aircasting.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.UUID;

public class RealtimeSession {
  @Expose @SerializedName("session_uuid") private UUID sessionUUID;
  @Expose @SerializedName("measurement") private Measurement measurement;
  @Expose @SerializedName("sensor_name") private String sensorName;
  @Expose @SerializedName("sensor_package_name") private String packageName;
  @Expose @SerializedName("measurement_type") private String measurementType;
  @Expose @SerializedName("measurement_short_type") private String shortType;
  @Expose @SerializedName("unit_name") private String unit;
  @Expose @SerializedName("unit_symbol") private String symbol;

  public RealtimeSession(UUID sessionUUID, MeasurementStream stream, Measurement measurement) {
    this.sessionUUID = sessionUUID;

    this.measurement = measurement;

    this.sensorName = stream.getSensorName();
    this.packageName = stream.getPackageName();
    this.measurementType = stream.getMeasurementType();
    this.shortType = stream.getShortType();
    this.unit = stream.getUnit();
    this.symbol = stream.getSymbol();
  }
}
