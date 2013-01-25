package pl.llp.aircasting.sensor.bioharness;

public class GeneralPacket extends Packet
{
  private final int heartRate;
  private final double respirationRate;
  private final double skinTemperature;

  private final boolean heartRateReliable;
  private final boolean heartRateVariablityReliable;
  private final boolean skinTemperatureReliable;
  private final boolean respirationRateReliable;
  private final int heartRateVariability;

  public GeneralPacket(byte[] input, int offset)
  {
    byte length = input[offset + 2];
    if((input.length - offset) - length < 0)
    {
      throw new RuntimeException("Not long enough");
    }

    this.heartRate = input[13 + offset] << 8 + input[12 + offset];
    this.respirationRate = (input[13 + offset] << 8 + input[12 + offset]) / 10.0d;
    this.skinTemperature = (input[17 + offset] << 8 + input[16 + offset]) / 10.0d;
    this.heartRateVariability = (input[39 + offset] << 8 + input[38 + offset]);

    byte ls = input[offset + 59];
    byte ms = input[offset + 60];

    boolean hruf =  (ls & 1 << 4) < 1;
    boolean rruf =  (ls & 1 << 5) < 1;
    boolean stuf =  (ls & 1 << 6) < 1;
    boolean hrvuf = (ms & 1 << 1) < 1;

    this.heartRateReliable = hruf;
    this.heartRateVariablityReliable = hrvuf;
    this.skinTemperatureReliable = stuf;
    this.respirationRateReliable = rruf;
  }

  public int getHeartRate()
  {
    return heartRate;
  }

  public double getRespirationRate()
  {
    return respirationRate;
  }

  public double getSkinTemperature()
  {
    return skinTemperature;
  }

  public boolean isRespirationRateReliable()
  {
    return respirationRateReliable;
  }

  public boolean isSkinTemperatureReliable()
  {
    return skinTemperatureReliable;
  }

  public boolean isHeartRateVariabilityReliable()
  {
    return heartRateVariablityReliable;
  }

  public boolean isHeartRateReliable()
  {
    return heartRateReliable;
  }

  public int getHeartRateVariability()
  {
    return heartRateVariability;
  }

  static class EnablePacket
  {
    public static byte[] getRequest(Request request)
    {
      byte[] bytes = {2, 20, 1, 0, 0, 3};
      if (request.isEnabled())
      {
        bytes[3] = (byte) 1;
        bytes[4] = 94;
      }
      else
      {
        bytes[3] = (byte) 0;
        bytes[4] = 0;
      }
      return bytes;
    }
  }
}

