package pl.llp.aircasting;

import pl.llp.aircasting.event.sensor.SensorEvent;

/**
 * Created by ags on 29/06/12 at 18:32
 */
public class New
{
  public static SensorEvent sensorEvent(String sensorName, double value)
  {
    return new SensorEvent("CERN", sensorName, "Higgs boson", "HB", "number", "#", 1, 2, 3, 4, 5, value);
  }
}
