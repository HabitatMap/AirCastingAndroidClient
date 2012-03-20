package pl.llp.aircasting.sensor.external;

import pl.llp.aircasting.event.ExternalSensorEvent;

public class ExternalSensorParser {
    public ExternalSensorEvent parse(String string) throws ParseException {
        String[] parts = string.split(";");
        if (parts.length < 4) {
            throw new ParseException();
        }

        String name = parts[0];
        String type = parts[1];
        String unit = parts[2];
        String symbol = parts[3];

        double value;
        try {
            value = Double.parseDouble(parts[4]);
        } catch (NumberFormatException e) {
            throw new ParseException(e);
        }

        return new ExternalSensorEvent(name, type, unit, symbol, value);
    }
}