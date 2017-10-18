package pl.llp.aircasting.helper;

import android.content.Context;
import android.os.Handler;
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
import pl.llp.aircasting.R;
import pl.llp.aircasting.model.*;
import pl.llp.aircasting.view.presenter.MeasurementAggregator;

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
    @Inject CurrentSessionManager currentSessionManager;
    @Inject ViewingSessionsManager viewingSessionsManager;
    @Inject ViewingSessionsSensorManager viewingSessionsSensorManager;
    @Inject ResourceHelper resourceHelper;
    @Inject CurrentSessionSensorManager currentSessionSensorManager;
    @Inject Context context;

    private final static int INTERVAL_IN_SECONDS = 60;
    private final static int MAX_AVERAGES_AMOUNT = 9;
    private final static int MOBILE_INTERVAL = 1000 * INTERVAL_IN_SECONDS; // 1 minute
    private final static int FIXED_INTERVAL = 1000 * 60 * INTERVAL_IN_SECONDS; // 1 hour
    private static int averagesCounter = 0;
    private static int interval;
    private static Map<String, Boolean> shouldUseExistingEntries = newHashMap();
    private static Map<String, Boolean> newEntriesForStream = newHashMap();
    private static Map<String, Boolean> staticChartGeneratedForStream = newHashMap();
    private static Map<String, List> averages = newHashMap();
    private static String requestedSensorName;
    private static long requestedSessionId;
    private boolean isSessionRecording;
    private Handler handler = new Handler();
    private Runnable updateEntriesTask = new Runnable() {
        @Override
        public void run() {
            allowChartUpdate();
            handler.postDelayed(updateEntriesTask, interval);
        }
    };

    public void start() {
        resetState();
        interval = currentSessionManager.getCurrentSession().isFixed() ? FIXED_INTERVAL : MOBILE_INTERVAL;
        handler.postDelayed(updateEntriesTask, interval);
    }

    public void stop() {
        resetState();
        handler.removeCallbacks(updateEntriesTask);
    }

    private void resetState() {
        averagesCounter = 0;
        averages.clear();
        newEntriesForStream.clear();
        shouldUseExistingEntries.clear();
        resetAllStaticCharts();
    }

    public void drawChart(LineChart chart, final Sensor sensor, long sessionId) {
        requestedSensorName = sensor.getSensorName();
        requestedSessionId = sessionId;
        isSessionRecording = requestedSessionId == Constants.CURRENT_SESSION_FAKE_ID;
        MeasurementStream stream = getStream();
        String descriptionText = getDescription(stream);

        draw(chart, descriptionText);

        if (stream == null) {
            chart.clear();
            return;
        }

        initStaticChart(stream, chart, sensor);
        updateChart(chart, sensor);
    }

    private void initStaticChart(MeasurementStream stream, LineChart chart, Sensor sensor) {
        if (!isSessionRecording && shouldStaticChartInitialize()) {
            averagesCounter = MAX_AVERAGES_AMOUNT;
            prepareEntries(stream);
            updateChartData(chart, sensor.getShortType());
            staticChartGeneratedForStream.put(getKey(requestedSensorName), true);
        }
    }

    private void updateChart(LineChart chart, Sensor sensor) {
//        if (shouldDynamicChartUpdate() || chartHasMissingData(chart)) {
            updateChartData(chart, sensor.getShortType());
            chart.notifyDataSetChanged();
//        } else {
//            return;
//        }
    }

    private void updateChartData(LineChart chart, String unit) {
        LineData lineData = chart.getLineData();

        if (lineData == null) {
            lineData = new LineData();
        }

        List entries = getEntriesForCurrentStream();
        if (entries == null) {
            return;
        }

        lineData.removeDataSet(0);
        LineDataSet dataSet = prepareDataSet(unit);

        ArrayList<Integer> colors = prepareDataSetColors(entries);
        dataSet.setCircleColors(colors);

        newEntriesForStream.put(getKey(requestedSensorName), false);
        staticChartGeneratedForStream.put(getKey(requestedSensorName), true);

        lineData.removeDataSet(0);
        lineData.addDataSet(dataSet);
        lineData.setValueFormatter(new MyValueFormatter());

        chart.setData(lineData);
    }

    public void resetAllStaticCharts() {
        staticChartGeneratedForStream.clear();
    }

    public void resetSpecificStaticCharts(String[] sensors) {
        for (String sensorName : sensors) {
            staticChartGeneratedForStream.remove(getKey(sensorName));
        }
    }

    public void resetDynamicCharts(Set<String> sensors) {
        for (String sensorName : sensors) {
            newEntriesForStream.put(getKey(sensorName), true);
            shouldUseExistingEntries.put(getKey(sensorName), true);
        }
    }

    private void draw(LineChart chart, String descriptionText) {
        YAxis leftAxis = chart.getAxisLeft();
        YAxis rightAxis = chart.getAxisRight();
        XAxis xAxis = chart.getXAxis();
        Legend legend = chart.getLegend();
        Description desc = new Description();

        leftAxis.setEnabled(false);
        leftAxis.setDrawGridLines(false);
        leftAxis.setDrawLabels(false);
        rightAxis.setEnabled(false);
        rightAxis.setDrawGridLines(false);
        rightAxis.setDrawLabels(false);
        xAxis.setEnabled(false);
        xAxis.setDrawLabels(true);
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(8);
        legend.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        legend.setTextSize(10);

        desc.setText(descriptionText);
        desc.setPosition(chart.getWidth(), chart.getHeight() - 10);
        desc.setTextSize(10);

        chart.setDescription(desc);
        chart.setNoDataText("");
        chart.setPinchZoom(false);
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setDoubleTapToZoomEnabled(false);
    }

    private LineDataSet prepareDataSet(String unit) {
        List<Entry> entries = getEntriesForCurrentStream();
        LineDataSet dataSet = new LineDataSet(entries, "1 min Avg - " + unit);

        shouldUseExistingEntries.put(getKey(requestedSensorName), false);

        dataSet.setDrawCircleHole(false);
        dataSet.setCircleRadius(7);
        dataSet.setLineWidth(3);
        dataSet.setValueTextSize(10);
        dataSet.setDrawHighlightIndicators(false);

        if (!isSessionRecording) {
            int color = context.getResources().getColor(R.color.gray);
            dataSet.setColor(color);
        }

        return dataSet;
    }

    private void prepareEntries(MeasurementStream stream) {
        List<List<Measurement>> periodData;
        double xValue = 8;
        double measurementsInPeriod = INTERVAL_IN_SECONDS / stream.getFrequency();
        List entries = new CopyOnWriteArrayList();
        List<Measurement> measurements = stream.getMeasurementsForPeriod(averagesCounter);
        periodData = new ArrayList(Lists.partition(measurements, (int) measurementsInPeriod));

        if (periodData.size() > 0) {
            synchronized (periodData) {
                for (List<Measurement> dataChunk : Lists.reverse(periodData)) {
                    if (dataChunk.size() > measurementsInPeriod - 3) {
                        double yValue = getAverage(dataChunk);
                        entries.add(new Entry((float) xValue, (float) yValue));
                        xValue--;
                    }
                }
            }
        }

        if (entries.size() == 0) {
            return;
        }

        averages.put(getKey(requestedSensorName), Lists.reverse(entries));
    }

    private double getAverage(List<Measurement> measurements) {
        MeasurementAggregator aggregator = new MeasurementAggregator();

        for (int i = 0; i < measurements.size(); i++) {
            aggregator.add(measurements.get(i));
        }

        return Math.round(aggregator.getAverage().getValue());
    }

    private void allowChartUpdate() {
        List<MeasurementStream> streams = (List) currentSessionManager.getMeasurementStreams();

        if (averagesCounter < MAX_AVERAGES_AMOUNT) {
            averagesCounter++;
        }

        for (MeasurementStream stream : streams) {
            String sensorName = stream.getSensorName();
            prepareEntries(stream);
            newEntriesForStream.put(getKey(sensorName), true);
        }
    }

    private ArrayList<Integer> prepareDataSetColors(List<Entry> entries) {
        ArrayList colors = new ArrayList<Integer>();

        for (Entry entry : entries) {
            Sensor sensor = getSensor();
            colors.add(resourceHelper.getColorAbsolute(sensor, entry.getY()));
        }

        return colors;
    }

    private String getKey(String sensorName) {
        return requestedSessionId + "_" + sensorName;
    }

    private List<Entry> getEntriesForCurrentStream() {
        return averages.get(getKey(requestedSensorName));
    }

    private MeasurementStream getStream() {
        MeasurementStream stream;

        if (isSessionRecording) {
            stream = currentSessionManager.getMeasurementStream(requestedSensorName);
        } else {
            stream = viewingSessionsManager.getMeasurementStream(requestedSensorName, requestedSessionId);
        }
        return stream;
    }

    private Sensor getSensor() {
        Sensor sensor;

        if (isSessionRecording) {
            sensor = currentSessionSensorManager.getSensorByName(requestedSensorName);
        } else {
            sensor = viewingSessionsSensorManager.getSensorByName(requestedSensorName, requestedSessionId);
        }
        return sensor;
    }

    private boolean shouldDynamicChartUpdate() {
        return newEntriesForStream.containsKey(getKey(requestedSensorName)) && newEntriesForStream.get(getKey(requestedSensorName)) == true;
    }

    private boolean shouldStaticChartInitialize() {
        return !staticChartGeneratedForStream.containsKey(getKey(requestedSensorName));
    }

    private boolean chartHasMissingData(LineChart chart) {
        if (averagesCounter > 0) {
            if (chart.getLineData() == null || chart.getLineData().getEntryCount() != averagesCounter) {
                return true;
            }
        }
        return false;
    }

    private String getDescription(MeasurementStream stream) {
        double time;
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

        if (!isSessionRecording) {
            Measurement lastMeasurement = stream.getLastMeasurements(1).get(0);
            time = lastMeasurement.getTime().getTime();
        } else {
            Calendar calendar = Calendar.getInstance();
            time = calendar.getTime().getTime();
        }

        return dateFormat.format(time);
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
