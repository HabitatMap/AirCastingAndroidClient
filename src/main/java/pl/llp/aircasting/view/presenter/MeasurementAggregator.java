package pl.llp.aircasting.view.presenter;

import pl.llp.aircasting.model.Measurement;

import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newCopyOnWriteArrayList;

public class MeasurementAggregator
{
  private double longitude = 0;
  private double latitude = 0;
  private double value = 0;
  private long time = 0;
  private int count = 0;

  public void add(Measurement measurement)
  {
    longitude += measurement.getLongitude();
    latitude += measurement.getLatitude();
    value += measurement.getValue();
    time += measurement.getTime().getTime();
    count += 1;
  }

  public void reset()
  {
    longitude = latitude = value = time = count = 0;
  }

  public Measurement getAverage()
  {
    return new Measurement(latitude / count, longitude / count, value / count, new Date(time / count));
  }

  public boolean isComposite()
  {
    return count > 1;
  }

  public boolean isEmpty()
  {
    return count == 0;
  }

  public List<Measurement> smoothenSamplesToReduceCount(List<Measurement> samples, int limit)
  {
    reset();

    List<Measurement> result = newCopyOnWriteArrayList();
    double fillFactor = 1.0 * limit / samples.size();
    double fill = 0.0;

    for (Measurement measurement : samples)
    {
      add(measurement);
      fill += fillFactor;
      if(fill > 1)
      {
        fill -= 1;
        result.add(getAverage());
        reset();
      }
    }
    if(count > 0)
    {
      result.add(getAverage());
    }
    return result;
  }
}
