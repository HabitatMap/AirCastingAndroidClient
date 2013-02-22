package pl.llp.aircasting.sensor.bioharness;

public class RtoRPacket extends Packet
{
  private final int timeStamp;
  int[] samples = new int[18];

  public RtoRPacket(byte[] input, int offset)
  {
    Builder builder = new Builder(input, offset);

    this.timeStamp = builder.fromBytes().fourth(11).third(10).second(9).first(8).value();
    for (int i = 0; i < samples.length; i++)
    {
      int index = 12 + i * 2;
      samples[i] = builder.signedFromTwoBytes().second(index + 1).first(index).value();
    }
  }

  public int[] getSamples()
  {
    return samples;
  }

  public int getTimeStamp()
  {
    return timeStamp;
  }
}
