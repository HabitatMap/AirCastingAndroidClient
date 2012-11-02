package pl.llp.aircasting.sensor;

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
