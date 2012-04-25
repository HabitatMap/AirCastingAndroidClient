package pl.llp.aircasting.sensor.external;

import pl.llp.aircasting.event.sensor.SensorEvent;

public class ExternalSensorParser
{
  /**
   * This has to match what Arduino produces
  * Value;Sensor package name;Sensor name;Type of measurement;Short type of measurement;Unit name;Unit symbol/abbreviation;T1;T2;T3;T4;T5
  **/
   enum Fields
   {

      MEASUREMENT_VALUE,

      SENSOR_PACKAGE_NAME,
      SENSOR_NAME,

      MEASUREMENT_TYPE,
      MEASUREMENT_SHORT_TYPE,
      MEASUREMENT_UNIT,
      MEASUREMENT_SYMBOL,

      THRESHOLD_VERY_LOW,
      THRESHOLD_LOW,
      THRESHOLD_MEDIUM,
      THRESHOLD_HIGH,
      THRESHOLD_VERY_HIGH
   }

    public SensorEvent parse(String string) throws ParseException {
        String[] parts = string.split(";");
        if (parts.length < 10) {
            throw new ParseException("Field count is wrong");
        }

        String packageName = parts[Fields.SENSOR_PACKAGE_NAME.ordinal()];
        String name = parts[Fields.SENSOR_NAME.ordinal()];
        String type = parts[Fields.MEASUREMENT_TYPE.ordinal()];
        String shortType = parts[Fields.MEASUREMENT_SHORT_TYPE.ordinal()];
        String unit = parts[Fields.MEASUREMENT_UNIT.ordinal()];
        String symbol = parts[Fields.MEASUREMENT_SYMBOL.ordinal()];

        double value;
        int veryLow, low, mid, high, veryHigh;
        try {
            veryLow = Integer.parseInt(parts[Fields.THRESHOLD_VERY_LOW.ordinal()]);
            low = Integer.parseInt(parts[Fields.THRESHOLD_LOW.ordinal()]);
            mid = Integer.parseInt(parts[Fields.THRESHOLD_MEDIUM.ordinal()]);
            high = Integer.parseInt(parts[Fields.THRESHOLD_HIGH.ordinal()]);
            veryHigh = Integer.parseInt(parts[Fields.THRESHOLD_VERY_HIGH.ordinal()]);

            value = Double.parseDouble(parts[Fields.MEASUREMENT_VALUE.ordinal()]);
        } catch (NumberFormatException e) {
            throw new ParseException(e);
        }

        return new SensorEvent(packageName, name, type, shortType, unit, symbol, veryLow, low, mid, high, veryHigh, value);
    }
}