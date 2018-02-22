package pl.llp.aircasting.model;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.activity.events.SessionLoadedForViewingEvent;
import pl.llp.aircasting.activity.events.SessionSensorsLoadedEvent;
import pl.llp.aircasting.model.events.FixedSessionsMeasurementEvent;
import pl.llp.aircasting.model.internal.SensorName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newConcurrentMap;
import static com.google.common.collect.Maps.newHashMap;

/**
 * Created by radek on 10/10/17.
 */
@Singleton
public class ViewingSessionsSensorManager {
    @Inject EventBus eventBus;

    private volatile Map<Long, Map<SensorName, Sensor>> viewingSessionsSensors = newConcurrentMap();
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
        boolean newFixedSession = event.isNewFixedSession();
        Map<SensorName, Sensor> sessionSensors = viewingSessionsSensors.get(session.getId());

        if (sessionSensors == null) {
            sessionSensors = newHashMap();
        }

        if (newFixedSession) {
            addPlaceholderStream(session);
        } else {
            deletePlaceholderSensor(session.getId());
        }

        for (MeasurementStream stream : session.getMeasurementStreams()) {
            if (stream.isMarkedForRemoval()) {
                continue;
            }

            Sensor sensor = new Sensor(stream);
            sessionSensors.put(SensorName.from(stream.getSensorName()), sensor);
        }

        viewingSessionsSensors.put(session.getId(), sessionSensors);

        eventBus.post(new SessionSensorsLoadedEvent(session.getId()));
    }

    @Subscribe
    public void onEvent(FixedSessionsMeasurementEvent event) {
        deletePlaceholderSensor(event.getSessionId());
    }

    private void deletePlaceholderSensor(long sessionId) {
        if (viewingSessionsSensors.containsKey(sessionId)) {
            viewingSessionsSensors.get(sessionId).remove(SensorName.from(PLACEHOLDER_SENSOR_NAME));
        }
    }

    public List<Sensor> getSensorsList(long sessionId) {
        ArrayList<Sensor> result = newArrayList();
        result.addAll(viewingSessionsSensors.get(sessionId).values());
        return result;
    }

    public void deleteSensorFromSession(Sensor sensor, long sessionId) {
        String sensorName = sensor.getSensorName();
        viewingSessionsSensors.get(sessionId).remove(SensorName.from(sensorName));
    }

    public void removeSessionSensors(long sessionId) {
        viewingSessionsSensors.remove(sessionId);
    }

    public void removeAllSessionsSensors() {
        viewingSessionsSensors.clear();
    }

    private void addPlaceholderStream(Session session) {
        if (!session.hasStream(PLACEHOLDER_SENSOR_NAME)) {
            MeasurementStream placeholderStream = new MeasurementStream("", PLACEHOLDER_SENSOR_NAME, "", "", "", "",
                    0, 0, 0, 0, 0);

            session.add(placeholderStream);
        }
    }
}
