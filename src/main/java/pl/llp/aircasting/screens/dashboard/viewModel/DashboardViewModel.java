package pl.llp.aircasting.screens.dashboard.viewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;
import android.util.Log;

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
import pl.llp.aircasting.util.Constants;

import static com.google.inject.internal.Maps.newHashMap;
import static java.util.Collections.sort;

public class DashboardViewModel extends ViewModel {
    public static final String SENSOR = "sensor";
    public static final String SESSION = "session";
    public static final String SESSION_ID = "session_id";
    public static final String STREAMS_REORDERED = "streams_reordered";
    public static final String BACKGROUND_COLOR = "background_color";
    public static final String NOW_VALUE_PRESENT = "now_value_present";
    public static final String NOW_VALUE = "now_value";
    public static final String SESSION_CURRENT = "session_current";
    public static final String REORDER_IN_PROGRESS = "reorder_in_progress";

    private final Comparator<Map<String, Object>> mDashboardDataComparator = new Comparator<Map<String, Object>>() {
        @Override
        public int compare(@Nullable Map<String, Object> left, @Nullable Map<String, Object> right) {
            Sensor leftSensor = (Sensor) left.get(SENSOR);
            Sensor rightSensor = (Sensor) right.get(SENSOR);

//            ComparisonChain chain = ComparisonChain.start()
//                    .compare(getSessionPosition(leftSessionId), getSessionPosition(rightSessionId));

//            if (stateRestored || streamsReordered.get(leftSessionId) == true) {
//                result = chain.compare(getPosition(left), getPosition(right)).result();
//            } else {
                return ComparisonChain.start().compare(leftSensor.getSensorName(), rightSensor.getSensorName()).result();
//            }

//            return result;
        }
    };

//    private final Comparator<Map<String, Double>> mRecentMeasurementsComparator = new Comparator<Map<String, Double>>() {
//        @Override
//        public int compare(@Nullable Map<String, Double> leftMap, @Nullable Map<String, Double> rightMap) {
//            return ComparisonChain.start().compare(getPosition(leftMap), getPosition(rightMap)).result();
//        }
//    }

    private CurrentSessionManager mCurrentSessionManager;
    private CurrentSessionSensorManager mCurrentSessionSensorManager;
    private ApplicationState mState;

    private MediatorLiveData<Session> mCurrentSession = new MediatorLiveData<>();
    private MediatorLiveData<Map<SensorName, Sensor>> mCurrentSensors = new MediatorLiveData<>();
    private MediatorLiveData<Map<String, Double>> mRecentMeasurements = new MediatorLiveData<>();
    private MediatorLiveData<List> mDashboardStreamData = new MediatorLiveData<>();
    private MediatorLiveData<TreeMap> mRecentMeasurementsData = new MediatorLiveData<>();
    private HashMap<String, Integer> mPositions = newHashMap();

    public DashboardViewModel(CurrentSessionManager currentSessionManager, CurrentSessionSensorManager currentSessionSensorManager, ApplicationState applicationState) {
        this.mCurrentSessionManager = currentSessionManager;
        this.mCurrentSessionSensorManager = currentSessionSensorManager;
        this.mState = applicationState;
    }

    public void init() {
        refreshCurrentSession();
        refreshCurrentSensors();
        refreshRecentMeasurements();
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

    public void refreshCurrentSession() {
        mCurrentSession.addSource(mCurrentSessionManager.getCurrentSessionData(), new Observer<Session>() {
            @Override
            public void onChanged(@Nullable Session session) {
                mCurrentSession.postValue(session);
            }
        });
    }

    public LiveData<Session> getCurrentSession() {
        return mCurrentSession;
    }

    public LiveData<Map<SensorName, Sensor>> getCurrentSensors() {
        Log.w("Dashboard viewModel", "getCurrentSensors");
        return mCurrentSensors;
    }

    public MutableLiveData<Map<String, Double>> getRecentMeasurements() {
        return mRecentMeasurements;
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
//                map.put(SESSION, getCurrentSession().getValue());
                    map.put(SESSION, mCurrentSessionManager.getCurrentSession());
                    map.put(BACKGROUND_COLOR, mState.recording().isRecording());
//                map.put(NOW_VALUE, getRecentMeasurements().getValue().get(sensor.getSensorName()));
                    map.put(NOW_VALUE, 20);
                    map.put(SESSION_CURRENT, true);
                    map.put(REORDER_IN_PROGRESS, mState.dashboardState.isSessionReorderInProgress());

                    mDashboardStreamData.getValue().add(map);
                }
                prepareStreamPositions(mDashboardStreamData.getValue());
                sort(mDashboardStreamData.getValue(), mDashboardDataComparator);
            }
        }

        return mDashboardStreamData;
    }

    public LiveData<TreeMap> getRecentMeasurementsData() {
        mRecentMeasurementsData.setValue(new TreeMap<>());

        TreeMap recentMeasurements = new TreeMap();
        for (Map.Entry entry : getRecentMeasurements().getValue().entrySet()) {
//            Map map = new TreeMap();
            recentMeasurements.put(entry.getKey(), entry.getValue());
        }

//        sort(recentMeasurements, mRecentMeasurementsComparator);
        mRecentMeasurementsData.setValue(recentMeasurements);

        return mRecentMeasurementsData;
    }

    private void prepareStreamPositions(List<TreeMap<String, Object>> data) {
        mPositions.clear();
        int currentPosition = 0;

        for (Map<String, Object> map : data) {
            Sensor sensor = (Sensor) map.get(SENSOR);
            String positionKey = sensor.toString();
            mPositions.put(positionKey, Integer.valueOf(currentPosition));
            currentPosition++;
        }
    }

//    private int getPosition(Map<String, Double> sensorItem) {
//        return mPositions.get(;
//    }
}
