package pl.llp.aircasting.helper;

import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.MeasurementLevel;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(InjectedTestRunner.class)
public class ThresholdsHelperTest {
    @Inject ThresholdsHelper thresholdsHelper;
    private Sensor sensor;

    @Before
    public void setup() {
        thresholdsHelper.settingsHelper = mock(SettingsHelper.class);
        when(thresholdsHelper.settingsHelper.hasThresholds(SimpleAudioReader.getSensor())).thenReturn(true);
        
        sensor = mock(Sensor.class);
        when(sensor.getThreshold(MeasurementLevel.VERY_LOW)).thenReturn(0);
        when(sensor.getThreshold(MeasurementLevel.LOW)).thenReturn(10);
        when(sensor.getThreshold(MeasurementLevel.MID)).thenReturn(20);
        when(sensor.getThreshold(MeasurementLevel.HIGH)).thenReturn(30);
        when(sensor.getThreshold(MeasurementLevel.VERY_HIGH)).thenReturn(40);
    }

    @Test
    public void shouldServeVeryLowFromSettings() {
        when(thresholdsHelper.settingsHelper.getThreshold(MeasurementLevel.VERY_LOW)).thenReturn(123);

        assertThat(thresholdsHelper.getThreshold(SimpleAudioReader.getSensor(), MeasurementLevel.VERY_LOW), equalTo(123));
    }

    @Test
    public void shouldServeLowFromSettings() {
        when(thresholdsHelper.settingsHelper.getThreshold(MeasurementLevel.LOW)).thenReturn(12);

        assertThat(thresholdsHelper.getThreshold(SimpleAudioReader.getSensor(), MeasurementLevel.LOW), equalTo(12));
    }

    @Test
    public void shouldServeMidFromSettings() {
        when(thresholdsHelper.settingsHelper.getThreshold(MeasurementLevel.MID)).thenReturn(100);

        assertThat(thresholdsHelper.getThreshold(SimpleAudioReader.getSensor(), MeasurementLevel.MID), equalTo(100));
    }

    @Test
    public void shouldServeHighFromSettings() {
        when(thresholdsHelper.settingsHelper.getThreshold(MeasurementLevel.HIGH)).thenReturn(500);

        assertThat(thresholdsHelper.getThreshold(SimpleAudioReader.getSensor(), MeasurementLevel.HIGH), equalTo(500));
    }

    @Test
    public void shouldServeVeryHighFromSettings() {
        when(thresholdsHelper.settingsHelper.getThreshold(MeasurementLevel.VERY_HIGH)).thenReturn(100000);

        assertThat(thresholdsHelper.getThreshold(SimpleAudioReader.getSensor(), MeasurementLevel.VERY_HIGH), equalTo(100000));
    }

    @Test
    public void shouldGetVeryLowFromSensor() {
        assertThat(thresholdsHelper.getThreshold(sensor, MeasurementLevel.VERY_LOW), equalTo(0));
    }

    @Test
    public void shouldGetLowFromSensor() {
        assertThat(thresholdsHelper.getThreshold(sensor, MeasurementLevel.LOW), equalTo(10));
    }

    @Test
    public void shouldGetMidFromSensor() {
        assertThat(thresholdsHelper.getThreshold(sensor, MeasurementLevel.MID), equalTo(20));
    }

    @Test
    public void shouldGetHighFromSensor() {
        assertThat(thresholdsHelper.getThreshold(sensor, MeasurementLevel.HIGH), equalTo(30));
    }

    @Test
    public void shouldGetVeryHighFromSensor() {
        assertThat(thresholdsHelper.getThreshold(sensor, MeasurementLevel.VERY_HIGH), equalTo(40));
    }

    @Test
    public void settingsShouldOverrideDefaults() {
        when(thresholdsHelper.settingsHelper.getThreshold(Matchers.<MeasurementLevel>any())).thenReturn(100);

        assertThat(thresholdsHelper.getThreshold(SimpleAudioReader.getSensor(), MeasurementLevel.LOW), equalTo(100));
        assertThat(thresholdsHelper.getThreshold(SimpleAudioReader.getSensor(), MeasurementLevel.VERY_LOW), equalTo(100));
        assertThat(thresholdsHelper.getThreshold(SimpleAudioReader.getSensor(), MeasurementLevel.MID), equalTo(100));
        assertThat(thresholdsHelper.getThreshold(SimpleAudioReader.getSensor(), MeasurementLevel.HIGH), equalTo(100));
        assertThat(thresholdsHelper.getThreshold(SimpleAudioReader.getSensor(), MeasurementLevel.VERY_HIGH), equalTo(100));
    }
}