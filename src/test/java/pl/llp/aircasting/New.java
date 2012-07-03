package pl.llp.aircasting;

import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.Note;

import java.util.Date;

/**
 * Created by ags on 29/06/12 at 18:32
 */
public class New
{
  public static SensorEvent sensorEvent(String sensorName, double value)
  {
    return new SensorEvent("CERN", sensorName, "Higgs boson", "HB", "number", "#", 1, 2, 3, 4, 5, value);
  }

  public static Note note(String text)
  {
    return new Note(new Date(), text, null, null);
  }

  public static Measurement measurement(int value)
  {
    return new Measurement(0, 0, value);
  }
}
