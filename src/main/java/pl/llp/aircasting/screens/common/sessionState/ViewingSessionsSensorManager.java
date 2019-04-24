package pl.llp.aircasting.screens.common.sessionState;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import pl.llp.aircasting.event.measurements.FixedMeasurementEvent;
import pl.llp.aircasting.event.sensor.FixedSensorEvent;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.event.session.SessionLoadedForViewingEvent;
import pl.llp.aircasting.event.sensor.SessionSensorsLoadedEvent;
import pl.llp.aircasting.model.internal.SensorName;
import pl.llp.aircasting.screens.dashboard.DashboardChartManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newConcurrentMap;
import static com.google.common.collect.Maps.newHashMap;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.SENSOR;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.SESSION_ID;

/**
 * Created by radek on 10/10/17.
 */
@Singleton
public class ViewingSessionsSensorManager {
    @Inject DashboardChartManager mDashboardChartManager;
    @Inject EventBus eventBus;

    private volatile Map<Long, Map<SensorName, Sensor>> viewingSessionsSensors = newConcurrentMap();
    private Map<String, Measurement> mRecentFixedMeasurements = newConcurrentMap();
    private ArrayList<String> mHiddenStreams = new ArrayList<>();
    public static final String PLACEHOLDER_SENSOR_NAME = "Sensor Placeholder";

    @Inject
    public void init() {
        eventBus.register(this);
    }

    public Map<Long, Map<SensorName, Sensor>> getAllViewingSensors() {
        return viewingSessionsSensors;
    }

    public Sensor getSensorByName(String sensorName, long sessionId) {
        return viewingSessionsSensors.get(sessionId).get(SensorName.from(sensorName));
    }

    @Subscribe
    public void onEvent(SessionLoadedForViewingEvent event) {
        Session session = event.getSession();
        long sessionId = session.getId();
        boolean newFixedSession = event.isNewFixedSession();
        Map<SensorName, Sensor> sessionSensors = viewingSessionsSensors.get(session.getId());

        if (sessionSensors == null) {
            sessionSensors = newHashMap();
        }

        if (newFixedSession) {
            addPlaceholderStream(session);
        } else {
            deletePlaceholderSensor(sessionId);
        }

        for (MeasurementStream stream : session.getMeasurementStreams()) {
            if (stream.isMarkedForRemoval()) {
                continue;
            }

            Sensor sensor = new Sensor(stream, sessionId);

            if (mHiddenStreams.contains(sensor.toString())) {
                continue;
            } else {
                sessionSensors.put(SensorName.from(stream.getSensorName()), sensor);
            }

            if (session.isFixed()) {
                if (!stream.getSensorName().equals(PLACEHOLDER_SENSOR_NAME)) {
                    mRecentFixedMeasurements.put(sensor.toString(), stream.getLastMeasurements(1).get(0));
                }
            }
        }

        viewingSessionsSensors.put(session.getId(), sessionSensors);

        notifySensorsChanged();
    }

    private void notifySensorsChanged() {
        eventBus.post(new SessionSensorsLoadedEvent());
    }

    public LiveData<Map<Long, Map<SensorName, Sensor>>> getViewingSensorsData() {
        final MutableLiveData<Map<Long, Map<SensorName, Sensor>>> data = new MutableLiveData<>();
        data.postValue(viewingSessionsSensors);
        return data;
    }

    public LiveData<Map<String, Measurement>> getRecentFixedMeasurements() {
        final MutableLiveData<Map<String, Measurement>> data = new MutableLiveData<>();
        data.postValue(mRecentFixedMeasurements);
        return data;
    }

    @Subscribe
    public void onEvent(FixedMeasurementEvent event) {
        deletePlaceholderSensor(event.getSessionId());
        mRecentFixedMeasurements.put(event.getSensor().toString(), event.getMeasurement());
        mDashboardChartManager.updateFixedAverageWithMeasurement(event.getSensor(), event.getSessionId(), event.getMeasurement());
        eventBus.post(new FixedSensorEvent());
    }

    private void deletePlaceholderSensor(long sessionId) {
        if (viewingSessionsSensors.containsKey(sessionId)) {
            Sensor placeholder = viewingSessionsSensors.get(sessionId).remove(SensorName.from(PLACEHOLDER_SENSOR_NAME));

            if (placeholder != null) {
                notifySensorsChanged();
            }
        }
    }

    public List<Sensor> getSensorsList(long sessionId) {
        ArrayList<Sensor> result = newArrayList();
        result.addAll(viewingSessionsSensors.get(sessionId).values());
        return result;
    }

    public void hideSessionStream(Map dataItem) {
        Sensor sensor = (Sensor) dataItem.get(SENSOR);
        String sensorName = sensor.getSensorName();
        long sessionId = (long) dataItem.get(SESSION_ID);
        viewingSessionsSensors.get(sessionId).remove(SensorName.from(sensorName));
        mHiddenStreams.add(sensor.toString());
        notifySensorsChanged();
    }

    public void deleteSensorFromSession(Sensor sensor, long sessionId) {
        String sensorName = sensor.getSensorName();
        viewingSessionsSensors.get(sessionId).remove(SensorName.from(sensorName));
        notifySensorsChanged();
    }

    public void removeSessionSensors(long sessionId) {
        viewingSessionsSensors.remove(sessionId);
        List toDelete = new ArrayList();
        for (String sensorString : mHiddenStreams) {
            if (sensorString.startsWith(String.valueOf(sessionId))) {
                toDelete.add(sensorString);
            }
        }
        mHiddenStreams.removeAll(toDelete);
        notifySensorsChanged();
    }

    public void removeAllSessionsSensors() {
        mRecentFixedMeasurements.clear();
        viewingSessionsSensors.clear();
        mHiddenStreams.clear();
        notifySensorsChanged();
    }

    private void addPlaceholderStream(Session session) {
        if (!session.hasStream(PLACEHOLDER_SENSOR_NAME)) {
            MeasurementStream placeholderStream = new MeasurementStream("", PLACEHOLDER_SENSOR_NAME, "", "", "", "",
                    0, 0, 0, 0, 0);

            session.add(placeholderStream);
        }
    }

    public Serializable getHiddenStreamsList() {
        return mHiddenStreams;
    }

    public void restoreHiddenStreams(ArrayList<String> hiddenSensors) {
        mHiddenStreams = hiddenSensors;
    }
}
