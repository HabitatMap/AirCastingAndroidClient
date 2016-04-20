package pl.llp.aircasting.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.UUID;

public class RealtimeSession {
  @Expose @SerializedName("session_uuid") private UUID sessionUUID;
  @Expose @SerializedName("sensor_name") private String sensorName;
  @Expose @SerializedName("measurement") private Measurement measurement;

  public RealtimeSession(UUID sessionUUID, MeasurementStream stream, Measurement measurement) {
    this.sessionUUID = sessionUUID;
    this.sensorName = stream.getSensorName();
    this.measurement = measurement;
  }
}
