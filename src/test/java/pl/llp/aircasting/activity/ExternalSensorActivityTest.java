package pl.llp.aircasting.activity;

import com.google.inject.Inject;
import com.xtremelabs.robolectric.shadows.ShadowToast;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.R;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(InjectedTestRunner.class)
public class ExternalSensorActivityTest {
    @Inject ExternalSensorActivity activity;

    @Before
    public void setup(){
        activity.onCreate(null);
    }

    @Test
    public void shouldJustDisplayAMessageIfBluetoothNotSupported() {
        activity.bluetoothAdapter = null;
        
        activity.onResume();

        assertThat(activity.isFinishing(), equalTo(true));
        assertThat(ShadowToast.getTextOfLatestToast(), equalTo(activity.getString(R.string.bluetooth_not_supported)));
    }
}
