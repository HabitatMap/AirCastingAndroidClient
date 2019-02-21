package pl.llp.aircasting.screens.dashboard.viewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.google.common.collect.ComparisonChain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.internal.SensorName;
import pl.llp.aircasting.screens.common.ApplicationState;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionManager;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionSensorManager;
import pl.llp.aircasting.screens.dashboard.DashboardChartManager;
import pl.llp.aircasting.util.Constants;

import static com.google.inject.internal.Maps.newHashMap;
import static java.util.Collections.sort;

public class DashboardViewModel extends ViewModel {
    public static final String SENSOR = "sensor";
    public static final String SESSION = "session";
    public static final String SESSION_ID = "session_id";
    public static final String BACKGROUND_COLOR = "background_color";
    public static final String REORDER_IN_PROGRESS = "reorder_in_progress";
    public static final String STREAM_CHART = "stream_chart";
    public static final String SESSION_RECORDING = "session_recording";


    private CurrentSessionManager mCurrentSessionManager;
    private CurrentSessionSensorManager mCurrentSessionSensorManager;
    private DashboardChartManager mDashboardChartManager;
    private ApplicationState mState;

    private MediatorLiveData<Session> mCurrentSession = new MediatorLiveData<>();
    private MediatorLiveData<Map<SensorName, Sensor>> mCurrentSensors = new MediatorLiveData<>();
    private MediatorLiveData<Map<String, Double>> mRecentMeasurements = new MediatorLiveData<>();
    private MediatorLiveData<Map<String, LineChart>> mLiveCharts = new MediatorLiveData<>();
    private MediatorLiveData<List> mDashboardStreamData = new MediatorLiveData<>();
    private MediatorLiveData<TreeMap> mRecentMeasurementsData = new MediatorLiveData<>();

    public DashboardViewModel(CurrentSessionManager currentSessionManager,
                              CurrentSessionSensorManager currentSessionSensorManager,
                              DashboardChartManager dashboardChartManager,
                              ApplicationState applicationState) {

        this.mCurrentSessionManager = currentSessionManager;
        this.mCurrentSessionSensorManager = currentSessionSensorManager;
        this.mDashboardChartManager = dashboardChartManager;
        this.mState = applicationState;
    }

    public void init() {
        refreshCurrentSensors();
        refreshRecentMeasurements();
        refreshLiveCharts();
    }

    public void refreshCurrentSensors() {
        mCurrentSensors.addSource(mCurrentSessionSensorManager.getCurrentSensorsData(), new Observer<Map<SensorName, Sensor>>() {
            @Override
            public void onChanged(@Nullable Map<SensorName, Sensor> sensorNameSensorMap) {
                mCurrentSensors.postValue(sensorNameSensorMap);
            }
        });
    }

    public void refreshRecentMeasurements() {
        mRecentMeasurements.addSource(mCurrentSessionSensorManager.getRecentMeasurements(), new Observer<Map<String, Double>>() {
            @Override
            public void onChanged(@Nullable Map<String, Double> recentMeasurements) {
                mRecentMeasurements.postValue(recentMeasurements);
            }
        });
    }

    public void refreshLiveCharts() {
        mLiveCharts.addSource(mDashboardChartManager.getCurrentCharts(), new Observer<Map<String, LineChart>>() {
            @Override
            public void onChanged(@Nullable Map<String, LineChart> liveCharts) {
                mLiveCharts.postValue(liveCharts);
            }
        });
    }

    public LiveData<Map<SensorName, Sensor>> getCurrentSensors() {
        Log.w("Dashboard viewModel", "getCurrentSensors");
        return mCurrentSensors;
    }

    public MutableLiveData<Map<String, Double>> getRecentMeasurements() {
        return mRecentMeasurements;
    }

    public MutableLiveData<Map<String, LineChart>> getLiveCharts() {
        return mLiveCharts;
    }

    public LiveData<List> getCurrentDashboardData() {
        mDashboardStreamData.setValue(new ArrayList());

        if (getCurrentSensors().getValue() != null) {
            if (getCurrentSensors().getValue().size() != mDashboardStreamData.getValue().size()) {

                for (Map.Entry entry : getCurrentSensors().getValue().entrySet()) {
                    HashMap map = new HashMap();
                    Sensor sensor = (Sensor) entry.getValue();

                    map.put(SESSION_ID, Constants.CURRENT_SESSION_FAKE_ID);
                    map.put(SENSOR, sensor);
                    map.put(SESSION, mCurrentSessionManager.getCurrentSession());
                    map.put(SESSION_RECORDING, mState.recording().isRecording());
                    map.put(REORDER_IN_PROGRESS, mState.dashboardState.isSessionReorderInProgress());
                    map.put(STREAM_CHART, mDashboardChartManager.getLiveChart(sensor));

                    mDashboardStreamData.getValue().add(map);
                }
            }
        }

        return mDashboardStreamData;
    }

    public LiveData<TreeMap> getRecentMeasurementsData() {
        mRecentMeasurementsData.setValue(new TreeMap<>());

        TreeMap recentMeasurements = new TreeMap();
        for (Map.Entry entry : getRecentMeasurements().getValue().entrySet()) {
            recentMeasurements.put(entry.getKey(), entry.getValue());
        }

        mRecentMeasurementsData.setValue(recentMeasurements);

        return mRecentMeasurementsData;
    }
}
