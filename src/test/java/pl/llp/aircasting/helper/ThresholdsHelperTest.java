package pl.llp.aircasting.helper;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.MeasurementLevel;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(InjectedTestRunner.class)
public class ThresholdsHelperTest {
    @Inject ThresholdsHelper thresholdsHelper;

    @Before
    public void setup() {
        thresholdsHelper.settingsHelper = mock(SettingsHelper.class);
        thresholdsHelper.eventBus = mock(EventBus.class);

        thresholdsHelper.onEvent(new SensorEvent("LHC", "Hadrons", "number", "#", 0, 10, 20, 30, 40, 10));

        when(thresholdsHelper.settingsHelper.hasThresholds(SimpleAudioReader.SENSOR_NAME)).thenReturn(true);
    }

    @Test
    public void shouldServeVeryLowFromSettings() {
        when(thresholdsHelper.settingsHelper.getThreshold(MeasurementLevel.VERY_LOW)).thenReturn(123);

        assertThat(thresholdsHelper.getThreshold(SimpleAudioReader.SENSOR_NAME, MeasurementLevel.VERY_LOW), equalTo(123));
    }

    @Test
    public void shouldServeLowFromSettings() {
        when(thresholdsHelper.settingsHelper.getThreshold(MeasurementLevel.LOW)).thenReturn(12);

        assertThat(thresholdsHelper.getThreshold(SimpleAudioReader.SENSOR_NAME, MeasurementLevel.LOW), equalTo(12));
    }

    @Test
    public void shouldServeMidFromSettings() {
        when(thresholdsHelper.settingsHelper.getThreshold(MeasurementLevel.MID)).thenReturn(100);

        assertThat(thresholdsHelper.getThreshold(SimpleAudioReader.SENSOR_NAME, MeasurementLevel.MID), equalTo(100));
    }

    @Test
    public void shouldServeHighFromSettings() {
        when(thresholdsHelper.settingsHelper.getThreshold(MeasurementLevel.HIGH)).thenReturn(500);

        assertThat(thresholdsHelper.getThreshold(SimpleAudioReader.SENSOR_NAME, MeasurementLevel.HIGH), equalTo(500));
    }

    @Test
    public void shouldServeVeryHighFromSettings() {
        when(thresholdsHelper.settingsHelper.getThreshold(MeasurementLevel.VERY_HIGH)).thenReturn(100000);

        assertThat(thresholdsHelper.getThreshold(SimpleAudioReader.SENSOR_NAME, MeasurementLevel.VERY_HIGH), equalTo(100000));
    }

    @Test
    public void shouldRememberVeryLowFromSensorEvents() {
        assertThat(thresholdsHelper.getThreshold("LHC", MeasurementLevel.VERY_LOW), equalTo(0));
    }

    @Test
    public void shouldRememberLowFromSensorEvents() {
        assertThat(thresholdsHelper.getThreshold("LHC", MeasurementLevel.LOW), equalTo(10));
    }

    @Test
    public void shouldRememberMidFromSensorEvents() {
        assertThat(thresholdsHelper.getThreshold("LHC", MeasurementLevel.MID), equalTo(20));
    }

    @Test
    public void shouldRememberHighFromSensorEvents() {
        assertThat(thresholdsHelper.getThreshold("LHC", MeasurementLevel.HIGH), equalTo(30));
    }

    @Test
    public void shouldRememberVeryHighFromSensorEvents() {
        assertThat(thresholdsHelper.getThreshold("LHC", MeasurementLevel.VERY_HIGH), equalTo(40));
    }

    @Test
    public void settingsShouldOverrideDefaults() {
        thresholdsHelper.onEvent(new SensorEvent(SimpleAudioReader.SENSOR_NAME, "", "", "", 0, 10, 20, 30, 40, 10));
        when(thresholdsHelper.settingsHelper.getThreshold(Matchers.<MeasurementLevel>any())).thenReturn(100);
        
        assertThat(thresholdsHelper.getThreshold(SimpleAudioReader.SENSOR_NAME, MeasurementLevel.LOW), equalTo(100));
        assertThat(thresholdsHelper.getThreshold(SimpleAudioReader.SENSOR_NAME, MeasurementLevel.VERY_LOW), equalTo(100));
        assertThat(thresholdsHelper.getThreshold(SimpleAudioReader.SENSOR_NAME, MeasurementLevel.MID), equalTo(100));
        assertThat(thresholdsHelper.getThreshold(SimpleAudioReader.SENSOR_NAME, MeasurementLevel.HIGH), equalTo(100));
        assertThat(thresholdsHelper.getThreshold(SimpleAudioReader.SENSOR_NAME, MeasurementLevel.VERY_HIGH), equalTo(100));
    }
}