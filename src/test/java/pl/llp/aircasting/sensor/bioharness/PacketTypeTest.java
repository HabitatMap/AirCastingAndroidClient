package pl.llp.aircasting.sensor.bioharness;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class PacketTypeTest
{
  @Test
  public void should_decode_packets_it_knows() throws Exception
  {
      // given
    PacketType[] values = PacketType.values();
    for (PacketType value : values)
    {
      // when
      PacketType decode = PacketType.decode(value.messageId);

      // then
      assertThat(value).isEqualTo(decode);
    }
  }
}
