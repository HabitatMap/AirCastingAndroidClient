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
    private final static int MINUTES_IN_HOUR = 60;
    private final static int MAX_AVERAGES_AMOUNT = 9;
    private final static int MOBILE_INTERVAL = 1000 * INTERVAL_IN_SECONDS; // 1 minute
    private final static int MAX_X_VALUE = 8;
    private final static double MOBILE_FREQUENCY_DIVISOR = 8 * 1000;
    private final static double FIXED_FREQUENCY_DIVISOR = 8 * 1000 * 60;
    private final static String FIXED_LABEL = "1 hr avg";
    private final static String MOBILE_LABEL = "1 min avg";
    private static int dynamicAveragesCount = 0;
    private static Map<String, Boolean> staticChartGeneratedForStream = newHashMap();
    private static Map<String, List> averages = newHashMap();
    private static String requestedSensorName;
    private static String requestedStreamKey;
    private static long requestedSessionId;
    private boolean isSessionCurrent;
    private boolean isSessionFixed;
    private Handler handler = new Handler();

    private Runnable updateEntriesTask = new Runnable() {
        @Override
        public void run() {
            allowChartUpdate();
            handler.postDelayed(updateEntriesTask, MOBILE_INTERVAL);
        }
    };

    public void start() {
        resetState();
        handler.postDelayed(updateEntriesTask, MOBILE_INTERVAL);
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
        isSessionFixed = sessionData.getSession(sessionId).isFixed();
        requestedStreamKey = getKey(requestedSessionId, requestedSensorName);

        draw(chart);
        chart.clear();

        if (stream == null) {
            return;
        }

        initStaticChart(stream);
        updateChart(chart, sensor.getSymbol());
    }

    private void initStaticChart(MeasurementStream stream) {
        if (isSessionFixed) {
            prepareFixedSessionEntries(requestedSessionId, stream);
        } else if (!isSessionFixed && !isSessionCurrent && shouldStaticChartInitialize()) {
            prepareEntries(requestedSessionId, stream);
            staticChartGeneratedForStream.put(requestedStreamKey, true);
        }
    }

    private void updateChart(LineChart chart, String unitSymbol) {
        updateChartData(chart, unitSymbol);
        chart.notifyDataSetChanged();
    }

    private void updateChartData(LineChart chart, String unitSymbol) {
        LineData lineData = chart.getLineData();

        if (lineData == null) {
            lineData = new LineData();
        }

        List<Entry> entries = getEntriesForCurrentStream();
        if (entries == null) {
            return;
        }

        LineDataSet dataSet = prepareDataSet(unitSymbol);

        lineData.removeDataSet(0);
        lineData.addDataSet(dataSet);
        lineData.setValueFormatter(new MyValueFormatter());

        chart.setData(lineData);
    }

    private void draw(LineChart chart) {
        YAxis leftAxis = chart.getAxisLeft();
        YAxis rightAxis = chart.getAxisRight();
        XAxis xAxis = chart.getXAxis();
        Legend legend = chart.getLegend();
        Description desc = new Description();

        desc.setText("");
        chart.setDescription(desc);

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

        chart.setNoDataText("");
        chart.setPinchZoom(false);
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setDoubleTapToZoomEnabled(false);
    }

    private LineDataSet prepareDataSet(String unitSymbol) {
        List<Entry> entries = getEntriesForCurrentStream();
        LineDataSet dataSet = new LineDataSet(entries, getDatasetLabel(unitSymbol));
        ArrayList<Integer> colors = prepareDataSetColors(entries);

        dataSet.setDrawCircleHole(false);
        dataSet.setCircleRadius(7);
        dataSet.setLineWidth(3);
        dataSet.setValueTextSize(10);
        dataSet.setDrawHighlightIndicators(false);
        dataSet.setCircleColors(colors);

        int color = context.getResources().getColor(R.color.gray);
        dataSet.setColor(color);

        return dataSet;
    }

    private String getDatasetLabel(String unitSymbol) {
        if (sessionData.getSession(requestedSessionId).isFixed()) {
            return FIXED_LABEL + " - " + unitSymbol;
        } else {
            return MOBILE_LABEL + " - " + unitSymbol;
        }
    }

    private void prepareEntries(long sessionId, MeasurementStream stream) {
        List<List<Measurement>> periodData;
        double streamFrequency = stream.getFrequency(MOBILE_FREQUENCY_DIVISOR);
        double xValue = MAX_X_VALUE;
        double measurementsInPeriod = INTERVAL_IN_SECONDS / streamFrequency;
        List entries = new CopyOnWriteArrayList();
        List<Measurement> measurements = stream.getMeasurementsForPeriod(MAX_AVERAGES_AMOUNT, MOBILE_FREQUENCY_DIVISOR, 0);
        periodData = new ArrayList(Lists.partition(measurements, (int) measurementsInPeriod));

        if (periodData.size() > 0) {
            synchronized (periodData) {
                for (List<Measurement> dataChunk : Lists.reverse(periodData)) {
                    if (dataChunk.size() > measurementsInPeriod - getTolerance(measurementsInPeriod)) {
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

    private void prepareFixedSessionEntries(long sessionId, MeasurementStream stream) {
        List<Measurement> measurements;
        double xValue = MAX_X_VALUE;
        List entries = new CopyOnWriteArrayList();
        List<List<Measurement>> periodData = new ArrayList<List<Measurement>>();

        double streamFrequency = stream.getFrequency(FIXED_FREQUENCY_DIVISOR);
        double measurementsInPeriod = INTERVAL_IN_SECONDS / streamFrequency;
        double maxMeasurementsAmount = MAX_AVERAGES_AMOUNT * measurementsInPeriod;

        if (stream.getMeasurementsCount() < maxMeasurementsAmount) {
            measurements = stream.getMeasurementsForPeriod(MAX_AVERAGES_AMOUNT, FIXED_FREQUENCY_DIVISOR, 0);

            if (measurements.isEmpty()) { return; }

            Date firstMeasurementTime = measurements.get(0).getTime();
            double minutes = firstMeasurementTime.getMinutes();

            double firstHourCutoff = MINUTES_IN_HOUR - minutes;
            if (measurements.size() > firstHourCutoff) {
                List<Measurement> firstHourMeasurements = measurements.subList(0, (int) firstHourCutoff - 1);
                if (!firstHourMeasurements.isEmpty()) {
                    periodData.add(firstHourMeasurements);
                }
                List<Measurement> measurementsRemainder = measurements.subList((int) (firstHourCutoff), measurements.size() - 1);
                measurements = measurementsRemainder;
            } else {
                return;
            }
        } else {
            int offset = (int) (sessionData.getSession(sessionId).getEnd().getMinutes() / streamFrequency);
            measurements = stream.getMeasurementsForPeriod(MAX_AVERAGES_AMOUNT, FIXED_FREQUENCY_DIVISOR, offset);
        }

        periodData.addAll(new ArrayList(Lists.partition(measurements, (int) measurementsInPeriod)));

        List<Measurement> lastPeriodData = periodData.get(periodData.size() - 1);

        // remove incomplete last hour average
        if (periodData.size() > 1 && lastPeriodData.size() < measurementsInPeriod - getTolerance(measurementsInPeriod)) {
            periodData.remove(lastPeriodData);
        }

        if (periodData.size() > 0) {
            synchronized (periodData) {
                for (List<Measurement> dataChunk : Lists.reverse(periodData)) {
                    double yValue = getAverage(dataChunk);
                    entries.add(new Entry((float) xValue, (float) yValue));
                    xValue--;
                }
            }
        }

        if (entries.size() == 0) {
            return;
        }

        averages.put(getKey(sessionId, stream.getSensorName()), Lists.reverse(entries));
    }

    private double getTolerance(double measurementsInPeriod) {
        return 0.1 * measurementsInPeriod;
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
