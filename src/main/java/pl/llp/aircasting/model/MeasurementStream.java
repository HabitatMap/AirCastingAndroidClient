package pl.llp.aircasting.model;

import android.util.Log;
import com.google.common.base.Optional;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static pl.llp.aircasting.model.internal.MeasurementLevel.*;

public class MeasurementStream implements Serializable {
    @Expose @SerializedName("measurements") private transient List<Measurement> measurements = new CopyOnWriteArrayList<Measurement>();
    @Expose @SerializedName("sensor_name") private String sensorName;
    @Expose @SerializedName("sensor_package_name") private String packageName;
    @Expose @SerializedName("measurement_type") private String measurementType;
    @Expose @SerializedName("measurement_short_type") private String shortType;
    @Expose @SerializedName("unit_name") private String unit;
    @Expose @SerializedName("unit_symbol") private String symbol;
    @Expose @SerializedName("average_value") Double avg;
    @Expose @SerializedName("threshold_very_high") private int thresholdVeryHigh;
    @Expose @SerializedName("threshold_very_low") private int thresholdVeryLow;
    @Expose @SerializedName("threshold_low") private int thresholdLow;
    @Expose @SerializedName("threshold_medium") private int thresholdMedium;
    @Expose @SerializedName("threshold_high") private int thresholdHigh;
    @Expose @SerializedName("min_latitude") private Double minLatitude;
    @Expose @SerializedName("max_latitude") private Double maxLatitude;
    @Expose @SerializedName("min_longitude") private Double minLongitude;
    @Expose @SerializedName("max_longitude") private Double maxLongitude;
    @Expose @SerializedName("deleted") private boolean markedForRemoval;

    private long id;
    private long sessionId;
    private Double sum = 0.0;
    private Double frequency = 0.0;
    private Double peak;
    private transient boolean submittedForRemoval;
    private transient Visibility visibility = Visibility.VISIBLE;
    private transient String address = "none";

    public MeasurementStream() {
    }

    public MeasurementStream(Sensor sensor) {
        this(sensor.getPackageName(),
                sensor.getSensorName(),
                sensor.getMeasurementType(),
                sensor.getShortType(),
                sensor.getUnit(),
                sensor.getSymbol(),
                sensor.getThreshold(VERY_LOW),
                sensor.getThreshold(MID),
                sensor.getThreshold(LOW),
                sensor.getThreshold(HIGH),
                sensor.getThreshold(VERY_HIGH),
                "none");
    }

    public MeasurementStream(
            String packageName, String sensorName, String type, String shortType, String unit, String symbol,
            int thresholdVeryLow, int thresholdLow, int thresholdMedium, int thresholdHigh, int thresholdVeryHigh) {
        this(packageName, sensorName, type, shortType, unit, symbol,
                thresholdVeryLow, thresholdLow, thresholdMedium, thresholdHigh, thresholdVeryHigh, "none");
    }

