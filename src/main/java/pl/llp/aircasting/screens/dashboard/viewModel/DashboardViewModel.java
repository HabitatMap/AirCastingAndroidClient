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

import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.internal.SensorName;
import pl.llp.aircasting.screens.common.ApplicationState;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionManager;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionSensorManager;
import pl.llp.aircasting.screens.common.sessionState.ViewingSessionsManager;
import pl.llp.aircasting.screens.common.sessionState.ViewingSessionsSensorManager;
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
    public static final String STREAM_IDENTIFIER = "stream_identifier";
    public static final String STREAM_TIMESTAMP = "stream_timestamp";
    public static final String TITLE_DISPLAY = "title_display";
    public static final String STREAM_RECENT_MEASUREMENT = "stream_recent_measurement";

    private CurrentSessionManager mCurrentSessionManager;
    private CurrentSessionSensorManager mCurrentSessionSensorManager;
    private ViewingSessionsManager mViewingSessionsManager;
    private ViewingSessionsSensorManager mViewingSessionsSensorManager;
    private DashboardChartManager mDashboardChartManager;
    private ApplicationState mState;

    // current session data
    private MediatorLiveData<Map<SensorName, Sensor>> mCurrentSensors = new MediatorLiveData<>();
    private MediatorLiveData<Map<String, Double>> mRecentMeasurements = new MediatorLiveData<>();
    private MediatorLiveData<Map<String, LineChart>> mLiveCharts = new MediatorLiveData<>();
    private MediatorLiveData<List> mDashboardStreamData = new MediatorLiveData<>();

    // viewing sessions data
    private MediatorLiveData<Map <Long, Map<SensorName, Sensor>>> mViewingSensors = new MediatorLiveData<>();
    private MediatorLiveData<List> mViewingDashboardData = new MediatorLiveData<>();
    private MediatorLiveData<Map<String, LineChart>> mStaticCharts = new MediatorLiveData<>();
    private MediatorLiveData<Map<String, Measurement>> mRecentFixedMeasurements = new MediatorLiveData<>();

    public DashboardViewModel(CurrentSessionManager currentSessionManager,
                              CurrentSessionSensorManager currentSessionSensorManager,
                              ViewingSessionsManager viewingSessionsManager,
                              ViewingSessionsSensorManager viewingSessionsSensorManager,
                              DashboardChartManager dashboardChartManager,
                              ApplicationState applicationState) {

        this.mCurrentSessionManager = currentSessionManager;
        this.mCurrentSessionSensorManager = currentSessionSensorManager;
        this.mViewingSessionsManager = viewingSessionsManager;
        this.mViewingSessionsSensorManager = viewingSessionsSensorManager;
        this.mDashboardChartManager = dashboardChartManager;
        this.mState = applicationState;
    }

    public void init() {
        refreshCurrentSensors();
        refreshRecentMeasurements();
        refreshLiveCharts();
        refreshViewingSensors();
        refreshStaticCharts();
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

    public void refreshStaticCharts() {
        mStaticCharts.addSource(mDashboardChartManager.getStaticCharts(), new Observer<Map<String, LineChart>>() {
            @Override
            public void onChanged(@Nullable Map<String, LineChart> staticCharts) {
                mStaticCharts.postValue(staticCharts);
            }
        });
    }

    public void refreshViewingSensors() {
        mViewingSensors.addSource(mViewingSessionsSensorManager.getViewingSensorsData(), new Observer<Map<Long, Map<SensorName, Sensor>>>() {
            @Override
            public void onChanged(@Nullable Map<Long, Map<SensorName, Sensor>> viewingSensors) {
                mViewingSensors.postValue(viewingSensors);
            }
        });
    }

    public void refreshRecentFixedMeasurements() {
        mRecentFixedMeasurements.addSource(mViewingSessionsSensorManager.getRecentFixedMeasurements(), new Observer<Map<String, Measurement>>() {
            @Override
            public void onChanged(@Nullable Map<String, Measurement> recentFixedMeasurements) {
                mRecentFixedMeasurements.postValue(recentFixedMeasurements);
            }
        });
    }

    public LiveData<Map<SensorName, Sensor>> getCurrentSensors() {
        return mCurrentSensors;
    }

    public MutableLiveData<Map<String, Double>> getRecentMeasurements() {
        return mRecentMeasurements;
    }

    public MutableLiveData<Map<String, LineChart>> getLiveCharts() {
        return mLiveCharts;
    }

    public LiveData<Map<Long, Map<SensorName, Sensor>>> getViewingSensors() {
        return mViewingSensors;
    }

    public LiveData<Map<String, LineChart>> getStaticCharts() {
        return mStaticCharts;
    }

    public LiveData<Map<String, Measurement>> getRecentFixedMeasurements() {
        return mRecentFixedMeasurements;
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
                    map.put(STREAM_CHART, mDashboardChartManager.getLiveChart(sensor));

                    mDashboardStreamData.getValue().add(map);
               }
            }
        }

        return mDashboardStreamData;
    }

    public LiveData<List> getViewingDashboardData() {
        mViewingDashboardData.setValue(new ArrayList());

        if (getViewingSensors().getValue() != null) {
            if (getViewingSensors().getValue().size() != mViewingDashboardData.getValue().size()) {
                for (Map.Entry<Long, Map<SensorName, Sensor>> entry : mViewingSensors.getValue().entrySet()) {
                    final Long sessionId = entry.getKey();
                    Map<SensorName, Sensor> sensors = entry.getValue();

                    for (final Sensor sensor : sensors.values()) {
                        HashMap map = new HashMap();

                        map.put(SESSION_ID, sessionId);
                        map.put(SENSOR, sensor);
                        map.put(SESSION, mViewingSessionsManager.getSession(sessionId));
                        map.put(SESSION_RECORDING, false);
                        map.put(STREAM_RECENT_MEASUREMENT, getStreamRecentMeasurementValue(sensor, sessionId));
                        map.put(REORDER_IN_PROGRESS, mState.dashboardState.isSessionReorderInProgress());
                        map.put(STREAM_CHART, mDashboardChartManager.getStaticChart(sensor, sessionId));
                        map.put(STREAM_IDENTIFIER, getStreamIdentifier(sensor, sessionId));
                        map.put(STREAM_TIMESTAMP, mViewingSessionsManager.getSession(sessionId).getStream(sensor.getSensorName()).getLastMeasurementTime());

                        mViewingDashboardData.getValue().add(map);
                    }
                }
            }
        }

        return mViewingDashboardData;
    }

    private Double getStreamRecentMeasurementValue(Sensor sensor, Long sessionId) {
        if (mViewingSessionsManager.isSessionFixed(sessionId)) {
            return mViewingSessionsManager.getMeasurementStream(sessionId, sensor.getSensorName()).getLatestMeasurementValue();
        } else {
            return Double.valueOf(0);
        }
    }

    private String getStreamIdentifier(Sensor sensor, long sessionId) {
        return String.valueOf(sessionId) + "_" + sensor.getSensorName();
    }

    public void clearAllViewingSensors() {
        mViewingSessionsManager.removeAllSessions();
        mViewingSessionsSensorManager.removeAllSessionsSensors();
        mDashboardChartManager.resetState();

        mViewingSensors.postValue(new HashMap<Long, Map<SensorName, Sensor>>());

    public void hideStream(Map dataItem) {
        mViewingSessionsSensorManager.hideSessionStream(dataItem);
    }

    public void refreshChartAverages() {
        if (!mViewingSensors.getValue().isEmpty()) {
            for (Map.Entry<Long, Map<SensorName, Sensor>> entry : mViewingSensors.getValue().entrySet()) {
                if (mViewingSessionsManager.isSessionFixed(entry.getKey())) {
                    for (final Sensor sensor : entry.getValue().values()) {
                        mDashboardChartManager.updateFixedAverage(sensor, entry.getKey());
                    }
                }
            }
        }
    }
}
