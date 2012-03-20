package pl.llp.aircasting.event;

public class ExternalSensorEvent extends AirCastingEvent {
    private String sensorName;
    private String unit;
    private String symbol;
    private String measurementType;
    private double value;

    public String getSensorName() {
        return sensorName;
    }

    public String getUnit() {
        return unit;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getValue() {
        return value;
    }

    public String getMeasurementType() {
        return measurementType;
    }

    public ExternalSensorEvent(String sensorName, String measurementType, String unit, String symbol, double value) {
        this.sensorName = sensorName;
        this.measurementType = measurementType;
        this.unit = unit;
        this.symbol = symbol;
        this.value = value;
    }

    @Override
    public String toString() {
        return "ExternalSensorEvent{" +
                "sensorName='" + sensorName + '\'' +
                ", unit='" + unit + '\'' +
                ", symbol='" + symbol + '\'' +
                ", measurementType='" + measurementType + '\'' +
                ", value=" + value +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExternalSensorEvent event = (ExternalSensorEvent) o;

        if (Double.compare(event.value, value) != 0) return false;
        if (measurementType != null ? !measurementType.equals(event.measurementType) : event.measurementType != null)
            return false;
        if (sensorName != null ? !sensorName.equals(event.sensorName) : event.sensorName != null) return false;
        if (symbol != null ? !symbol.equals(event.symbol) : event.symbol != null) return false;
        if (unit != null ? !unit.equals(event.unit) : event.unit != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = sensorName != null ? sensorName.hashCode() : 0;
        result = 31 * result + (unit != null ? unit.hashCode() : 0);
        result = 31 * result + (symbol != null ? symbol.hashCode() : 0);
        result = 31 * result + (measurementType != null ? measurementType.hashCode() : 0);
        temp = value != +0.0d ? Double.doubleToLongBits(value) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
