package pl.llp.aircasting.sensor.bioharness;

import static pl.llp.aircasting.sensor.bioharness.Builder.builder;

public class ECGPacket
{
  private final int[] samples;
  private final int timeStamp;

  public ECGPacket(byte[] input, int offset)
  {
    Builder reader = builder(input, offset);

    this.timeStamp = reader.intFromBytes().fourth(11).third(10).second(9).first(8).value();
    NeedsCount builder = reader.packed10byteInts(12);
    samples = builder.samples(63).ints();
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
