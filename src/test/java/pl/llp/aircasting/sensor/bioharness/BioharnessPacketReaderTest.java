package pl.llp.aircasting.sensor.bioharness;

import com.google.common.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Created by ags on 02/27/13 at 23:10
 */
public class BioharnessPacketReaderTest
{
  RtoRPacket packet;
  BioharnessPacketReader reader;

  @Before
  public void setUp() throws Exception
  {
    EventBus eventBus = new EventBus();
    BioharnessPacketReader object = new BioharnessPacketReader(eventBus);
    reader = spy(object);
    packet = mock(RtoRPacket.class);
  }

  @Test
  public void should_filter_out_repeating_values() throws Exception
  {
      // given
      when(packet.getSamples()).thenReturn(new int[]{1, 2, 2, 3, 3, 3});

      // when
      reader.postRtoR(packet, 1);

      // then
      verify(reader, times(3)).postRtoREvent(anyInt(), anyLong());
  }
}
