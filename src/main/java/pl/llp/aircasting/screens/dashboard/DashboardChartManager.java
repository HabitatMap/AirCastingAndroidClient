package pl.llp.aircasting.screens.dashboard;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
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
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.R;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionManager;
import pl.llp.aircasting.screens.common.helpers.ResourceHelper;
import pl.llp.aircasting.model.*;
import pl.llp.aircasting.screens.common.sessionState.ViewingSessionsManager;
import pl.llp.aircasting.screens.dashboard.events.NewChartAveragesEvent;
import pl.llp.aircasting.util.Constants;

import java.text.DecimalFormat;
import java.util.*;

import static com.google.common.collect.Maps.newHashMap;

/**
 * Created by radek on 06/07/17.
 */
@Singleton
public class DashboardChartManager {
    @Inject CurrentSessionManager mCurrentSessionManager;
    @Inject ViewingSessionsManager mViewingSessionsManager;
    @Inject ResourceHelper mResourceHelper;
    @Inject EventBus eventBus;
    @Inject Context mContext;

    private final static int INTERVAL_IN_SECONDS = 60;
    private final static int MOBILE_INTERVAL = 1000 * INTERVAL_IN_SECONDS; // 1 minute
    private final static String FIXED_LABEL = "1 hr avg";
    private final static String MOBILE_LABEL = "1 min avg";

    public final static String CURRENT_CHART = "current_chart";
    public final static String STATIC_CHART = "static_chart";

    private static Map<String, LineChart> mCurrentCharts = newHashMap();
    private static Map<String, LineChart> mStaticCharts = newHashMap();
    private static Map<String, List> mAverages = newHashMap();
    private Map<String, Sensor> mSensors = newHashMap();
    private Handler mHandler = new Handler();

    private Runnable updateEntriesTask = new Runnable() {
        @Override
        public void run() {
            prepareCurrentEntries();
            mHandler.postDelayed(updateEntriesTask, MOBILE_INTERVAL);
        }
    };

    public void start() {
        resetState();
        mHandler.postDelayed(updateEntriesTask, MOBILE_INTERVAL);
    }

    public void stop() {
        resetState();
        mHandler.removeCallbacks(updateEntriesTask);
    }

    public void resetState() {
        mSensors.clear();
        mAverages.clear();
        mCurrentCharts.clear();
        mStaticCharts.clear();
    }

    public LiveData<Map<String, LineChart>> getCurrentCharts() {
        final MutableLiveData<Map<String, LineChart>> data = new MutableLiveData<>();
        data.postValue(mCurrentCharts);
        return data;
    }

    public LiveData<Map<String, LineChart>> getStaticCharts() {
        final MutableLiveData<Map<String, LineChart>> data = new MutableLiveData<>();
        data.postValue(mStaticCharts);
        return data;
    }

    public LineChart getLiveChart(final Sensor sensor) {
        LineChart chart;
        String sensorName = sensor.getSensorName();

        if (!mSensors.containsKey(sensorName)) {
            mSensors.put(sensorName, sensor);
        }
        String streamKey = getKey(Constants.CURRENT_SESSION_FAKE_ID, sensorName);

        if (!mCurrentCharts.containsKey(streamKey)) {
            chart = new LineChart(mContext);
            draw(chart);
        } else {
            chart = mCurrentCharts.get(streamKey);
        }

        setChartDataset(sensorName, Constants.CURRENT_SESSION_FAKE_ID, chart, sensor.getSymbol(), CURRENT_CHART);

        mCurrentCharts.put(sensorName, chart);

        return chart;
    }

    public Object getStaticChart(Sensor sensor, Long sessionId) {
        LineChart chart;
        String sensorName = sensor.getSensorName();

        if (!mSensors.containsKey(sensorName)) {
            mSensors.put(sensorName, sensor);
        }

        String streamKey = getKey(sessionId, sensorName);

        if (!mStaticCharts.containsKey(streamKey)) {
            chart = new LineChart(mContext);
            draw(chart);
        } else {
            chart = mStaticCharts.get(streamKey);
        }

        prepareStaticEntries(chart, sessionId, sensor);

        return chart;
    }

    public void updateFixedAverage(Sensor sensor, long sessionId) {
        LineChart chart = mStaticCharts.get(getKey(sessionId, sensor.getSensorName()));
        if (chart == null) {
            chart = new LineChart(mContext);
            draw(chart);
        }
        prepareStaticEntries(chart, sessionId, sensor);
        eventBus.post(new NewChartAveragesEvent(STATIC_CHART));
    }

