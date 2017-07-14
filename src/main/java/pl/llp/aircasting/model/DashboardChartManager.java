package pl.llp.aircasting.model;

import android.os.Handler;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.google.common.collect.Maps.newHashMap;


/**
 * Created by radek on 06/07/17.
 */
@Singleton
public class DashboardChartManager {
    @Inject SessionManager sessionManager;

    private final static int INTERVAL = 1000 * 60; // 1 minute
    private Map<String, List> averages = newHashMap();
    private Handler handler = new Handler();
    private Runnable updateEntriesTask = new Runnable() {
        @Override
        public void run() {
            updateLiveEntries();
            handler.postDelayed(updateEntriesTask, INTERVAL);
        }
    };

    public void start() {
        averages.clear();
        handler.postDelayed(updateEntriesTask, INTERVAL);
    }

    public void stop() {
        averages.clear();
        handler.removeCallbacks(updateEntriesTask);
    }

    public void drawChart(LineChart chart, Sensor sensor) {
        YAxis leftAxis = chart.getAxisLeft();
        YAxis rightAxis = chart.getAxisRight();
        XAxis xAxis = chart.getXAxis();
        leftAxis.setEnabled(false);
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawLabels(false);
        rightAxis.setEnabled(false);
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawLabels(false);
        xAxis.setEnabled(false);
        xAxis.setEnabled(false);
        xAxis.setDrawLabels(true);
        chart.setNoDataText("No data available yet");

        MeasurementStream stream = sessionManager.getMeasurementStream(sensor.getSensorName());
        if (stream == null) {
            chart.clear();
            return;
        }

        if (sessionManager.isSessionSaved()) {
            prepareEntriesForViewing();
        }

        List<Entry> entries = getEntriesForStream(stream);
        if (entries == null) {
            return;
        }

        LineDataSet dataSet = new LineDataSet(entries, "1 min Avg - " + sensor.getUnit());
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
    }

    public void prepareEntriesForViewing() {
        List<MeasurementStream> streams = (List) sessionManager.getMeasurementStreams();

        for (MeasurementStream stream : streams) {
            double xValue = 0;
            String sensorName = stream.getSensorName();
            List entries = new CopyOnWriteArrayList();
            List<Measurement> measurements = stream.getMeasurementsForPeriod(10);
            double measurementsInPeriod = 60 / stream.getFrequency();
            List<List<Measurement>> periodData = new ArrayList(Lists.partition(measurements, (int) measurementsInPeriod));
            List<Measurement> lastPeriodData = periodData.get(periodData.size() - 1);

            if (lastPeriodData.size() < measurementsInPeriod) {
                periodData.remove(periodData.size() - 1);
            }

            if (periodData.size() > 0) {
                for (List<Measurement> batch : periodData) {
                    double yValue = getAverage(batch);
                    entries.add(new Entry((float) xValue, (float) yValue));
                    xValue++;
                }
            }

            if (entries.size() == 0) {
                return;
            }

            averages.put(sensorName, entries);
        }
    }

    private void updateLiveEntries() {
        List<MeasurementStream> streams = (List) sessionManager.getMeasurementStreams();

        for (MeasurementStream stream : streams) {
            double xValue, yValue;
            List entries;
            String sensorName = stream.getSensorName();

            List<Measurement> measurements = stream.getMeasurementsForPeriod(1);
            yValue = getAverage(measurements);

            if (averages.containsKey(sensorName)) {
                entries = averages.get(sensorName);
                Entry lastEntry = (Entry) entries.get(entries.size() - 1);
                xValue = lastEntry.getX() + 1;
            } else {
                entries = new CopyOnWriteArrayList();
                xValue = 0;
            }

            entries.add(new Entry((float) xValue, (float) yValue));
            if (entries.size() > 10) {
                entries.remove(0);
            }

            averages.put(sensorName, entries);
        }
    }

    private List<Entry> getEntriesForStream(MeasurementStream stream) {
        return averages.get(stream.getSensorName());
    }

    private double getAverage(List<Measurement> measurements) {
        double sum = 0;

        for (Measurement measurement : measurements) {
            sum += measurement.getValue();
        }

        return (sum / measurements.size());
    }
}
