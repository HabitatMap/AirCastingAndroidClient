package pl.llp.aircasting.sensor;

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.New;
import pl.llp.aircasting.model.internal.MeasurementLevel;
import pl.llp.aircasting.model.Sensor;

import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static com.google.common.collect.Maps.newHashMap;
import static org.junit.Assert.assertEquals;

@RunWith(InjectedTestRunner.class)
public class ThresholdsHolderTest
{
  @Inject ThresholdsHolder holder;
  Sensor sensor;

  @Before
  public void setUp() throws Exception
  {
    sensor = New.sensor();

    HashMap<MeasurementLevel, Integer> objectObjectHashMap = newHashMap();
    holder.thresholds.put(sensor, objectObjectHashMap);
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
    MeasurementLevel level = holder.getLevel(sensor, threshold + 0.01);

    // then
    assertEquals(MeasurementLevel.LOW, level);
  }

  @Test
  public void level_selection_should_include_lower_bound() throws Exception
  {
    // given
    int threshold = sensor.getThreshold(MeasurementLevel.LOW);

    // when
    MeasurementLevel level = holder.getLevel(sensor, threshold);

    // then
    assertEquals(MeasurementLevel.LOW, level);
  }
}
