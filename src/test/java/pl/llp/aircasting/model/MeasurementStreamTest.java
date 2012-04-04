package pl.llp.aircasting.model;

import pl.llp.aircasting.InjectedTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.IsCollectionContaining.hasItem;

@RunWith(InjectedTestRunner.class)
public class MeasurementStreamTest {
    MeasurementStream stream = new MeasurementStream("big sensor", "temperature", "degrees Celsius", "C");
    Measurement measurement = new Measurement(0, 0, 0);
    Measurement otherMeasurement = new Measurement(0, 0, 1);

    @Before
    public void setup() {
        stream.add(measurement);
        stream.add(otherMeasurement);
    }

    @Test
    public void shouldStoreMeasurements() {
        assertThat(stream.getMeasurements(), hasItem(equalTo(measurement)));
    }

    @Test
    public void shouldStoreSensorName() {
        assertThat(stream.getSensorName(), equalTo("big sensor"));
    }

    @Test
    public void shouldStoreMeasurementType() {
        assertThat(stream.getMeasurementType(), equalTo("temperature"));
    }

    @Test
    public void shouldStoreUnit() {
        assertThat(stream.getUnit(), equalTo("degrees Celsius"));
    }

    @Test
    public void shouldStoreSymbol() {
        assertThat(stream.getSymbol(), equalTo("C"));
    }

    @Test
    public void shouldProvideAnAverage() {
        assertThat(stream.getAvg(), equalTo(0.5));
    }

    @Test
    public void shouldProvidePeak() {
        assertThat(stream.getPeak(), equalTo(1.0));
    }
}
