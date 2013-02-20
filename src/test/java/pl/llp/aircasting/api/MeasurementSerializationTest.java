package pl.llp.aircasting.api;

import pl.llp.aircasting.guice.GsonProvider;
import pl.llp.aircasting.model.Measurement;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.fest.assertions.Assertions.assertThat;

public class MeasurementSerializationTest
{
  final double LONGITUDE = 1.0;
  final double LATITUDE = 2.0;
  final double VALUE = 3.0;
  final Date TIME = new Date(123456789);

  private Measurement measurement;
  private String measurementAsJson;

  private Gson gson;

  @Before
  public void setUp() throws Exception
  {
    gson = new GsonProvider().get();
    measurement = new Measurement(LATITUDE, LONGITUDE, VALUE, TIME);
    measurementAsJson = gson.toJson(measurement);
  }

  @Test
  public void should_serialize_and_deserialize() throws Exception
  {
    // given
    Measurement copy = gson.fromJson(measurementAsJson, Measurement.class);

    // when
    String copyAsJson = gson.toJson(copy);

    // then
    assertThat(copyAsJson).isEqualToIgnoringCase(measurementAsJson);
  }

  @Test
  public void should_deserialize_milliseconds() throws Exception
  {
    // given
    Measurement copy = gson.fromJson(measurementAsJson, Measurement.class);

    // when
    long timeFromCopy = copy.getTime().getTime();

    // then
    assertThat(timeFromCopy).isEqualTo(measurement.getTime().getTime());
  }
}
