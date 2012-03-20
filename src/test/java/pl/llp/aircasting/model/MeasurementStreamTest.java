package pl.llp.aircasting.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.InjectedTestRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.IsCollectionContaining.hasItem;

@RunWith(InjectedTestRunner.class)
public class MeasurementStreamTest {
    MeasurementStream stream = new MeasurementStream();
    Measurement measurement = new Measurement(0, 0, 0);
    Measurement otherMeasurement = new Measurement(0, 0, 1);

    @Before
    public void setup(){
        stream.add(measurement);
        stream.add(otherMeasurement);
    }

    @Test
    public void shouldStoreMeasurements() {
        assertThat(stream.getMeasurements(), hasItem(equalTo(measurement)));
    }
}
