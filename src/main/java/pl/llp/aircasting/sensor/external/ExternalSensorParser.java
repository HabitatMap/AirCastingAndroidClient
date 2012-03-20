package pl.llp.aircasting.sensor.external;

import pl.llp.aircasting.event.ExternalSensorEvent;

public class ExternalSensorParser {
    public ExternalSensorEvent parse(String string) throws ParseException {
        String[] parts = string.split(";");
        if (parts.length < 4) {
            throw new ParseException();
        }

        String name = parts[0];
        String unit = parts[1];
        String symbol = parts[2];

        double value;
        try {
            value = Double.parseDouble(parts[3]);
        } catch (NumberFormatException e) {
            throw new ParseException(e);
        }

        return new ExternalSensorEvent(name, unit, symbol, value);
    }
}