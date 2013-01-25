package pl.llp.aircasting.sensor.bioharness;

abstract class Packet
{
  enum Request
  {
    ENABLED(true),
    DISABLED(false);

    private boolean enabled;

    Request(boolean enabled)
    {
      this.enabled = enabled;
    }

    public boolean isEnabled()
    {
      return enabled;
    }
  }
}
