package pl.llp.aircasting.sensor.external;

import pl.llp.aircasting.event.sensor.SensorEvent;

public class ExternalSensorParser {
    public SensorEvent parse(String string) throws ParseException {
        String[] parts = string.split(";");
        if (parts.length < 10) {
            throw new ParseException("Field count is wrong");
        }

        String name = parts[0];
        String type = parts[1];
        String shortType = parts[2];
        String unit = parts[3];
        String symbol = parts[4];

        double value;
        int veryLow, low, mid, high, veryHigh;
        try {
            veryLow = Integer.parseInt(parts[5]);
            low = Integer.parseInt(parts[6]);
            mid = Integer.parseInt(parts[7]);
            high = Integer.parseInt(parts[8]);
            veryHigh = Integer.parseInt(parts[9]);

            value = Double.parseDouble(parts[10]);
        } catch (NumberFormatException e) {
            throw new ParseException(e);
        }

        return new SensorEvent(name, type, shortType, unit, symbol, veryLow, low, mid, high, veryHigh, value);
    }
}