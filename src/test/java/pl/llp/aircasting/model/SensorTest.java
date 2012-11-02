package pl.llp.aircasting.model;

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.New;
import pl.llp.aircasting.event.sensor.SensorEvent;

import org.hamcrest.core.IsEqual;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(InjectedTestRunner.class)
public class SensorTest
{
  private SensorEvent event;
  private Sensor sensor;

  @Before
  public void setup()
  {
    event = New.sensorEvent();
    sensor = new Sensor(event);
  }

  @Test
  public void shouldProvideTooHighThreshold()
  {
    assertThat(sensor.getThreshold(MeasurementLevel.VERY_HIGH), equalTo(event.getVeryHigh()));
  }

  @Test
  public void shouldProvideHighThreshold()
  {
    assertThat(sensor.getThreshold(MeasurementLevel.HIGH), equalTo(event.getHigh()));
  }

  @Test
  public void shouldProvideMidThreshold()
  {
    assertThat(sensor.getThreshold(MeasurementLevel.MID), equalTo(event.getMid()));
  }

  @Test
  public void shouldProvideLowThreshold()
  {
    assertThat(sensor.getThreshold(MeasurementLevel.LOW), equalTo(event.getLow()));
  }

  @Test
  public void shouldProvideVeryLowThreshold()
  {
    assertThat(sensor.getThreshold(MeasurementLevel.VERY_LOW), equalTo(event.getVeryLow()));
  }

  @Test
  public void shouldReturnLowerOneOnTheBorder()
  {
    assertThat(sensor.level(event.getLow()), IsEqual.equalTo(MeasurementLevel.VERY_LOW));
    assertThat(sensor.level(event.getMid()), IsEqual.equalTo(MeasurementLevel.LOW));
    assertThat(sensor.level(event.getHigh()), IsEqual.equalTo(MeasurementLevel.MID));
    assertThat(sensor.level(event.getVeryHigh()), IsEqual.equalTo(MeasurementLevel.HIGH));
  }
}
