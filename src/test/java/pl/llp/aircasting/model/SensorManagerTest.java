package pl.llp.aircasting.model;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.event.ui.ToggleStreamEvent;
import pl.llp.aircasting.event.ui.ViewStreamEvent;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@RunWith(InjectedTestRunner.class)
public class SensorManagerTest {
    @Inject SensorManager manager;

    @Before
    public void setup() {
        manager.eventBus = mock(EventBus.class);
    }

    @Test
    public void shouldEnableAllSensorsByDefault() {
        assertThat(manager.isEnabled("LHC"), equalTo(true));
    }

    @Test
    public void shouldStoreInformationAboutDisabledSensors() {
        manager.onEvent(new ToggleStreamEvent("LHC"));

        assertThat(manager.isEnabled("LHC"), equalTo(false));
    }

    @Test
    public void shouldReEnableSensors() {
        manager.onEvent(new ToggleStreamEvent("LHC"));
        manager.onEvent(new ToggleStreamEvent("LHC"));

        assertThat(manager.isEnabled("LHC"), equalTo(true));
    }

    @Test
    public void phoneMicrophoneShouldBeVisibleByDefault() {
        assertThat(manager.getVisibleSensor(), equalTo(SimpleAudioReader.SENSOR_NAME));
    }

    @Test
    public void shouldStoreInformationAboutCurrentVisibleSensor() {
        manager.onEvent(new ViewStreamEvent("LHC"));

        assertThat(manager.getVisibleSensor(), equalTo("LHC"));
    }
}
