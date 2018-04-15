package pl.llp.aircasting.view.presenter;

import pl.llp.aircasting.model.Measurement;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Date;

import static com.google.common.collect.Lists.newArrayList;

public class MeasurementAggregator {
    private double cumulativeLongitude;
    private double cumulativeLatitude;
    private double cumulativeValue;
    private long cumulativeTime;
    private int count;

    public void add(Measurement measurement) {
        cumulativeLongitude += measurement.getLongitude();
        cumulativeLatitude += measurement.getLatitude();
        cumulativeValue += measurement.getValue();
        cumulativeTime += measurement.getTime().getTime();
        count += 1;
    }

    public void reset() {
        cumulativeLongitude = cumulativeLatitude = cumulativeValue = cumulativeTime = count = 0;
    }

    public Measurement getAverage() {
        double latitude = cumulativeLatitude / count;
        double longitude = cumulativeLongitude / count;
        double value = cumulativeValue / count;
        Date time = new Date(cumulativeTime / count);
        return new Measurement(latitude, longitude, value, time);
    }

    public boolean isComposite() {
        return count > 1;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public ArrayList<Measurement> smoothenSamplesToReduceCount(ArrayList<Measurement> samples, int limit) {
        reset();

        ArrayList<Measurement> result = newArrayList();
        double fillFactor = 1.0 * limit / samples.size();
        double fill = 0.0;

        for (Measurement measurement : samples) {
            add(measurement);
            fill += fillFactor;
            if (fill > 1) {
                fill -= 1;
                result.add(getAverage());
                reset();
            }
        }
        if (count > 0) {
            result.add(getAverage());
        }
        return result;
    }

    public static Measurement getAverage(ImmutableList<Measurement> measurements) {
        MeasurementAggregator aggregator = new MeasurementAggregator();

        synchronized (measurements) {
            for (Measurement measurement : measurements) {
                aggregator.add(measurement);
            }
        }

        return aggregator.getAverage();
    }
}
