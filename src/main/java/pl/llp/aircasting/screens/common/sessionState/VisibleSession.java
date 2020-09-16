package pl.llp.aircasting.screens.common.sessionState;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import pl.llp.aircasting.event.ui.SensorChangedEvent;
import pl.llp.aircasting.screens.common.ApplicationState;
import pl.llp.aircasting.event.session.CurrentSessionSetEvent;
import pl.llp.aircasting.event.session.VisibleSessionUpdatedEvent;
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
    @Inject ApplicationState state;
    @Inject CurrentSessionManager currentSessionManager;

    final Sensor AUDIO_SENSOR = SimpleAudioReader.getSensor();
    private Session session;
    private Sensor sensor;

    @Inject
    public VisibleSession(EventBus eventBus, ApplicationState state, CurrentSessionManager currentSessionManager) {
        this.eventBus = eventBus;
        this.state = state;
        this.currentSessionManager = currentSessionManager;
    }

    @Inject
    public void init() {
        eventBus.register(this);
    }

    @Subscribe
    public void onEvent(CurrentSessionSetEvent event) {
        this.session = event.getSession();
    }

    public void setSession(@NotNull Session session) {
        this.session = session;
        eventBus.post(new VisibleSessionUpdatedEvent(session));
    }

    public void setSensor(@NotNull Sensor sensor) {
        this.sensor = sensor;
        eventBus.post(new VisibleStreamUpdatedEvent(sensor));
    }

    public void changeSensor(@NotNull Sensor sensor) {
        this.sensor = sensor;
        eventBus.post(new SensorChangedEvent(sensor));
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
        return getSession().getStream(sensor.getSensorName());
    }

    public boolean isSessionLocationless() {
        return getSession().isLocationless();
    }

    public boolean isCurrentSessionVisible() {
        return getSession() == currentSessionManager.getCurrentSession();
    }

    public boolean isVisibleSessionRecording() {
        return isCurrentSessionVisible() && state.recording().isRecording();
    }

    public boolean isVisibleSessionViewed() {
        return getSession() != currentSessionManager.getCurrentSession();
    }

    public boolean isVisibleSessionFixed() {
        if (getSession() != null) {
            return getSession().isFixed();
        }
        return false;
    }

    public double getAvg(Sensor sensor) {
        String sensorName = sensor.getSensorName();

        if (getSession().hasStream(sensorName)) {
            return getSession().getStream(sensorName).getAvg();
        } else {
            return 0;
        }
    }

    public double getPeak(Sensor sensor) {
        String sensorName = sensor.getSensorName();

        if (getSession().hasStream(sensorName)) {
            return getSession().getStream(sensorName).getPeak();
        } else {
            return 0;
        }
    }

    public Iterable<Note> getSessionNotes() {
        return getSession().getNotes();
    }

    public void deleteNote(Note note) {
        getSession().getNotes().remove(note);
    }

    public Note getSessionNote(int i) {
        return getSession().getNotes().get(i);
    }

    public int getSessionNoteCount() {
        return getSession().getNotes().size();
    }

    public List<Measurement> getMeasurements(Sensor sensor) {
        String name = sensor.getSensorName();

        if (getSession().hasStream(name)) {
            MeasurementStream stream = getSession().getStream(name);
            return stream.getMeasurements();
        } else {
            return newArrayList();
        }
    }

    public long getVisibleSessionId() {
        if (getSession() != null && !isCurrentSessionVisible()) {
            return getSession().getId();
        }

        return Constants.CURRENT_SESSION_FAKE_ID;
    }
}
