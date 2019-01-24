package pl.llp.aircasting.sensor;

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.New;
import pl.llp.aircasting.event.sensor.ThresholdSetEvent;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.internal.MeasurementLevel;
import pl.llp.aircasting.sensor.common.ThresholdsHolder;

import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static com.google.common.collect.Maps.newHashMap;
import static org.fest.assertions.Assertions.assertThat;

@RunWith(InjectedTestRunner.class)
public class ThresholdsHolderTest
{
  private static final int NEW_LOW = 39;
  @Inject
  ThresholdsHolder holder;
  Sensor sensor;

  @Before
  public void setUp() throws Exception
  {
    sensor = New.sensor();

    HashMap<MeasurementLevel, Integer> objectObjectHashMap = newHashMap();
    holder.thresholds.put(sensor, objectObjectHashMap);
    for (MeasurementLevel level : MeasurementLevel.OBTAINABLE_LEVELS)
    {
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
    assertThat(level).isEqualTo(MeasurementLevel.LOW);
  }

  @Test
  public void level_selection_should_include_lower_bound() throws Exception
  {
    // given
    int threshold = sensor.getThreshold(MeasurementLevel.LOW);

    // when
    MeasurementLevel level = holder.getLevel(sensor, threshold);

    // then
    assertThat(MeasurementLevel.LOW).isEqualTo(level);
  }

  @Test
  public void should_use_new_threshold_values() throws Exception
  {
    // given
    assertThat(holder.getLevel(sensor, NEW_LOW)).isNotEqualTo(MeasurementLevel.LOW);

    // when
    holder.onEvent(new ThresholdSetEvent(sensor, MeasurementLevel.LOW, NEW_LOW));

    // then
    assertThat(holder.getLevel(sensor, NEW_LOW)).isEqualTo(MeasurementLevel.LOW);
  }
}
