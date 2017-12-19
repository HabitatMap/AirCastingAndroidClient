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
import pl.llp.aircasting.util.Constants;
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
    @Inject ResourceHelper resourceHelper;
    @Inject Context context;
    @Inject SessionDataFactory sessionData;

    private final static int INTERVAL_IN_SECONDS = 60;
    private final static int MAX_AVERAGES_AMOUNT = 9;
    private final static int MOBILE_INTERVAL = 1000 * INTERVAL_IN_SECONDS; // 1 minute
    private final static int FIXED_INTERVAL = 1000 * 60 * INTERVAL_IN_SECONDS; // 1 hour
    private final static int MAX_X_VALUE = 8;
    private final static String FIXED_LABEL = "1 hr Avg";
    private final static String MOBILE_LABEL = "1 min Avg";
    private static int dynamicAveragesCount = 0;
    private static int interval;
    private static Map<String, Boolean> staticChartGeneratedForStream = newHashMap();
    private static Map<String, List> averages = newHashMap();
    private static String requestedSensorName;
    private static String requestedStreamKey;
    private static long requestedSessionId;
    private boolean isSessionCurrent;
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
        dynamicAveragesCount = 0;
        averages.clear();
        resetAllStaticCharts();
    }

    public void resetAllStaticCharts() {
        staticChartGeneratedForStream.clear();
    }

    public void resetSpecificStaticCharts(Long sessionId, String[] sensors) {
        for (String sensorName : sensors) {
            staticChartGeneratedForStream.remove(getKey(sessionId, sensorName));
        }
    }

    public void drawChart(LineChart chart, final Sensor sensor, long sessionId) {
        requestedSensorName = sensor.getSensorName();
        requestedSessionId = sessionId;
        MeasurementStream stream = getStream();
        isSessionCurrent = requestedSessionId == Constants.CURRENT_SESSION_FAKE_ID;
        requestedStreamKey = getKey(requestedSessionId, requestedSensorName);
        String descriptionText = getTimestamp(stream);

        draw(chart, descriptionText);
        chart.clear();

        if (stream == null) {
            return;
        }

        initStaticChart(stream, chart, sensor);
        updateChart(chart, sensor);
    }

    private void initStaticChart(MeasurementStream stream, LineChart chart, Sensor sensor) {
        if (!isSessionCurrent && shouldStaticChartInitialize()) {
            prepareEntries(requestedSessionId, stream);
            updateChartData(chart, sensor.getShortType());
            staticChartGeneratedForStream.put(requestedStreamKey, true);
        }
    }

    private void updateChart(LineChart chart, Sensor sensor) {
        updateChartData(chart, sensor.getShortType());
        chart.notifyDataSetChanged();
    }

    private void updateChartData(LineChart chart, String unit) {
        LineData lineData = chart.getLineData();

        if (lineData == null) {
            lineData = new LineData();
        }

        List<Entry> entries = getEntriesForCurrentStream();
        if (entries == null) {
            return;
        }

        LineDataSet dataSet = prepareDataSet(unit);

        lineData.removeDataSet(0);
        lineData.addDataSet(dataSet);
        lineData.setValueFormatter(new MyValueFormatter());

        chart.setData(lineData);
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
        LineDataSet dataSet = new LineDataSet(entries, getDatasetLabel() + unit);
        ArrayList<Integer> colors = prepareDataSetColors(entries);

        dataSet.setDrawCircleHole(false);
        dataSet.setCircleRadius(7);
        dataSet.setLineWidth(3);
        dataSet.setValueTextSize(10);
        dataSet.setDrawHighlightIndicators(false);
        dataSet.setCircleColors(colors);

        if (!isSessionCurrent) {
            int color = context.getResources().getColor(R.color.gray);
            dataSet.setColor(color);
        }

        return dataSet;
    }

    private String getDatasetLabel() {
        if (sessionData.getSession(requestedSessionId).isFixed()) {
            return FIXED_LABEL;
        } else {
            return MOBILE_LABEL;
        }
    }

    private void prepareEntries(long sessionId, MeasurementStream stream) {
        List<List<Measurement>> periodData;
        boolean isFixed = sessionData.getSession(sessionId).isFixed();
        double streamFrequency = stream.getFrequency(isFixed);
        double xValue = MAX_X_VALUE;
        double measurementsInPeriod = INTERVAL_IN_SECONDS / streamFrequency;
        List entries = new CopyOnWriteArrayList();
        List<Measurement> measurements = stream.getMeasurementsForPeriod(MAX_AVERAGES_AMOUNT);
        periodData = new ArrayList(Lists.partition(measurements, (int) measurementsInPeriod));

        if (periodData.size() > 0) {
            synchronized (periodData) {
                for (List<Measurement> dataChunk : Lists.reverse(periodData)) {
                    if (dataChunk.size() > measurementsInPeriod - getTolerance(streamFrequency)) {
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

        averages.put(getKey(sessionId, stream.getSensorName()), Lists.reverse(entries));
    }

    private double getTolerance(double streamFrequency) {
        if (0.9 <= streamFrequency && streamFrequency < 1.1) {
            return 3.5;
        } else {
            return  6.5;
        }
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

        if (dynamicAveragesCount < MAX_AVERAGES_AMOUNT) {
            dynamicAveragesCount++;
        }

        for (MeasurementStream stream : streams) {
            prepareEntries(Constants.CURRENT_SESSION_FAKE_ID, stream);
        }
    }

    private boolean shouldStaticChartInitialize() {
        return !staticChartGeneratedForStream.containsKey(requestedStreamKey);
    }

    private ArrayList<Integer> prepareDataSetColors(List<Entry> entries) {
        ArrayList colors = new ArrayList<Integer>();

        for (Entry entry : entries) {
            Sensor sensor = getSensor();
            colors.add(resourceHelper.getColorAbsolute(sensor, entry.getY()));
        }

        return colors;
    }

    private String getKey(double sessionId, String sensorName) {
        return String.valueOf(sessionId) + "_" + sensorName;
    }

    private List<Entry> getEntriesForCurrentStream() {
        return averages.get(requestedStreamKey);
    }

    private MeasurementStream getStream() {
        MeasurementStream stream = sessionData.getStream(requestedSensorName, requestedSessionId);
        return stream;
    }

    private Sensor getSensor() {
        Sensor sensor = sessionData.getSensor(requestedSensorName, requestedSessionId);

        return sensor;
    }

    private String getTimestamp(MeasurementStream stream) {
        double time;
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

        if (!isSessionCurrent) {
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
