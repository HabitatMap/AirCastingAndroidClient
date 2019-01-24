package pl.llp.aircasting.event.sensor;

import pl.llp.aircasting.sensor.common.ExternalSensorDescriptor;

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
