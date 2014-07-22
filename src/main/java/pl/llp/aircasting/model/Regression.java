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
    @Expose @SerializedName("measurement_symbol") private String measurementSymbol;
    @Expose @SerializedName("measurement_unit") private String measurementUnit;
    @Expose @SerializedName("coefficients") private double[] coefficients;
    @Expose @SerializedName("threshold_very_low") private int thresholdVeryLow;
    @Expose @SerializedName("threshold_low") private int thresholdLow;
    @Expose @SerializedName("threshold_medium") private int thresholdMedium;
    @Expose @SerializedName("threshold_high") private int thresholdHigh;
    @Expose @SerializedName("threshold_very_high") private int thresholdVeryHigh;

    public String getSensorName() {
        return sensorName;
    }

    public String getSensorPackageName() {
        return sensorPackageName;
    }

    public String getMeasurementType() {
        return measurementType;
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

    public Regression(String sensorName, String sensorPackageName, String measurementType, String measurementSymbol,
                      String measurementUnit, double[] coefficients, int thresholdVeryLow,
                      int thresholdLow, int thresholdMedium, int thresholdHigh, int thresholdVeryHigh) {
        this.sensorName = sensorName;
        this.sensorPackageName = sensorPackageName;
        this.measurementType = measurementType;
        this.measurementSymbol = measurementSymbol;
        this.measurementUnit = measurementUnit;
        this.coefficients = coefficients;
        this.thresholdVeryLow = thresholdVeryLow;
        this.thresholdLow = thresholdLow;
        this.thresholdMedium = thresholdMedium;
        this.thresholdHigh = thresholdHigh;
        this.thresholdVeryHigh = thresholdVeryHigh;
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