    public void updateFixedAverageWithMeasurement(Sensor sensor, long sessionId, Measurement measurement) {
        if (measurement.getTime().getMinutes() == 0) {
            prepareStaticEntries(mStaticCharts.get(getKey(sessionId, sensor.getSensorName())), sessionId, sensor);
            eventBus.post(new NewChartAveragesEvent(STATIC_CHART));
        }
    }

    private void prepareCurrentEntries() {
        List<MeasurementStream> streams = (List) mCurrentSessionManager.getMeasurementStreams();

        for (MeasurementStream stream : streams) {
            String sensorName = stream.getSensorName();
            String streamKey = getKey(Constants.CURRENT_SESSION_FAKE_ID, sensorName);
            List<Entry> entries = ChartAveragesCreator.getMobileEntries(stream);

            mAverages.put(streamKey, Lists.reverse(entries));
            LineChart chart = mCurrentCharts.get(sensorName);
            setChartDataset(sensorName, Constants.CURRENT_SESSION_FAKE_ID, chart, stream.getSymbol(), CURRENT_CHART);

            mCurrentCharts.put(sensorName, chart);
        }

        eventBus.post(new NewChartAveragesEvent(CURRENT_CHART));
    }

    private void prepareStaticEntries(LineChart chart, Long sessionId, Sensor sensor) {
        List<Entry> entries;
        String sensorName = sensor.getSensorName();
        MeasurementStream stream = mViewingSessionsManager.getMeasurementStream(sessionId, sensorName);
        String streamKey = getKey(sessionId, sensorName);

        if (mViewingSessionsManager.isSessionFixed(sessionId)) {
            entries = ChartAveragesCreator.getFixedEntries(stream);
        } else {
            entries = ChartAveragesCreator.getMobileEntries(stream);
        }

        mAverages.put(streamKey, Lists.reverse(entries));

        setChartDataset(sensorName, sessionId, chart, stream.getSymbol(), STATIC_CHART);
        mStaticCharts.put(streamKey, chart);

        Log.w(sensorName, String.valueOf(mAverages.get(streamKey)));
    }

    private void setChartDataset(String sensorName, Long sessionId, LineChart chart, String unitSymbol, String chartType) {
        List<Entry> entries;
        String datasetLabel;

        if (chartType.equals(STATIC_CHART)) {
            entries = getStaticEntriesForStream(sensorName, sessionId);
        } else {
            entries = getEntriesForStream(sensorName);
        }

        if (entries == null || entries.isEmpty()) {
            return;
        }

        if (mViewingSessionsManager.isSessionFixed(sessionId)) {
            datasetLabel = FIXED_LABEL;
        } else {
            datasetLabel = MOBILE_LABEL;
        }

        LineData lineData = new LineData();
        LineDataSet dataSet = prepareDataSet(entries, sensorName, datasetLabel);

        lineData.removeDataSet(0);
        lineData.addDataSet(dataSet);
        lineData.setValueFormatter(new MyValueFormatter());

        chart.clear();
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

    private LineDataSet prepareDataSet(List<Entry> entries, String sensorName, String datasetLabel) {
        LineDataSet dataSet = new LineDataSet(entries, datasetLabel);
        ArrayList<Integer> colors = prepareDataSetColors(sensorName, entries);

        dataSet.setDrawCircleHole(false);
        dataSet.setCircleRadius(7);
        dataSet.setLineWidth(3);
        dataSet.setValueTextSize(10);
        dataSet.setDrawHighlightIndicators(false);
        dataSet.setCircleColors(colors);

        int color = mContext.getResources().getColor(R.color.gray);
        dataSet.setColor(color);

        return dataSet;
    }

    private ArrayList<Integer> prepareDataSetColors(String sensorName, List<Entry> entries) {
        ArrayList colors = new ArrayList<Integer>();

        for (Entry entry : entries) {
            colors.add(mResourceHelper.getColorAbsolute(mSensors.get(sensorName), entry.getY()));
        }

        return colors;
    }

    private String getKey(long sessionId, String sensorName) {
        return String.valueOf(sessionId) + "_" + sensorName;
    }

    private List<Entry> getEntriesForStream(String sensorName) {
        return mAverages.get(getKey(Constants.CURRENT_SESSION_FAKE_ID, sensorName));
    }

    private List<Entry> getStaticEntriesForStream(String sensorName, Long sessionId) {
        return mAverages.get(getKey(sessionId, sensorName));
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
