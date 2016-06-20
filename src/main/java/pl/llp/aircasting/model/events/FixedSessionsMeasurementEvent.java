package pl.llp.aircasting.model.events;

import pl.llp.aircasting.model.FixedSessionsMeasurement;

public class FixedSessionsMeasurementEvent
{
  private FixedSessionsMeasurement fixedSessionsMeasurement;

  public FixedSessionsMeasurementEvent(FixedSessionsMeasurement fixedSessionsMeasurement)
  {
    this.fixedSessionsMeasurement = fixedSessionsMeasurement;
  }

  public FixedSessionsMeasurement getFixedSessionsMeasurement()
  {
    return fixedSessionsMeasurement;
  }
}
