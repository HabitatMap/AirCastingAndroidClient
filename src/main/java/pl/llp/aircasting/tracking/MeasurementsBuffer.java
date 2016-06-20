package pl.llp.aircasting.tracking;

import java.util.*;

public class MeasurementsBuffer {
  private static final long INTERVAL = 60000;
  private long lastGettingTime;
  private List<Double> values;

  public MeasurementsBuffer() {
    lastGettingTime = oneIntervalAgo();
    values = new ArrayList<Double>();
  }

  public void add(Double value) {
    values.add(value);
  }

  public Double get() {
    if(isGettable())
      return getAverage();
    else
      return null;
  }

  public boolean isGettable() {
    return !values.isEmpty() && oneIntervalAgo() >= lastGettingTime;
  }

  private double getAverage() {
    double average = calculateAverage();
    values.clear();
    lastGettingTime = System.currentTimeMillis();

    return average;
  }

  private double calculateAverage() {
    int size = values.size();
    double sum = 0;

    for (int i = 0; i < size; i++)
      sum += values.get(i);

    double average = sum / size;

    return average;
  }

  private long oneIntervalAgo() {
    return System.currentTimeMillis() - INTERVAL;
  }
}
