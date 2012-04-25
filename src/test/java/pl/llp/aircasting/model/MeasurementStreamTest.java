package pl.llp.aircasting.model;

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.event.sensor.SensorEvent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.IsCollectionContaining.hasItem;

@RunWith(InjectedTestRunner.class)
public class MeasurementStreamTest {
    private SensorEvent event;
    private MeasurementStream stream;

    Measurement measurement = new Measurement(0, 0, 0);
    Measurement otherMeasurement = new Measurement(0, 0, 1);

    @Before
    public void setup() {
        event = Any.sensorEvent();
        stream = new MeasurementStream(event);

        stream.add(measurement);
        stream.add(otherMeasurement);
    }

    @Test
    public void should_not_be_empty_with_measurements() {
        assertThat(stream.isEmpty(), equalTo(false));
    }

    @Test
    public void should_be_empty_without_measurements() {
        MeasurementStream stream = new MeasurementStream(event);
        assertThat(stream.isEmpty(), equalTo(true));
    }

    @Test
    public void shouldStoreMeasurements() {
        assertThat(stream.getMeasurements(), hasItem(equalTo(measurement)));
    }

    @Test
    public void shouldStoreSensorName() {
        assertThat(stream.getSensorName(), equalTo(event.getSensorName()));
    }

    @Test
    public void shouldStoreMeasurementType() {
        assertThat(stream.getMeasurementType(), equalTo(event.getMeasurementType()));
    }

    @Test
    public void shouldStoreUnit() {
        assertThat(stream.getUnit(), equalTo(event.getUnit()));
    }

    @Test
    public void shouldStoreSymbol() {
        assertThat(stream.getSymbol(), equalTo(event.getSymbol()));
    }

    @Test
    public void shouldProvideAnAverage() {
        assertThat(stream.getAvg(), equalTo(0.5));
    }

    @Test
    public void shouldProvidePeak() {
        assertThat(stream.getPeak(), equalTo(1.0));
    }

    @Test
    public void setting_all_measurements_should_properly_average() {
        ArrayList<Measurement> measurements = newArrayList(new Measurement(10), new Measurement(20));
        stream.setMeasurements(measurements);

        assertThat(stream.getAvg(), equalTo(15.0));
    }

    @Test
    public void setting_average_should_override_calculated() throws Exception {
        // given
        assertEquals(0.5, stream.getAvg(), 0.1);

        // when
        stream.setAvg(2);

        // then
        assertEquals(2, stream.getAvg(), 0.1);
    }

    @Test
    public void adding_a_measurement_should_override_set() throws Exception {
        // given
        stream.setAvg(2);
        assertEquals(2, stream.getAvg(), 0.1);

        // when
        stream.add(new Measurement(0.5, 1, 5));

        // then
        assertEquals(2, stream.getAvg(), 0.1);
    }
}
