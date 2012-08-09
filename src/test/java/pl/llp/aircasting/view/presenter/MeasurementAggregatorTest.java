package pl.llp.aircasting.view.presenter;

import pl.llp.aircasting.New;
import pl.llp.aircasting.model.Measurement;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Created by ags on 03/08/12 at 14:18
 */
public class MeasurementAggregatorTest
{
  private MeasurementAggregator aggregator;

  @Before
  public void setUp() throws Exception
  {
    aggregator = new MeasurementAggregator();
  }

  @Test
  public void should_decreaseNumberOfSamples() throws Exception
  {
    // given
    ArrayList<Measurement> measurements = measurements(1, -1);

    // when
    List<Measurement> list = aggregator.smoothenSamplesToReduceCount(measurements, 1);

    // then
    assertEquals(0, list.get(0).getValue(), 0.1);

  }

  ArrayList<Measurement> measurements(int... values)
  {
    ArrayList<Measurement> result = Lists.newArrayList();

    for (int value : values)
    {
      result.add(New.measurement(value));
    }

    return result;
  }
}
