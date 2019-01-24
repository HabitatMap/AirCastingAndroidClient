package pl.llp.aircasting.sensor.bioharness;

public class TimestampTracker
{
  boolean initialized;

  private long externalBase;
  private long internalBase;
  private long delta;

  public long getLocalTimestamp(long timeStamp)
  {
    if(!initialized)
    {
      externalBase = timeStamp;
      internalBase = System.currentTimeMillis();
      delta = internalBase - externalBase;
      initialized = true;
    }

    return internalBase + (timeStamp - externalBase);
  }
}
