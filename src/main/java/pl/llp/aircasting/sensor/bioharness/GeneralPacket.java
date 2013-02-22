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

    Builder builder = new Builder(input, offset);
    this.heartRate = builder.fromBytes().second(13).first(12).value();
    this.respirationRate = builder.fromBytes().second(15).first(14).value();
    this.skinTemperature = builder.fromBytes().second(17).first(16).value();
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
}

