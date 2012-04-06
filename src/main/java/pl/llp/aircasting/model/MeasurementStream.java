package pl.llp.aircasting.model;

import pl.llp.aircasting.MeasurementLevel;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.helper.SoundHelper;

import java.io.Serializable;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class MeasurementStream implements Serializable {
    private transient List<Measurement> measurements = newArrayList();

    private long id;

    private long sessionId;

    private String sensorName;

    private String measurementType;

    private String unit;

    private String symbol;

    private Double sum = 0.0;

    private Double avg;

    private Double peak = Double.NEGATIVE_INFINITY;

    private int thresholdVeryHigh;

    private int thresholdVeryLow;

    private int thresholdLow;

    private int thresholdMedium;

    private int thresholdHigh;

    private transient boolean avgDirty = true;

    public MeasurementStream() {
    }

    public MeasurementStream(Sensor sensor) {
        this(sensor.getSensorName(),
             sensor.getMeasurementType(),
             sensor.getUnit(),
             sensor.getSymbol(),
             sensor.getThreshold(MeasurementLevel.VERY_LOW),
             sensor.getThreshold(MeasurementLevel.MID),
             sensor.getThreshold(MeasurementLevel.LOW),
             sensor.getThreshold(MeasurementLevel.HIGH),
             sensor.getThreshold(MeasurementLevel.VERY_HIGH));
    }

    public MeasurementStream(SensorEvent evt) {
        this(evt.getSensorName(), evt.getMeasurementType(), evt.getUnit(), evt.getSymbol(),
             evt.getVeryLow(),
             evt.getLow(),
             evt.getMid(),
             evt.getHigh(),
             evt.getVeryHigh());
    }

    public MeasurementStream(String sensor, String type, String unit, String symbol,
                             int thresholdVeryLow, int thresholdLow,
                             int thresholdMedium,
                             int thresholdHigh, int thresholdVeryHigh
                            ) {
        this.sensorName = sensor;
        this.measurementType = type;
        this.unit = unit;
        this.symbol = symbol;

        this.thresholdVeryLow = thresholdVeryLow;
        this.thresholdLow = thresholdLow;
        this.thresholdMedium = thresholdMedium;
        this.thresholdHigh = thresholdHigh;
        this.thresholdVeryHigh = thresholdVeryHigh;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public void add(Measurement measurement) {
        measurements.add(measurement);
        avgDirty = true;

        double value = measurement.getValue();
        sum += value;
        if (value > peak) {
            peak = value;
        }
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

    private double calculatePeak() {
        double newPeak = SoundHelper.TOTALLY_QUIET;
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
        if (avgDirty) {
            avg = calculateAvg();
            avgDirty = false;
        }
        return avg;
    }

    private double calculateAvg() {
        double sum = getSum();

        return sum / (measurements.isEmpty() ? 1 : measurements.size());
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

        avgDirty = true;
    }

    public Double getSum() {
        return sum;
    }

    public void setAvg(double avg) {
        this.avg = avg;
        avgDirty = false;
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
}
