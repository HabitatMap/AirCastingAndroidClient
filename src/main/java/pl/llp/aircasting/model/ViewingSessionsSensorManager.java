package pl.llp.aircasting.model;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.activity.events.SessionAddedEvent;
import pl.llp.aircasting.model.internal.SensorName;
import pl.llp.aircasting.sensor.VisibleSensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newConcurrentMap;
import static com.google.inject.internal.Lists.newArrayList;

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

    public List<Sensor> getSensorsForSession(Long sessionId) {
        ArrayList<Sensor> result = newArrayList();
        result.addAll(viewingSessionsSensors.get(sessionId).values());
        return result;
    }

    @Subscribe
    public void onEvent(SessionAddedEvent event) {
        Session session = event.getSession();
        Map<SensorName, Sensor> sessionSensors = viewingSessionsSensors.get(session.getId());

        viewingSessionsSensors = newConcurrentMap();

        for (MeasurementStream stream : session.getMeasurementStreams()) {
            if (stream.isMarkedForRemoval()) {
                continue;
            }

            Sensor sensor = new Sensor(stream);
            String name = sensor.getSensorName();
            sessionSensors.put(SensorName.from(name), sensor);

            visibleSensor = sensor;
        }
    }
}
