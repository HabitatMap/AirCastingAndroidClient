package pl.llp.aircasting.event;

public class ExternalSensorEvent extends AirCastingEvent {
    private String name;
    private String unit;
    private String symbol;
    private double value;

    public String getName() {
        return name;
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

    public ExternalSensorEvent(String name, String unit, String symbol, double value) {
        this.name = name;
        this.unit = unit;
        this.symbol = symbol;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExternalSensorEvent event = (ExternalSensorEvent) o;

        if (Double.compare(event.value, value) != 0) return false;
        if (name != null ? !name.equals(event.name) : event.name != null) return false;
        if (symbol != null ? !symbol.equals(event.symbol) : event.symbol != null) return false;
        if (unit != null ? !unit.equals(event.unit) : event.unit != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = name != null ? name.hashCode() : 0;
        result = 31 * result + (unit != null ? unit.hashCode() : 0);
        result = 31 * result + (symbol != null ? symbol.hashCode() : 0);
        temp = value != +0.0d ? Double.doubleToLongBits(value) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "ExternalSensorEvent{" +
                "name='" + name + '\'' +
                ", unit='" + unit + '\'' +
                ", symbol='" + symbol + '\'' +
                ", value=" + value +
                '}';
    }
}
