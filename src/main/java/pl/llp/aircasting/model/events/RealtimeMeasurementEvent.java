package pl.llp.aircasting.model.events;

import pl.llp.aircasting.model.RealtimeMeasurement;

public class RealtimeMeasurementEvent
{
  private RealtimeMeasurement realtimeMeasurement;

  public RealtimeMeasurementEvent(RealtimeMeasurement realtimeMeasurement)
  {
    this.realtimeMeasurement = realtimeMeasurement;
  }

  public RealtimeMeasurement getRealtimeMeasurement()
  {
    return realtimeMeasurement;
  }
}
