package pl.llp.aircasting.activity.events;

import pl.llp.aircasting.sensor.ExternalSensorDescriptor;

public class SensorStoppedEvent
{
  private ExternalSensorDescriptor descriptor;

  public SensorStoppedEvent(ExternalSensorDescriptor descriptor)
  {
    this.descriptor = descriptor;
  }

  public ExternalSensorDescriptor getDescriptor()
  {
    return descriptor;
  }
}