    public MeasurementStream(String packageName, String sensorName, String type, String shortType, String unit, String symbol,
                             int thresholdVeryLow, int thresholdLow,
                             int thresholdMedium,
                             int thresholdHigh, int thresholdVeryHigh, String address
    ) {
        this.packageName = packageName;
        this.sensorName = sensorName;
        this.measurementType = type;
        this.shortType = shortType;
        this.unit = unit;
        this.symbol = symbol;

        this.thresholdVeryLow = thresholdVeryLow;
        this.thresholdLow = thresholdLow;
        this.thresholdMedium = thresholdMedium;
        this.thresholdHigh = thresholdHigh;
        this.thresholdVeryHigh = thresholdVeryHigh;

        this.address = address;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public long getMeasurementsCount() {
        return measurements.size();
    }

    public List<Measurement> getLastMeasurements(int amount) {
        int size = measurements.size();

        if (size > amount)
            return measurements.subList(size - amount, size);
        else
            return measurements;
    }

    public double getLatestMeasurementValue() {
        return getLastMeasurements(1).get(0).getValue();
    }

    public List<Measurement> getMeasurementsForPeriod(int amount) {
        frequency = calculateSamplingFrequency();

        try {
            int measurementsInPeriod = (int) (60 / frequency) * amount;
            return getLastMeasurements(measurementsInPeriod);
        } catch (IndexOutOfBoundsException e) {
            return getMeasurementsForPeriod(amount - 1);
        }
    }

    public Date getLastMeasurementTime() {
        return getLastMeasurements(1).get(0).getTime();
    }

    private double calculateSamplingFrequency() {
        if (frequency > 0.0) {
            return frequency;
        }

        double deltaSum = 0;
        List<Measurement> sample = getLastMeasurements(5);

        for (int i = 0; i < sample.size() - 1; i++) {
            double delta = sample.get(i + 1).getTime().getTime() - sample.get(i).getTime().getTime();
            deltaSum += delta;
        }

        double average = deltaSum / (4 * 1000);

        return average;
    }

    public void add(Measurement measurement) {
        if (peak == null)
            peak = Double.NEGATIVE_INFINITY;

        measurements.add(measurement);
        double value = measurement.getValue();
        sum += value;
        if (value > peak) {
            peak = value;
        }

        Optional<Double> average = Optional.fromNullable(avg);
        avg = average.or(0.0) + (value - average.or(0.0)) / (measurements.size());

        if (minLatitude == null) minLatitude = Double.POSITIVE_INFINITY;
        if (minLongitude == null) minLongitude = Double.POSITIVE_INFINITY;
        if (maxLatitude == null) maxLatitude = Double.NEGATIVE_INFINITY;
        if (maxLongitude == null) maxLongitude = Double.NEGATIVE_INFINITY;

        maxLatitude = Math.max(maxLatitude, measurement.getLatitude());
        minLatitude = Math.min(minLatitude, measurement.getLatitude());

        maxLongitude = Math.max(maxLongitude, measurement.getLatitude());
        minLongitude = Math.min(minLongitude, measurement.getLatitude());
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

    private double calculatePeak() {
        double newPeak = Integer.MIN_VALUE;
        for (Measurement measurement : measurements) {
            if (measurement.getValue() > newPeak) {
                newPeak = measurement.getValue();
            }
        }
        return newPeak;
    }

    public double getPeak() {
        if (peak == null) {
            peak = calculatePeak();
        }
        return peak;
    }

    public double getAvg() {
        if (avg == null) {
            avg = calculateAvg();
        }
        return avg;
    }

    private double calculateAvg() {
        double sum = getSum();

        return sum / (measurements.isEmpty() ? 1 : measurements.size());
    }

    public double getFrequency(boolean isFixed) {
        if (frequency <= 0.0) {
            if (isFixed) {
                frequency = Double.valueOf(1);
            } else {
                frequency = calculateSamplingFrequency();
            }
        }
        return frequency;
    }

    public void setPeak(double peak) {
        this.peak = peak;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MeasurementStream stream = (MeasurementStream) o;

        if (measurementType != null ? !measurementType.equals(stream.measurementType) : stream.measurementType != null)
            return false;
        if (sensorName != null ? !sensorName.equals(stream.sensorName) : stream.sensorName != null) return false;
        if (symbol != null ? !symbol.equals(stream.symbol) : stream.symbol != null) return false;
        if (unit != null ? !unit.equals(stream.unit) : stream.unit != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = sensorName != null ? sensorName.hashCode() : 0;
        result = 31 * result + (measurementType != null ? measurementType.hashCode() : 0);
        result = 31 * result + (unit != null ? unit.hashCode() : 0);
        result = 31 * result + (symbol != null ? symbol.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MeasurementStream{" +
                "measurements=" + measurements +
                ", sensorName='" + sensorName + '\'' +
                ", measurementType='" + measurementType + '\'' +
                ", unit='" + unit + '\'' +
                ", symbol='" + symbol + '\'' +
                ", sum=" + sum +
                ", peak=" + peak +
                '}';
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSessionId() {
        return sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    public void setMeasurements(List<Measurement> measurements) {
        this.measurements = measurements;

        sum = 0.0;
        for (Measurement measurement : measurements) {
            sum += measurement.getValue();
        }

        avg = null;
    }

    public Double getSum() {
        return sum;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }

    public int getThresholdVeryHigh() {
        return thresholdVeryHigh;
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

    public boolean isEmpty() {
        return measurements.isEmpty();
    }

    public String getPackageName() {
        return packageName;
    }

    public boolean isMarkedForRemoval() {
        return markedForRemoval;
    }

    public void setMarkedForRemoval(boolean markedForRemoval) {
        this.markedForRemoval = markedForRemoval;
    }

    public boolean isSubmittedForRemoval() {
        return submittedForRemoval;
    }

    public void setSubmittedForRemoval(boolean submittedForRemoval) {
        this.submittedForRemoval = submittedForRemoval;
    }

    public boolean isVisible() {
        boolean deleted = Visibility.INVISIBLE_DELETED.equals(visibility);
        boolean disconnected = Visibility.INVISIBLE_DISCONNECTED.equals(visibility);
        return !(deleted || disconnected);
    }

    public void markAs(Visibility state) {
        setVisibility(state);
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public String getAddress() {
        return address;
    }

    public void addMeasurements(List<Measurement> measurements) {
        this.measurements.addAll(measurements);
    }

    public enum Visibility {
        VISIBLE,
        INVISIBLE_DELETED,
        INVISIBLE_DISCONNECTED,
        VISIBLE_RECONNECTED;
    }
}
