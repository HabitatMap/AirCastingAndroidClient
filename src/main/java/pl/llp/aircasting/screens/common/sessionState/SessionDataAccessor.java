package pl.llp.aircasting.screens.common.sessionState;

import android.support.annotation.NonNull;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import pl.llp.aircasting.screens.common.helpers.NoOp;
import pl.llp.aircasting.util.Logger;
import pl.llp.aircasting.model.*;
import pl.llp.aircasting.storage.repository.SessionRepository;

import java.util.List;

/**
 * Created by radek on 20/10/17.
 */
@Singleton
public class SessionDataAccessor {
    @Inject
    CurrentSessionManager currentSessionManager;
    @Inject
    ViewingSessionsManager viewingSessionsManager;
    @Inject
    CurrentSessionSensorManager currentSessionSensorManager;
    @Inject
    ViewingSessionsSensorManager viewingSessionsSensorManager;
    @Inject SessionState sessionState;
    @Inject SessionRepository sessionRepository;
    @Inject VisibleSession visibleSession;

    public Session getSession(long sessionId) {
        Session session;

        if (sessionState.isSessionCurrent(sessionId)) {
            session = currentSessionManager.getCurrentSession();
        } else {
            session = viewingSessionsManager.getSession(sessionId);
        }

        return session;
    }

    @NonNull
    public Sensor getSensor(String sensorName, long sessionId) {
        Sensor sensor;

        if (sessionState.isSessionCurrent(sessionId)) {
            sensor = currentSessionSensorManager.getSensorByName(sensorName);
        } else {
            sensor = viewingSessionsSensorManager.getSensorByName(sensorName, sessionId);
        }

        return  sensor;
    }

    public List<Sensor> getSensorsList(long sessionId) {
        List<Sensor> result;

        if (sessionState.isSessionCurrent(sessionId)) {
            result = currentSessionSensorManager.getSensorsList();
        } else {
            result = viewingSessionsSensorManager.getSensorsList(sessionId);
        }

        return result;
    }

    public MeasurementStream getStream(String sensorName, long sessionId) {
        MeasurementStream stream;

        if (sessionState.isSessionCurrent(sessionId)) {
            stream = currentSessionManager.getMeasurementStream(sensorName);
        } else {
            stream = viewingSessionsManager.getMeasurementStream(sessionId, sensorName);
        }

        return stream;
    }

    public Integer getSessionSensorsCount(long sessionId) {
        int size;

        if (sessionState.isSessionCurrent(sessionId)) {
            size = currentSessionSensorManager.getSensorsList().size();
        } else {
            size = viewingSessionsSensorManager.getSensorsList(sessionId).size();
        }

        return size;
    }

    public double getNow(Sensor sensor, long sessionId) {
        double now = 0;

        if (sensor != null) {
            if (sessionState.isSessionBeingViewed(sessionId)) {
                now = getStream(sensor.getSensorName(), sessionId).getLatestMeasurementValue();
            }
            if (sessionState.isSessionCurrent(sessionId)) {
                now = currentSessionSensorManager.getNow(sensor);
            }
        }

        return now;
    }

    public void deleteSession(long sessionId) {
        if (sessionState.isSessionCurrent(sessionId)) {
            currentSessionManager.deleteSession();
        } else {
            clearViewingSession(sessionId);
            sessionRepository.markSessionForRemoval(sessionId);
        }
    }

    public void deleteSensorStream(Sensor sensor, long sessionId) {
        String sensorName = sensor.getSensorName();

        if (sessionState.isSessionCurrent(sessionId)) {
            currentSessionSensorManager.deleteSensorFromCurrentSession(sensor);
        } else {
            viewingSessionsSensorManager.deleteSensorFromSession(sensor, sessionId);
        }
        
        deleteSensorStream(sensorName, sessionId);
    }

    void deleteSensorStream(String sensorName, long sessionId) {
        MeasurementStream stream = getStream(sensorName, sessionId);

        if (stream == null) {
            Logger.w("No stream for sensor [" + sensorName + "]");
            return;
        }

        sessionRepository.deleteStream(getSession(sessionId), stream);
        stream.setMarkedForRemoval(true);
        getSession(sessionId).removeStream(stream);
    }

    public void clearViewingSession(long sessionId) {
        viewingSessionsManager.removeSession(sessionId);
        viewingSessionsSensorManager.removeSessionSensors(sessionId);
    }

    public void setVisibleSession(long sessionId) {
        if (sessionState.isSessionCurrent(sessionId)) {
            visibleSession.setSession(currentSessionManager.getCurrentSession());
        } else {
            List sessionIds = viewingSessionsManager.getSessionIdsList();

            if (!sessionIds.contains(sessionId)) {
                viewingSessionsManager.view(sessionId, NoOp.progressListener());
            }

            visibleSession.setSession(getSession(sessionId));
        }
    }

    public void setVisibleSensor(Sensor sensor) {
        visibleSession.setSensor(sensor);
    }

    public void setVisibleSensorFromName(long sessionId, String sensorName) {
        MeasurementStream stream = getStream(sensorName, sessionId);
        Sensor sensor = new Sensor(stream, sessionId);

        visibleSession.setSensor(sensor);
    }
}
