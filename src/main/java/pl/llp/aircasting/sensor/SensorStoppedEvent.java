package pl.llp.aircasting.sensor;

import pl.llp.aircasting.model.ExternalSensorDescriptor;

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
