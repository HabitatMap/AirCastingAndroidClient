package pl.llp.aircasting.sensor.bioharness;

public class RtoRPacket extends Packet
{
  int[] samples = new int[18];

  public RtoRPacket(byte[] input, int offset)
  {
    Builder builder = new Builder(input, offset);
    for (int i = 0; i < samples.length; i++)
    {
      int index = i * 2;
      samples[i] = builder.intFromBytes().second(index + 1).first(index).value();
    }
  }

  public int[] getSamples()
  {
    return samples;
  }
}
