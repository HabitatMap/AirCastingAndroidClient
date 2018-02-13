package pl.llp.aircasting.helper;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import pl.llp.aircasting.activity.ApplicationState;
import pl.llp.aircasting.activity.events.VisibleSessionUpdatedEvent;
import pl.llp.aircasting.event.ui.VisibleStreamUpdatedEvent;
import pl.llp.aircasting.model.*;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;
import pl.llp.aircasting.util.Constants;

import java.util.List;

import static com.google.inject.internal.Lists.newArrayList;

/**
 * Created by radek on 18/10/17.
 */
@Singleton
public class VisibleSession {
    @Inject EventBus eventBus;
    @Inject SessionDataFactory sessionDataFactory;
    @Inject ApplicationState state;
    @Inject CurrentSessionManager currentSessionManager;

    final Sensor AUDIO_SENSOR = SimpleAudioReader.getSensor();
    private Session session;
    private Sensor sensor;

    public void setSession(@NotNull Long sessionId) {
        this.session = sessionDataFactory.getSession(sessionId);
        eventBus.post(new VisibleSessionUpdatedEvent(session));
    }

    public void setSensor(@NotNull String sensorName) {
        this.sensor = sessionDataFactory.getSensor(sensorName, getVisibleSessionId());
        eventBus.post(new VisibleStreamUpdatedEvent(sensor));
    }

    @NotNull
    public Session getSession() {
        return session;
    }

    @NotNull
    public Sensor getSensor() {
        return sensor != null ? sensor : AUDIO_SENSOR;
    }

    public MeasurementStream getStream() {
        return session.getStream(sensor.getSensorName());
    }

    public boolean isSessionLocationless() {
        return session.isLocationless();
    }

    public boolean isCurrentSessionVisible() {
        return session == currentSessionManager.getCurrentSession();
    }

    public boolean isVisibleSessionRecording() {
        return isCurrentSessionVisible() && state.recording().isRecording();
    }

    public boolean isVisibleSessionViewed() {
        return session != currentSessionManager.getCurrentSession();
    }

    public boolean isVisibleSessionFixed() {
        return session.isFixed();
    }

    public double getAvg(Sensor sensor) {
        String sensorName = sensor.getSensorName();

        if (session.hasStream(sensorName)) {
            return session.getStream(sensorName).getAvg();
        } else {
            return 0;
        }
    }

    public double getPeak(Sensor sensor) {
        String sensorName = sensor.getSensorName();

        if (session.hasStream(sensorName)) {
            return session.getStream(sensorName).getPeak();
        } else {
            return 0;
        }
    }

    public Iterable<Note> getSessionNotes() {
        return session.getNotes();
    }

    public Note getSessionNote(int i) {
        return session.getNotes().get(i);
    }

    public int getSessionNoteCount() {
        return session.getNotes().size();
    }

    public List<Measurement> getMeasurements(Sensor sensor) {
        String name = sensor.getSensorName();

        if (session.hasStream(name)) {
            MeasurementStream stream = session.getStream(name);
            return stream.getMeasurements();
        } else {
            return newArrayList();
        }
    }

    public long getVisibleSessionId() {
        long sessionId;

        if (isCurrentSessionVisible()) {
            sessionId = Constants.CURRENT_SESSION_FAKE_ID;
        } else {
            sessionId = session.getId();
        }

        return sessionId;
    }
}
