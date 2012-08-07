package pl.llp.aircasting.model;

import pl.llp.aircasting.MeasurementLevel;

/**
 * Created by ags on 06/08/12 at 16:01
 */
public class MeasurementLevelEvent
{
  public MeasurementLevelEvent(Sensor sensor, MeasurementLevel level)
  {
    this.sensor = sensor;
    this.level = level;
  }

  private Sensor sensor;
  private MeasurementLevel level;

  public Sensor getSensor()
  {
    return sensor;
  }

  public MeasurementLevel getLevel()
  {
    return level;
  }
}
