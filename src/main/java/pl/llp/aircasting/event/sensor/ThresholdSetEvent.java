package pl.llp.aircasting.event.sensor;

import pl.llp.aircasting.model.MeasurementLevel;
import pl.llp.aircasting.model.Sensor;

/**
 * Created by ags on 30/08/12 at 16:54
 */
public class ThresholdSetEvent
{
  private final Sensor sensor;
  private final MeasurementLevel level;
  private final int value;

  public ThresholdSetEvent(Sensor sensor, MeasurementLevel level, int value)
  {
    this.sensor = sensor;
    this.level = level;
    this.value = value;
  }

  public Sensor getSensor()
  {
    return sensor;
  }

  public MeasurementLevel getLevel()
  {
    return level;
  }

  public int getValue()
  {
    return value;
  }
}
