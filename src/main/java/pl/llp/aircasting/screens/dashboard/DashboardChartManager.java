package pl.llp.aircasting.screens.dashboard;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
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
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.R;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionManager;
import pl.llp.aircasting.screens.common.helpers.ResourceHelper;
import pl.llp.aircasting.screens.common.sessionState.SessionDataFactory;
import pl.llp.aircasting.model.*;
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
    @Inject ResourceHelper mResourceHelper;
    @Inject EventBus eventBus;
    @Inject Context mContext;
    @Inject SessionDataFactory mSessionData;

    private final static int INTERVAL_IN_SECONDS = 60;
    private final static int MOBILE_INTERVAL = 1000 * INTERVAL_IN_SECONDS; // 1 minute
    private final static String FIXED_LABEL = "1 hr avg";
    private final static String MOBILE_LABEL = "1 min avg";

    private static Map<String, LineChart> mLiveCharts = newHashMap();
    private static Map<String, Boolean> mStaticChartGeneratedForStream = newHashMap();
    private static Map<String, List> mAverages = newHashMap();
    private Sensor mSensor;
    private static String mRequestedSensorName;
    private static String mRequestedStreamKey;
    private static long mRequestedSessionId;
    private boolean mIsSessionCurrent;
    private boolean mIsSessionFixed;
    private Handler mHandler = new Handler();

    private Runnable updateEntriesTask = new Runnable() {
        @Override
        public void run() {
            allowChartUpdate();
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

    private void resetState() {
        mAverages.clear();
        resetAllStaticCharts();
    }

    public void resetAllStaticCharts() {
        mStaticChartGeneratedForStream.clear();
    }

    public void resetSpecificStaticCharts(Long sessionId, String[] sensors) {
        for (String sensorName : sensors) {
            mStaticChartGeneratedForStream.remove(getKey(sessionId, sensorName));
        }
    }

    public LineChart getLiveChart(final Sensor sensor) {
        LineChart chart;

        mSensor = sensor;
        mRequestedSensorName = sensor.getSensorName();
        mRequestedSessionId = Constants.CURRENT_SESSION_FAKE_ID;
        mRequestedStreamKey = getKey(mRequestedSessionId, mRequestedSensorName);

        if (!mLiveCharts.containsKey(mRequestedStreamKey)) {
            chart = new LineChart(mContext);
            draw(chart);
        } else {
            chart = mLiveCharts.get(mRequestedStreamKey);
        }

        updateChartData(mRequestedSensorName, chart, sensor.getSymbol());

        mLiveCharts.put(mRequestedSensorName, chart);

        return chart;
    }

    public LiveData<Map<String, LineChart>> getCurrentCharts() {
        final MutableLiveData<Map<String, LineChart>> data = new MutableLiveData<>();
        data.postValue(mLiveCharts);
        return data;
    }

//    public void drawChart(final Sensor sensor, long sessionId) {
//        LineChart chart = new LineChart(mContext);
//        mSensor = sensor;
//        mRequestedSensorName = sensor.getSensorName();
//        mRequestedSessionId = sessionId;
//        MeasurementStream stream = getStream();
//        mIsSessionCurrent = mRequestedSessionId == Constants.CURRENT_SESSION_FAKE_ID;
//        mIsSessionFixed = mSessionData.getSession(sessionId).isFixed();
//        mRequestedStreamKey = getKey(mRequestedSessionId, mRequestedSensorName);
//
//        chart.clear();
//        draw(chart);
//
//        if (stream == null) {
//            return;
//        }
//
//        if (shouldStaticChartInitialize()) {
//            initStaticChart(stream);
//        }
//
//        updateChartData(chart, sensor.getSymbol());
//    }

    private void initStaticChart(MeasurementStream stream) {
        if (!mIsSessionCurrent) {
            if (mIsSessionFixed) {
                List<Entry> entries = ChartAveragesCreator.getFixedEntries(stream);
                mAverages.put(getKey(mRequestedSessionId, stream.getSensorName()), Lists.reverse(entries));
            } else if (!mIsSessionFixed && shouldStaticChartInitialize()) {
                List<Entry> entries = ChartAveragesCreator.getMobileEntries(stream);
                mAverages.put(mRequestedStreamKey, Lists.reverse(entries));
            }

            mStaticChartGeneratedForStream.put(mRequestedStreamKey, true);
        }
    }

    private void updateChartData(String sensorName, LineChart chart, String unitSymbol) {
        List<Entry> entries = getEntriesForStream(sensorName);
        if (entries == null || entries.isEmpty()) {
            return;
        }

        LineData lineData = new LineData();
        LineDataSet dataSet = prepareDataSet(sensorName, unitSymbol);

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

    private LineDataSet prepareDataSet(String sensorName, String unitSymbol) {
        List<Entry> entries = getEntriesForStream(sensorName);
        LineDataSet dataSet = new LineDataSet(entries, getDatasetLabel(unitSymbol));
        ArrayList<Integer> colors = prepareDataSetColors(entries);

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

    private String getDatasetLabel(String unitSymbol) {
        if (mSessionData.getSession(mRequestedSessionId).isFixed()) {
            return FIXED_LABEL + " - " + unitSymbol;
        } else {
            return MOBILE_LABEL + " - " + unitSymbol;
        }
    }

    private void allowChartUpdate() {
        List<MeasurementStream> streams = (List) mCurrentSessionManager.getMeasurementStreams();

        for (MeasurementStream stream : streams) {
            String sensorName = stream.getSensorName();
            mRequestedStreamKey = getKey(Constants.CURRENT_SESSION_FAKE_ID, sensorName);
            List<Entry> entries = ChartAveragesCreator.getMobileEntries(stream);

            mAverages.put(mRequestedStreamKey, Lists.reverse(entries));
            LineChart chart = mLiveCharts.get(sensorName);
            updateChartData(sensorName, chart, stream.getSymbol());

            mLiveCharts.put(sensorName, chart);
        }

        eventBus.post(new NewChartAveragesEvent());
    }

    private boolean shouldStaticChartInitialize() {
        return !mStaticChartGeneratedForStream.containsKey(mRequestedStreamKey);
    }

    private ArrayList<Integer> prepareDataSetColors(List<Entry> entries) {
        ArrayList colors = new ArrayList<Integer>();

        for (Entry entry : entries) {
            colors.add(mResourceHelper.getColorAbsolute(mSensor, entry.getY()));
        }

        return colors;
    }

    private String getKey(double sessionId, String sensorName) {
        return String.valueOf(sessionId) + "_" + sensorName;
    }

    private List<Entry> getEntriesForStream(String sensorName) {
        return mAverages.get(getKey(Constants.CURRENT_SESSION_FAKE_ID, sensorName));
    }

    private MeasurementStream getStream() {
        MeasurementStream stream = mSessionData.getStream(mRequestedSensorName, mRequestedSessionId);
        return stream;
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
