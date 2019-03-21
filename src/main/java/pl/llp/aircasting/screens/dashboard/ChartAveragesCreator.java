package pl.llp.aircasting.screens.dashboard;

import android.util.Log;
import com.github.mikephil.charting.data.Entry;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Session;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by radek on 27/02/18.
 */
@Singleton
public class ChartAveragesCreator {
    private final static int INTERVAL_IN_SECONDS = 60;
    private final static int MAX_AVERAGES_AMOUNT = 9;
    private final static int MAX_X_VALUE = 8;
    private final static double MOBILE_FREQUENCY_DIVISOR = 8 * 1000;
    private static List oldEntries = new CopyOnWriteArrayList();
    private static boolean usePreviousEntry = false;

    public synchronized static List getMobileEntries(MeasurementStream stream) {
        final ArrayList<List<Measurement>> periodData;
        double streamFrequency = stream.getFrequency(MOBILE_FREQUENCY_DIVISOR);
        double xValue = MAX_X_VALUE;
        int measurementsInPeriod = (int) (INTERVAL_IN_SECONDS / streamFrequency);

        List<Entry> entries = new CopyOnWriteArrayList();

        final List<Measurement> measurements = stream.getMeasurementsForPeriod(MAX_AVERAGES_AMOUNT, MOBILE_FREQUENCY_DIVISOR);
        periodData = new ArrayList(Lists.partition(measurements, measurementsInPeriod));
        final List<List<Measurement>> reversedPeriodData = Lists.reverse(periodData);

        synchronized (reversedPeriodData) {
            if (periodData.size() > 0) {
                for (int i = 0; i < reversedPeriodData.size(); i++) {
                    double yValue;
                    try {
                        List<Measurement> dataChunk = Collections.synchronizedList(reversedPeriodData.get(i));
                        synchronized (dataChunk) {
                            if (dataChunk.size() > measurementsInPeriod - getTolerance(measurementsInPeriod)) {
                                yValue = getAverage(dataChunk);
                                if (usePreviousEntry && !entries.isEmpty()) {
                                    yValue = entries.get(i - 1).getY();
                                } else if (usePreviousEntry && entries.isEmpty()) {
                                    yValue = measurements.get(0).getValue();
                                }

                                usePreviousEntry = false;
                                entries.add(new Entry((float) xValue, (float) yValue));
                                xValue--;
                            }
                        }
                    } catch (ConcurrentModificationException e) {
                        return oldEntries;
                    }
                }
            }
        }

        if (entries.size() == 0) {
            return entries;
        }

        oldEntries = entries;
        return entries;
    }

    public static List<Entry> getFixedEntries(MeasurementStream stream) {
        List<Measurement> measurements;
        double xValue = MAX_X_VALUE;
        List entries = new CopyOnWriteArrayList();
        List<List<Measurement>> periodData = new ArrayList();

        int maxMeasurementsAmount = 600;

        measurements = stream.getLastMeasurements(maxMeasurementsAmount);

        if (measurements.isEmpty()) {
            return entries;
        }

        int hour = measurements.get(0).getTime().getHours();
        List<Measurement> measurementsInHour = new ArrayList<Measurement>();

        Log.w("first hour", String.valueOf(hour));

        for (int i = 0; i < measurements.size(); i++) {
            Measurement measurement = measurements.get(i);
            int measurementHour = measurement.getTime().getHours();

            if (hour == measurementHour) {
                measurementsInHour.add(measurement);
            } else {
                Log.w("measuremennts in hour", String.valueOf(measurementsInHour.size()));

                periodData.add(measurementsInHour);
                hour = measurementHour;
                measurementsInHour = new ArrayList<Measurement>();
                measurementsInHour.add(measurement);
            }
        }

        if (periodData.size() > 0) {
            for (List<Measurement> dataChunk : Lists.reverse(periodData)) {
                if (xValue < 0) {
                    return entries;
                }
                synchronized (dataChunk) {
                    double yValue = getAverage(dataChunk);
                    entries.add(new Entry((float) xValue, (float) yValue));
                    xValue--;
                }
            }
        }

        if (entries.size() == 0) {
            return entries;
        }

        return entries;
    }

    private static double getTolerance(double measurementsInPeriod) {
        return 0.1 * measurementsInPeriod;
    }

    private static int getAverage(List<Measurement> measurements) {
        double sum = 0;
        int lastIndex = 1;
        List<Measurement> m = Collections.synchronizedList(measurements);
        int size = m.size();

            synchronized (m) {
                try {
                    for (int i = 0; i < size; i++) {
                        lastIndex = i;
                        sum += m.get(i).getValue();
                    }
                } catch (ConcurrentModificationException e) {
                    if (lastIndex == 0) {
                        usePreviousEntry = true;
                        return (int) sum;
                    } else {
                        return (int) sum / lastIndex;
                    }
                }
            }

        return (int) (sum / size);
    }
}
