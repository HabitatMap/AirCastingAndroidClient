package pl.llp.aircasting.model;

import com.google.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.event.ui.ToggleStreamEvent;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(InjectedTestRunner.class)
public class SensorManagerTest {
    @Inject SensorManager manager;

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
}
