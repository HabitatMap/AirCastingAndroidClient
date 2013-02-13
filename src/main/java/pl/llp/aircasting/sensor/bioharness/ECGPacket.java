package pl.llp.aircasting.sensor.bioharness;

import static pl.llp.aircasting.sensor.bioharness.Builder.builder;

public class ECGPacket
{
  private final int[] samples;

  public ECGPacket(byte[] input, int offset)
  {
    NeedsCount builder = builder(input, offset).packed10byteInts(12);
    samples = builder.samples(63).ints();
  }

  public int[] getSamples()
  {
    return samples;
  }
}
