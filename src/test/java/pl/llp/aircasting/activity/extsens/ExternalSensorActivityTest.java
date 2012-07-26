package pl.llp.aircasting.activity.extsens;

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.R;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.ExternalSensorDescriptor;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import com.google.inject.Inject;
import com.xtremelabs.robolectric.shadows.ShadowToast;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(InjectedTestRunner.class)
public class ExternalSensorActivityTest
{
  @Inject
  ExternalSensorActivity activity;
  private BluetoothDevice ANY_DEVICE;

  Context context = null;

  private String ANY_ADDRESS = "any";
  private ExternalSensorDescriptor ANY_DESCRIPTOR = new ExternalSensorDescriptor("", ANY_ADDRESS);

  Intent intent;
  private SettingsHelper mockHelper;

  @Before
  public void setup()
  {
    activity.onCreate(null);
    intent = mock(Intent.class);

    ANY_DEVICE = mock(BluetoothDevice.class);
    when(ANY_DEVICE.getAddress()).thenReturn(ANY_ADDRESS);

    mockHelper = mock(SettingsHelper.class);
    when(mockHelper.knownSensors()).thenReturn(newArrayList(ANY_DESCRIPTOR));
  }

  @Test
  public void shouldJustDisplayAMessageIfBluetoothNotSupported()
  {
    activity.bluetoothAdapter = null;

    activity.onResume();

    assertThat(activity.isFinishing(), equalTo(true));
    assertThat(ShadowToast.getTextOfLatestToast(), equalTo(activity.getString(R.string.bluetooth_not_supported)));
  }

  @Test
  public void should_addUnknownDeviceAsAvailable() throws Exception
  {
    // given
    when(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)).thenReturn(ANY_DEVICE);

    // when
    activity.receiver.onReceive(context, intent);

    // then
    assertEquals(0, activity.knownSensorAdapter.getCount());
    assertEquals(1, activity.availableSensorAdapter.devices.size());
  }
}
