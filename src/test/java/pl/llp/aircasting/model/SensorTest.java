package pl.llp.aircasting.model;

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
    private Sensor sensor;

    @Before
    public void setup() {
        SensorEvent event = new SensorEvent("LHC", "Hadrons", "number", "#", 0, 10, 20, 30, 40, 10);
        sensor = new Sensor(event);
    }

    @Test
    public void shouldProvideTooHighThreshold() {
        assertThat(sensor.getThreshold(MeasurementLevel.VERY_HIGH), equalTo(40));
    }

    @Test
    public void shouldProvideHighThreshold() {
        assertThat(sensor.getThreshold(MeasurementLevel.HIGH), equalTo(30));
    }

    @Test
    public void shouldProvideMidThreshold() {
        assertThat(sensor.getThreshold(MeasurementLevel.MID), equalTo(20));
    }

    @Test
    public void shouldProvideLowThreshold() {
        assertThat(sensor.getThreshold(MeasurementLevel.LOW), equalTo(10));
    }

    @Test
    public void shouldProvideVeryLowThreshold() {
        assertThat(sensor.getThreshold(MeasurementLevel.VERY_LOW), equalTo(0));
    }
}
