package pl.llp.aircasting.sensor.external;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.event.ExternalSensorEvent;
import pl.llp.aircasting.helper.SettingsHelper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(InjectedTestRunner.class)
public class ExternalSensorTest {
    @Inject ExternalSensor sensor;

    @Before
    public void setup() throws IOException {
        sensor.parser = mock(ExternalSensorParser.class);

        sensor.eventBus = mock(EventBus.class);
    }

    @Test
    public void shouldReadInputAndGenerateEvents() throws IOException {
        ExternalSensorEvent event1 = mock(ExternalSensorEvent.class);
        when(sensor.parser.parse("Reading 1")).thenReturn(event1);

        sensor.read("Reading 1");

        verify(sensor.eventBus).post(event1);
    }
}
