package pl.llp.aircasting.sensor.bioharness;

public class GeneralPacket extends Packet
{
  private final int heartRate;
  private final double respirationRate;
  private final double skinTemperature;

  public GeneralPacket(byte[] input, int offset)
  {
    byte dlc = input[offset + 2];
    if(input.length - dlc < 0)
    {
      throw new RuntimeException("Not long enough");
    }

    int heartRateLS = input[offset + 12] & 0xFF;
    int heartRateMS = input[offset + 13] & 0xFF;
    this.heartRate = heartRateLS;
    int respirationRateLS = input[14 + offset] & 0xFF;
    int respirationRateMS = input[15 + offset] & 0xFF;
    this.respirationRate = (respirationRateLS | respirationRateMS << 8) / 10.0d;
    int skinTempLS = input[offset + 16] & 0xFF;
    int skinTempMS = input[offset + 17] & 0xFF;
    this.skinTemperature = (skinTempMS << 8 | skinTempLS) / 10.0d;
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

