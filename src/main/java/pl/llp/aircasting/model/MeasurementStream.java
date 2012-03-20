package pl.llp.aircasting.model;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class MeasurementStream {
    private List<Measurement> measurements = newArrayList();
    private String sensorName;
    private String measurementType;
    private String unit;
    private String symbol;

    private double sum;
    private double peak = Double.NEGATIVE_INFINITY;

    public MeasurementStream(String sensorName, String measurementType, String unit, String symbol) {
        this.sensorName = sensorName;
        this.measurementType = measurementType;
        this.unit = unit;
        this.symbol = symbol;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public void add(Measurement measurement) {
        measurements.add(measurement);

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

    public double getAvg() {
        return sum / (measurements.isEmpty() ? 1 : measurements.size());
    }

    public double getPeak() {
        return peak;
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
}
