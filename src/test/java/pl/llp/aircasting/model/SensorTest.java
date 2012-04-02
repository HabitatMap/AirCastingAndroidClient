package pl.llp.aircasting.model;

import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.MeasurementLevel;
import pl.llp.aircasting.event.sensor.SensorEvent;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(InjectedTestRunner.class)
public class SensorTest {
    @Inject SensorEvent event;
    private Sensor sensor;

    @Before
    public void setup() {
        sensor = new Sensor(event);
    }

    @Test
    public void shouldProvideTooHighThreshold() {
        assertThat(sensor.getThreshold(MeasurementLevel.VERY_HIGH), equalTo(event.getVeryHigh()));
    }

    @Test
    public void shouldProvideHighThreshold() {
        assertThat(sensor.getThreshold(MeasurementLevel.HIGH), equalTo(event.getHigh()));
    }

    @Test
    public void shouldProvideMidThreshold() {
        assertThat(sensor.getThreshold(MeasurementLevel.MID), equalTo(event.getMid()));
    }

    @Test
    public void shouldProvideLowThreshold() {
        assertThat(sensor.getThreshold(MeasurementLevel.LOW), equalTo(event.getLow()));
    }

    @Test
    public void shouldProvideVeryLowThreshold() {
        assertThat(sensor.getThreshold(MeasurementLevel.VERY_LOW), equalTo(event.getVeryLow()));
    }
}
