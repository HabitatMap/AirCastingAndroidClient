package pl.llp.aircasting.helper;

import pl.llp.aircasting.MeasurementLevel;
import pl.llp.aircasting.New;
import pl.llp.aircasting.model.Sensor;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.Assert.assertEquals;

public class ResourceHelperTest
{
  ResourceHelper helper;
  Sensor sensor;

  @Before
  public void setUp() throws Exception
  {
    helper = new ResourceHelper();
    sensor = New.sensor();

    HashMap<MeasurementLevel, Integer> objectObjectHashMap = newHashMap();
    helper.thresholds.put(sensor, objectObjectHashMap);
    for (MeasurementLevel level : MeasurementLevel.OBTAINABLE_LEVELS) {
      objectObjectHashMap.put(level, sensor.getThreshold(level));
    }
  }

  @Test
  public void level_selection_should_treat_doubles_properly() throws Exception
  {
    // given
    int threshold = sensor.getThreshold(MeasurementLevel.LOW);

    // when
    MeasurementLevel level = helper.getLevel(sensor, threshold + 0.01);

    // then
    assertEquals(MeasurementLevel.LOW, level);
  }

  @Test
  public void level_selection_should_include_lower_bound() throws Exception
  {
    // given
    int threshold = sensor.getThreshold(MeasurementLevel.LOW);

    // when
    MeasurementLevel level = helper.getLevel(sensor, threshold);

    // then
    assertEquals(MeasurementLevel.LOW, level);
  }
}
