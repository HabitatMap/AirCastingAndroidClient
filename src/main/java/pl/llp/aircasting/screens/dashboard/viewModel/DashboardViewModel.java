package pl.llp.aircasting.screens.dashboard.viewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.internal.SensorName;
import pl.llp.aircasting.screens.common.ApplicationState;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionManager;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionSensorManager;
import pl.llp.aircasting.util.Constants;

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

//    @Inject CurrentSessionManager mCurrentSessionManager;
//    @Inject CurrentSessionSensorManager mCurrentSessionSensorManager;
//    @Inject ApplicationState mState;

    private CurrentSessionManager mCurrentSessionManager;
    private CurrentSessionSensorManager mCurrentSessionSensorManager;
    private ApplicationState mState;

    private MediatorLiveData<Session> mCurrentSession = new MediatorLiveData<>();
    private MediatorLiveData<Map<SensorName, Sensor>> mCurrentSensors = new MediatorLiveData<>();
    private MutableLiveData<Map<String, Double>> mRecentMeasurements = new MutableLiveData<>();
    private MediatorLiveData<List> mDashboardStreamData = new MediatorLiveData<>();

    public DashboardViewModel(CurrentSessionManager currentSessionManager, CurrentSessionSensorManager currentSessionSensorManager, ApplicationState applicationState) {
        this.mCurrentSessionManager = currentSessionManager;
        this.mCurrentSessionSensorManager = currentSessionSensorManager;
        this.mState = applicationState;
    }

    public void init() {
        refreshCurrentSession();
        refreshCurrentSensors();
    }

    public void refreshCurrentSensors() {
        mCurrentSensors.addSource(mCurrentSessionSensorManager.getCurrentSensorsData(), new Observer<Map<SensorName, Sensor>>() {
            @Override
            public void onChanged(@Nullable Map<SensorName, Sensor> sensorNameSensorMap) {
                mCurrentSensors.postValue(sensorNameSensorMap);
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
//        if (mDashboardStreamData.getValue() == null) {
            mDashboardStreamData.setValue(new ArrayList());
//        }

        Log.w("viewModel sensors size", String.valueOf(getCurrentSensors().getValue().size()));

        if (getCurrentSensors().getValue().size() != mDashboardStreamData.getValue().size()) {
            int count = 0;

            for (Map.Entry entry : getCurrentSensors().getValue().entrySet()) {
                HashMap map = new HashMap();
//                map.put(SESSION_ID, sessionId);
//                map.put(SENSOR, sensor);
//                map.put(SESSION, sessionData.getSession(sessionId));
//                map.put(BACKGROUND_COLOR, sessionState.sessionHasColoredBackground(sessionId));
//                map.put(NOW_VALUE, String.valueOf((int) sessionData.getNow(sensor, sessionId)));
//                map.put(SESSION_CURRENT, sessionState.isSessionCurrent(sessionId));
//                map.put(REORDER_IN_PROGRESS, state.dashboardState.isSessionReorderInProgress());
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
//                count++;
            }
        }
        return mDashboardStreamData;
    }
}
