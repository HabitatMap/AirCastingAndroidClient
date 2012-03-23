package pl.llp.aircasting.sensor.external;

import pl.llp.aircasting.event.sensor.SensorEvent;

public class ExternalSensorParser {
    public SensorEvent parse(String string) throws ParseException {
        String[] parts = string.split(";");
        if (parts.length < 4) {
            throw new ParseException();
        }

        String name = parts[0];
        String type = parts[1];
        String unit = parts[2];
        String symbol = parts[3];

        double value;
        int veryLow, low, mid, high, veryHigh;
        try {
            veryLow = Integer.parseInt(parts[4]);
            low = Integer.parseInt(parts[5]);
            mid = Integer.parseInt(parts[6]);
            high = Integer.parseInt(parts[7]);
            veryHigh = Integer.parseInt(parts[8]);

            value = Double.parseDouble(parts[9]);
        } catch (NumberFormatException e) {
            throw new ParseException(e);
        }

        return new SensorEvent(name, type, unit, symbol, veryLow, low, mid, high, veryHigh, value);
    }
}