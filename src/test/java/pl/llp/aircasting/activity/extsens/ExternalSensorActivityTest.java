package pl.llp.aircasting.activity.extsens;

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.R;
import pl.llp.aircasting.helper.SettingsHelper;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import com.google.inject.Inject;
import com.xtremelabs.robolectric.shadows.ShadowToast;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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
    when(mockHelper.getSensorAddress()).thenReturn(ANY_ADDRESS);
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
    assertEquals(0, activity.connectedSensorAdapter.devices.size());
    assertEquals(1, activity.availableSensorAdapter.devices.size());
  }

  @Test
  public void should_addKnownDevices() throws Exception
  {
      // given
    activity.settingsHelper = mockHelper;

      // when
    activity.showPreviouslyConnectedSensor();

      // then
    assertEquals(1, activity.connectedSensorAdapter.data.size());
  }

  @Test
  public void should_notAddKnownDeviceAsAvailable() throws Exception
  {
    // given
    activity.settingsHelper = mockHelper;
    when(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)).thenReturn(ANY_DEVICE);
    activity.showPreviouslyConnectedSensor();

    // when
    activity.receiver.onReceive(context, intent);

    // then
    assertEquals(0, activity.availableSensorAdapter.data.size());
  }
}
