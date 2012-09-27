package pl.llp.aircasting.activity.extsens;

import pl.llp.aircasting.InjectedTestRunner;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.common.collect.Sets.newHashSet;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(InjectedTestRunner.class)
public class PairedSensorAdapterTest
{
  PairedSensorAdapter adapter;

  BluetoothDevice DEVICE;

  @Before
  public void setUp() throws Exception
  {
    Context context = mock(Context.class);
    adapter = new PairedSensorAdapter(context);
    adapter = spy(adapter);

    DEVICE = mock(BluetoothDevice.class);
    when(DEVICE.getAddress()).thenReturn("1048576");
    when(DEVICE.getName()).thenReturn("Bluetorch");

    when(adapter.getBondedDevices()).thenReturn(newHashSet(DEVICE));
  }

  @Test
  public void should_load_paired_devices() throws Exception
  {
    // given

    // when
    adapter.updatePairedDevices();

    // then
    assertEquals(1, adapter.data.size());
  }


  @Test
  public void multiple_updates_should_not_multiply_devices() throws Exception
  {
    // given

    // when
    adapter.updatePairedDevices();
    adapter.updatePairedDevices();

    // then
    assertEquals(1, adapter.data.size());
  }

  @Test
  public void connecting_with_a_device_should_hide_from_paired() throws Exception
  {
    // given
    adapter.updatePairedDevices();
    assertEquals(1, adapter.data.size());

    // when
    adapter.markAsConnected(0);

    // then
    assertEquals(0, adapter.data.size());
  }
}
