package pl.llp.aircasting.model;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;
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
import pl.llp.aircasting.activity.ChartOptionsActivity;
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
    @Inject Context context;

    private final static int MOBILE_INTERVAL = 1000 * 60; // 1 minute
    private final static int FIXED_INTERVAL = 1000 * 60 * 60; // 1 hour
    private int averagesCounter = 0;
    private int interval;
    private Map<String, Boolean> newEntriesForStream = newHashMap();
    private Map<String, Boolean> staticChartGeneratedForStream = newHashMap();
    private Map<String, List> averages = newHashMap();
    private Handler handler = new Handler();
    private Runnable updateEntriesTask = new Runnable() {
        @Override
        public void run() {
            allowChartUpdate();
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
        String sensorName = sensor.getSensorName();
        MeasurementStream stream = sessionManager.getMeasurementStream(sensorName);
        String descriptionText = getDescription(stream);
        chart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, ChartOptionsActivity.class));
            }
        });

        draw(chart, descriptionText);

        if (stream == null) {
            chart.clear();
            return;
        }

        if (sessionManager.isSessionSaved() && shouldStaticChartUpdate(sensorName)) {
            averagesCounter = 10;
            updateChart(chart, sensor.getShortType(),sensorName);
        }

        if (!shouldDynamicChartUpdate(sensorName)) {
            return;
        }

        updateChart(chart, sensor.getShortType(), sensorName);
        chart.notifyDataSetChanged();
    }

    public void resetStaticCharts() {
        staticChartGeneratedForStream.clear();
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

        if (sessionManager.isSessionSaved()) {
            Measurement lastMeasurement = stream.getLastMeasurements(1).get(0);
            time = lastMeasurement.getTime().getTime();
        } else {
            Calendar calendar = Calendar.getInstance();
            time = calendar.getTime().getTime();
        }

        return dateFormat.format(time);
    }

    private void updateChart(LineChart chart, String unit, String sensorName) {
        LineData lineData = chart.getLineData();

        if (lineData == null) {
            lineData = new LineData();
        }

        lineData.removeDataSet(0);
        LineDataSet dataSet = prepareDataSet(unit, sensorName);

        List entries = getEntriesForStream(sensorName);
        if (entries == null) {
            return;
        }

        ArrayList<Integer> colors = prepareDataSetColors(sensorName, entries);
        dataSet.setCircleColors(colors);

        newEntriesForStream.put(sensorName, false);
        staticChartGeneratedForStream.put(sensorName, true);

        lineData.removeDataSet(0);
        lineData.addDataSet(dataSet);
        lineData.setValueFormatter(new MyValueFormatter());

        chart.setData(lineData);
    }

    private LineDataSet prepareDataSet(String unit, String sensorName) {
        if (averagesCounter > 0) {
            prepareEntries();
        }
        List<Entry> entries = getEntriesForStream(sensorName);
        LineDataSet dataSet = new LineDataSet(entries, "1 min Avg - " + unit);

        dataSet.setDrawCircleHole(false);
        dataSet.setCircleRadius(7);
        return dataSet;
    }

    private void prepareEntries() {
        List<MeasurementStream> streams = (List) sessionManager.getMeasurementStreams();

        for (MeasurementStream stream : streams) {
            double xValue = 0;
            double measurementsInPeriod = 60 / stream.getFrequency();
            String sensorName = stream.getSensorName();
            List entries = new CopyOnWriteArrayList();
            List<Measurement> measurements = stream.getMeasurementsForPeriod(averagesCounter);
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

    private void allowChartUpdate() {
        List<MeasurementStream> streams = (List) sessionManager.getMeasurementStreams();

        for (MeasurementStream stream : streams) {
            String sensorName = stream.getSensorName();
            newEntriesForStream.put(sensorName, true);
        }

        if (averagesCounter <= 10) {
            averagesCounter++;
        }
    }

    private ArrayList<Integer> prepareDataSetColors(String sensorName, List<Entry> entries) {
        ArrayList colors = new ArrayList<Integer>();

        for (Entry entry : entries) {
            Sensor sensor = sensorManager.getSensorByName(sensorName);
            colors.add(resourceHelper.getColorAbsolute(sensor, entry.getY()));
        }

        return colors;
    }

    private List<Entry> getEntriesForStream(String sensorName) {
        return averages.get(sensorName);
    }

    private double getAverage(List<Measurement> measurements) {
        double sum = 0;
        Iterator<Measurement> iter = measurements.iterator();

        while (iter.hasNext()) {
            Measurement measurement = iter.next();
            sum += measurement.getValue();
        }

        return Math.round((sum / measurements.size()));
    }

    private boolean shouldDynamicChartUpdate(String sensorName) {
        return newEntriesForStream.containsKey(sensorName) && newEntriesForStream.get(sensorName) == true;
    }

    private boolean shouldStaticChartUpdate(String sensorName) {
        return !staticChartGeneratedForStream.containsKey(sensorName);
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
