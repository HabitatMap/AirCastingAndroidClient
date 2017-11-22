package pl.llp.aircasting.model;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.activity.events.SessionLoadedEvent;
import pl.llp.aircasting.activity.events.SessionSensorsLoadedEvent;
import pl.llp.aircasting.helper.SessionDataFactory;
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
    public void onEvent(SessionLoadedEvent event) {
        Session session = event.getSession();
        Map<SensorName, Sensor> sessionSensors = newHashMap();

        for (MeasurementStream stream : session.getMeasurementStreams()) {
            if (stream.isMarkedForRemoval()) {
                continue;
            }

            Sensor sensor = new Sensor(stream);
            sessionSensors.put(SensorName.from(stream.getSensorName()), sensor);
        }

        viewingSessionsSensors.put(event.getSession().getId(), sessionSensors);
        eventBus.post(new SessionSensorsLoadedEvent(session));
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
}
