package pl.llp.aircasting.model;

import android.os.Handler;
import android.util.Log;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.helper.ResourceHelper;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.google.common.collect.Maps.newHashMap;

/**
 * Created by radek on 06/07/17.
 */
@Singleton
public class DashboardChartManager {
    @Inject SessionManager sessionManager;
    @Inject ResourceHelper resourceHelper;
    @Inject SensorManager sensorManager;

    private final static int ENTRIES_MAX_SIZE = 10;
    private final static int MOBILE_INTERVAL = 1000 * 10; // 1 minute
    private final static int FIXED_INTERVAL = 1000 * 60 * 60; // 1 hour
    private int interval;
    private Map<String, Boolean> shouldChartUpdate = newHashMap();
    private Map<String, List> averages = newHashMap();
    private Handler handler = new Handler();
    private Runnable updateEntriesTask = new Runnable() {
        @Override
        public void run() {
            updateLiveEntries();
            handler.postDelayed(updateEntriesTask, interval);
        }
    };

    public void start() {
        averages.clear();
        interval = sessionManager.getSession().isFixed() ? FIXED_INTERVAL : MOBILE_INTERVAL;
        handler.postDelayed(updateEntriesTask, interval);
    }

    public void stop() {
        averages.clear();
        handler.removeCallbacks(updateEntriesTask);
    }

    public void drawChart(LineChart chart, Sensor sensor) {
        MeasurementStream stream = sessionManager.getMeasurementStream(sensor.getSensorName());
        String descriptionText = getDescription(stream);

        draw(chart, descriptionText);

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

        prepareLineData(chart, entries, sensor.getShortType(), stream);
        chart.notifyDataSetChanged();
    }

    private void draw(LineChart chart, String descriptionText) {
        YAxis leftAxis = chart.getAxisLeft();
        YAxis rightAxis = chart.getAxisRight();
        XAxis xAxis = chart.getXAxis();
        Legend legend = chart.getLegend();

        leftAxis.setEnabled(false);
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawLabels(false);
        rightAxis.setEnabled(false);
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawLabels(false);
        xAxis.setEnabled(false);
        xAxis.setDrawLabels(true);
        legend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);

        Description desc = new Description();
        desc.setText(descriptionText);
        desc.setPosition(chart.getWidth(), chart.getHeight() - 10);
        chart.setDescription(desc);
        chart.setNoDataText("No data available yet");
    }

    private String getDescription(MeasurementStream stream) {
        double time;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        if (sessionManager.isSessionSaved()) {
            Measurement lastMeasurement = stream.getLastMeasurements(1).get(0);
            time = lastMeasurement.getTime().getTime();
        } else {
            Calendar calendar = Calendar.getInstance();
            time = calendar.getTime().getTime();
        }

        return sdf.format(time);
    }

    private void prepareLineData(LineChart chart, List entries, String unit, MeasurementStream stream) {
        if (shouldChartUpdate.get(stream.getSensorName()) == true) {
            LineData lineData = chart.getLineData();

            if (lineData == null) {
                lineData = new LineData();
            }

            LineDataSet dataSet = (LineDataSet) lineData.getDataSetByIndex(0);

            if (dataSet == null) {
                dataSet = createDataSet(unit);
            }

            dataSet.addEntry((Entry) entries.get(entries.size() - 1));

            Log.i(stream.getSensorName(), String.valueOf(entries.get(entries.size()-1)));

            if (dataSet.getEntryCount() > 10) {
                dataSet.removeEntry(0);
            }

            ArrayList<Integer> colors = prepareDataSetColors(stream, entries);
            dataSet.setCircleColors(colors);

            shouldChartUpdate.put(stream.getSensorName(), false);

            lineData.removeDataSet(0);
            lineData.addDataSet(dataSet);

            lineData.setValueFormatter(new MyValueFormatter());
            chart.setData(lineData);
        }
    }

    private LineDataSet createDataSet(String unit) {
        LineDataSet dataSet = new LineDataSet(null, "1 min Avg - " + unit);
        dataSet.setDrawCircleHole(false);
        dataSet.setCircleRadius(7);
        return dataSet;
    }

    private void prepareEntriesForViewing() {
        List<MeasurementStream> streams = (List) sessionManager.getMeasurementStreams();

        for (MeasurementStream stream : streams) {
            double xValue = 0;
            double measurementsInPeriod = 10 / stream.getFrequency();
            String sensorName = stream.getSensorName();
            List entries = new CopyOnWriteArrayList();
            List<Measurement> measurements = stream.getMeasurementsForPeriod(10);
            List<List<Measurement>> periodData = new ArrayList(Lists.partition(measurements, (int) measurementsInPeriod));

            trimIncompletePeriodData(periodData, measurementsInPeriod);

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

    private void trimIncompletePeriodData(List<List<Measurement>> periodData, double measurementsInPeriod) {
        List<Measurement> lastPeriodData = periodData.get(periodData.size() - 1);

        if (lastPeriodData.size() < measurementsInPeriod) {
            periodData.remove(periodData.size() - 1);
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
            if (entries.size() > ENTRIES_MAX_SIZE) {
                entries.remove(0);
            }

            averages.put(sensorName, entries);
            shouldChartUpdate.put(sensorName, true);
        }
    }

    private ArrayList<Integer> prepareDataSetColors(MeasurementStream stream, List<Entry> entries) {
        ArrayList colors = new ArrayList<Integer>();

        for (Entry entry : entries) {
            Sensor sensor = sensorManager.getSensorByName(stream.getSensorName());
            colors.add(resourceHelper.getColorAbsolute(sensor, entry.getY()));
        }

        return colors;
    }

    private List<Entry> getEntriesForStream(MeasurementStream stream) {
        return averages.get(stream.getSensorName());
    }

    private double getAverage(List<Measurement> measurements) {
        double sum = 0;

        for (Measurement measurement : measurements) {
            sum += measurement.getValue();
        }

        return Math.round((sum / measurements.size()));
    }

    public class MyValueFormatter implements IValueFormatter {

        private DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,###");
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            return mFormat.format(value);
        }
    }
}
