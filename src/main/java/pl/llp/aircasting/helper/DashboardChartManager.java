package pl.llp.aircasting.helper;

import android.content.Context;
import android.os.Handler;
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
import pl.llp.aircasting.R;
import pl.llp.aircasting.model.*;
import pl.llp.aircasting.util.ChartAveragesCreator;
import pl.llp.aircasting.util.Constants;

import java.text.DecimalFormat;
import java.util.*;

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

    public void drawChart(View view, final Sensor sensor, long sessionId) {
        requestedSensorName = sensor.getSensorName();
        requestedSessionId = sessionId;
        MeasurementStream stream = getStream();
        isSessionCurrent = requestedSessionId == Constants.CURRENT_SESSION_FAKE_ID;
        isSessionFixed = sessionData.getSession(sessionId).isFixed();
        requestedStreamKey = getKey(requestedSessionId, requestedSensorName);
        LineChart chart = (LineChart) view.findViewById(R.id.chart);

        chart.clear();
        draw(chart);

        if (stream == null) {
            return;
        }

        if (shouldStaticChartInitialize()) {
            initStaticChart(stream);
        }

        updateChartData(chart, sensor.getSymbol());
    }

    private void initStaticChart(MeasurementStream stream) {
        if (!isSessionCurrent) {
            if (isSessionFixed) {
                Session session = sessionData.getSession(requestedSessionId);
                List<Entry> entries = ChartAveragesCreator.getFixedEntries(session, stream);
                averages.put(getKey(requestedSessionId, stream.getSensorName()), Lists.reverse(entries));
            } else if (!isSessionFixed && shouldStaticChartInitialize()) {
                List<Entry> entries = ChartAveragesCreator.getMobileEntries(stream);
                averages.put(requestedStreamKey, Lists.reverse(entries));
            }

            staticChartGeneratedForStream.put(requestedStreamKey, true);
        }
    }

    private void updateChartData(LineChart chart, String unitSymbol) {
        List<Entry> entries = getEntriesForCurrentStream();
        if (entries == null || entries.isEmpty()) {
            return;
        }

        LineData lineData = new LineData();
        LineDataSet dataSet = prepareDataSet(unitSymbol);

        lineData.removeDataSet(0);
        lineData.addDataSet(dataSet);
        lineData.setValueFormatter(new MyValueFormatter());

        chart.setData(lineData);

        chart.notifyDataSetChanged();
        chart.invalidate();
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

    private void allowChartUpdate() {
        List<MeasurementStream> streams = (List) currentSessionManager.getMeasurementStreams();

        for (MeasurementStream stream : streams) {
            String streamKey = getKey(Constants.CURRENT_SESSION_FAKE_ID, stream.getSensorName());
            List<Entry> entries = ChartAveragesCreator.getMobileEntries(stream);

            averages.put(streamKey, Lists.reverse(entries));
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
