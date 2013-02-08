package pl.llp.aircasting.sensor.bioharness;

public class SummaryPacket extends Packet
{
  private final int heartRate;
  private final double respirationRate;
  private final double skinTemperature;
  private final int heartRateVariability;

  private final boolean heartRateReliable;
  private final boolean heartRateVariablityReliable;
  private final boolean skinTemperatureReliable;
  private final boolean respirationRateReliable;

  public SummaryPacket(byte[] input, int offset)
  {
    byte dlc = input[offset + 2];
    if(input.length - dlc < 0)
    {
      throw new RuntimeException("Not long enough");
    }

    int heartRateLS = input[offset + 13] & 0xFF;
    int heartRateMS = input[offset + 14] & 0xFF;
    this.heartRate = heartRateLS | (heartRateMS << 8);

    int respirationRateLS = input[(offset + 15)] & 0xFF;
    int respirationRateMS = input[(offset + 16)] & 0xFF;
    this.respirationRate = (respirationRateLS | (respirationRateMS << 8)) / 10.0d;

    int skinTempLS = input[offset + 17] & 0xFF;
    int skinTempMS = input[offset + 18] & 0xFF;
    this.skinTemperature = (skinTempMS << 8 | skinTempLS) / 10.0d;

    int heartRateVariabilityLS = input[offset + 38] & 0xFF;
    int heartRateVariabilityMS = input[offset + 39] & 0xFF;
    this.heartRateVariability = (heartRateVariabilityLS | (heartRateVariabilityMS << 8));

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

  public int getHeartRateVariability()
  {
    return heartRateVariability;
  }

  public boolean isRespirationRateReliable()
  {
    return respirationRateReliable && respirationRate < 6000;
  }

  public boolean isSkinTemperatureReliable()
  {
    return skinTemperatureReliable && skinTemperature > -3000;
  }

  public boolean isHeartRateVariabilityReliable()
  {
    return heartRateVariablityReliable && heartRateVariability < 65000;
  }

  public boolean isHeartRateReliable()
  {
    return heartRateReliable && heartRate < 60000;
  }

  static class EnablePacket
  {
    public static byte[] getRequest(Request request)
    {
      byte[] bytes = { 2, -67, 2, 0, 0, 0, 3 };
      if (request.isEnabled())
      {
        bytes[3] = (byte) 1;
        bytes[5] = -60;
      }
      return bytes;
    }

  }
}
