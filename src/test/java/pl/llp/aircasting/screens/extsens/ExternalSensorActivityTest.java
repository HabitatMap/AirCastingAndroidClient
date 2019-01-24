package pl.llp.aircasting.screens.extsens;

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.R;
import pl.llp.aircasting.screens.common.helpers.SettingsHelper;
import pl.llp.aircasting.sensor.common.ExternalSensorDescriptor;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import com.google.inject.Inject;
import com.xtremelabs.robolectric.shadows.ShadowToast;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(InjectedTestRunner.class)
public class ExternalSensorActivityTest
{
  @Inject ExternalSensorActivity activity;
  private BluetoothDevice ANY_DEVICE;

  private String ANY_ADDRESS = "any";
  private ExternalSensorDescriptor ANY_DESCRIPTOR = new ExternalSensorDescriptor("", ANY_ADDRESS, "");

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
    // given
    activity.bluetoothAdapter = null;

    // when
    activity.onResume();

    // then
    assertThat(activity.isFinishing(), equalTo(true));
    assertThat(ShadowToast.getTextOfLatestToast(), equalTo(activity.getString(R.string.bluetooth_not_supported)));
  }

  @Test
  public void pressing_button_should_openBluetoothSetting() throws Exception
  {
    // given
    activity.context = mock(Context.class);
    activity.onResume();

    // when
    activity.openBluetoothButton.performClick();

    // then
    verify(activity.context).startActivity(Matchers.<Intent>anyObject());
  }
}
