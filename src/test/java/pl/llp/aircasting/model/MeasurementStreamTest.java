package pl.llp.aircasting.model;

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.New;
import pl.llp.aircasting.model.events.SensorEvent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static com.google.common.collect.Lists.newArrayList;
import static junit.framework.Assert.assertEquals;
import static org.fest.assertions.Assertions.assertThat;

@RunWith(InjectedTestRunner.class)
public class MeasurementStreamTest
{
    private SensorEvent event;
    private MeasurementStream stream;

    Measurement measurement = new Measurement(0, 0, 0);
    Measurement otherMeasurement = new Measurement(0, 0, 1);

    @Before
    public void setup() {
        event = New.sensorEvent();
        stream = event.stream();

        stream.add(measurement);
        stream.add(otherMeasurement);
    }

    @Test
    public void should_not_be_empty_with_measurements() {
        assertThat(stream.isEmpty()).isFalse();
    }

    @Test
    public void should_be_empty_without_measurements() {
        MeasurementStream stream = event.stream();
        assertThat(stream.isEmpty()).isTrue();
    }

    @Test
    public void shouldStoreMeasurements() {
        assertThat(stream.getMeasurements()).contains(measurement);
    }

    @Test
    public void shouldStoreSensorName() {
      assertThat(stream.getSensorName()).isEqualTo(event.getSensorName());
    }

    @Test
    public void shouldStoreMeasurementType() {
      assertThat(stream.getMeasurementType()).isEqualTo(event.getMeasurementType());
    }

    @Test
    public void shouldStoreUnit() {
        assertThat(stream.getUnit()).isEqualTo(event.getUnit());
    }

    @Test
    public void shouldStoreSymbol() {
        assertThat(stream.getSymbol()).isEqualTo(event.getSymbol());
    }

    @Test
    public void shouldProvideAnAverage() {
        assertThat(stream.getAvg()).isEqualTo(0.5);
    }

    @Test
    public void shouldProvidePeak() {
        assertThat(stream.getPeak()).isEqualTo(1.0);
    }

    @Test
    public void setting_all_measurements_should_properly_average() {
        ArrayList<Measurement> measurements = newArrayList(new Measurement(10), new Measurement(20));
        stream.setMeasurements(measurements);

        assertThat(stream.getAvg()).isEqualTo(15.0);
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
        assertEquals(3.0, stream.getAvg(), 0.1);
    }

  @Test
  public void should_calculateAverage() throws Exception
  {
    stream = new MeasurementStream();
      // given
      stream.add(New.measurement(0));
      stream.add(New.measurement(2));
      assertEquals(1.0, stream.getAvg());

      // when
      stream.add(New.measurement(4));

      // then
    assertEquals(2.0, stream.getAvg());
  }
}
