package pl.llp.aircasting.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by marcin on 17/07/14.
 */
public class Regression {

    @Expose @SerializedName("sensor_name") private String sensorName;
    @Expose @SerializedName("sensor_package_name") private String sensorPackageName;
    @Expose @SerializedName("measurement_type") private String measurementType;
    @Expose @SerializedName("measurement_short_type") private String shortType;
    @Expose @SerializedName("unit_symbol") private String measurementSymbol;
    @Expose @SerializedName("unit_name") private String measurementUnit;
    @Expose @SerializedName("coefficients") private double[] coefficients;
    @Expose @SerializedName("threshold_very_low") private int thresholdVeryLow;
    @Expose @SerializedName("threshold_low") private int thresholdLow;
    @Expose @SerializedName("threshold_medium") private int thresholdMedium;
    @Expose @SerializedName("threshold_high") private int thresholdHigh;
    @Expose @SerializedName("threshold_very_high") private int thresholdVeryHigh;
    @Expose @SerializedName("reference_sensor_name") private String referenceSensorName;
    @Expose @SerializedName("reference_sensor_package_name") private String referenceSensorPackageName;
    @Expose @SerializedName("is_owner") private boolean isOwner;
    @Expose @SerializedName("id") private int backendId;
    private boolean isEnabled;

    public String getReferenceSensorName() {
        return referenceSensorName;
    }

    public String getReferenceSensorPackageName() {
        return referenceSensorPackageName;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public int getBackendId() {
        return backendId;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public String getSensorName() {
        return sensorName;
    }

    public String getSensorPackageName() {
        return sensorPackageName;
    }

    public String getMeasurementType() {
        return measurementType;
    }

    public String getShortType() {
        return shortType;
    }

    public String getMeasurementSymbol() {
        return measurementSymbol;
    }

    public String getMeasurementUnit() {
        return measurementUnit;
    }

    public double[] getCoefficients() {
        return coefficients;
    }

    public int getThresholdVeryLow() {
        return thresholdVeryLow;
    }

    public int getThresholdLow() {
        return thresholdLow;
    }

    public int getThresholdMedium() {
        return thresholdMedium;
    }

    public int getThresholdHigh() {
        return thresholdHigh;
    }

    public int getThresholdVeryHigh() {
        return thresholdVeryHigh;
    }

    public Regression(String sensorName, String sensorPackageName, String measurementType, String shortType,
                      String measurementSymbol, String measurementUnit, double[] coefficients, int thresholdVeryLow,
                      int thresholdLow, int thresholdMedium, int thresholdHigh, int thresholdVeryHigh,
                      String referenceSensorName, String referenceSensorPackageName, boolean isOwner,
                      int backendId, boolean isEnabled) {
        this.sensorName = sensorName;
        this.sensorPackageName = sensorPackageName;
        this.measurementType = measurementType;
        this.shortType = shortType;
        this.measurementSymbol = measurementSymbol;
        this.measurementUnit = measurementUnit;
        this.coefficients = coefficients;
        this.thresholdVeryLow = thresholdVeryLow;
        this.thresholdLow = thresholdLow;
        this.thresholdMedium = thresholdMedium;
        this.thresholdHigh = thresholdHigh;
        this.thresholdVeryHigh = thresholdVeryHigh;
        this.referenceSensorName = referenceSensorName;
        this.referenceSensorPackageName = referenceSensorPackageName;
        this.isOwner = isOwner;
        this.backendId = backendId;
        this.isEnabled = isEnabled;
    }

    public double apply(double value) {
        int len = coefficients.length;
        double val = coefficients[len - 1];
        for (int i = len - 2; i >= 0; i--) {
            val *= value;
            val += coefficients[i];
        }
        return val;
    }
}
