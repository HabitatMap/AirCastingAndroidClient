package pl.llp.aircasting.model;

import android.content.Context;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.internal.Preconditions;
import org.jetbrains.annotations.NotNull;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.activity.ApplicationState;
import pl.llp.aircasting.activity.events.SessionLoadedForViewingEvent;
import pl.llp.aircasting.storage.ProgressListener;
import pl.llp.aircasting.storage.repository.SessionRepository;
import pl.llp.aircasting.tracking.ContinuousTracker;

import java.util.Map;

import static com.google.inject.internal.Maps.newHashMap;
import static pl.llp.aircasting.model.CurrentSessionManager.TOTALLY_FAKE_COORDINATE;

/**
 * Created by radek on 10/10/17.
 */
@Singleton
public class ViewingSessionsManager {
    @Inject SessionRepository sessionRepository;
    @Inject EventBus eventBus;
    @Inject ApplicationState state;
    @Inject ContinuousTracker tracker;
    @Inject Context context;

    private static Map<Long, Session> sessionsForViewing = newHashMap();
    private static Map<Long, Session> fixedSessions = newHashMap();
    private static Session newFixedSession;

    @Inject
    public void init() {
        eventBus.register(this);
    }

    public void viewAndStartSyncing(Long sessionId, ProgressListener progressListener) {
        Preconditions.checkNotNull(progressListener);
        Session session = sessionRepository.loadShallow(sessionId);
        sessionsForViewing.put(sessionId, session);
        addFixedSession(session);
        notifyNewSession(session);
    }

    public void view(Long sessionId, @NotNull ProgressListener listener) {
        Preconditions.checkNotNull(listener);
        Session session = sessionRepository.loadFully(sessionId, listener);
        if (session.isFixed()) {
            addFixedSession(session);
        }
        sessionsForViewing.put(sessionId, session);
        notifyNewSession(session);
    }

    public void addFixedSession(Session session) {
        fixedSessions.put(session.getId(), session);
        Intents.triggerSync(context);
    }

    public void createAndSetFixedSession() {
        newFixedSession = new Session(true);
    }

    public void startFixedSession(String title, String tags, String description, boolean isIndoor, LatLng latlng) {
        if (newFixedSession == null) {
            createAndSetFixedSession();
        }
        newFixedSession.setTitle(title);
        newFixedSession.setTags(tags);
        newFixedSession.setDescription(description);
        newFixedSession.setIndoor(isIndoor);

        if (latlng == null) {
            newFixedSession.setLatitude(TOTALLY_FAKE_COORDINATE);
            newFixedSession.setLongitude(TOTALLY_FAKE_COORDINATE);
        } else {
            newFixedSession.setLatitude(latlng.latitude);
            newFixedSession.setLongitude(latlng.longitude);
        }

        if (!tracker.startTracking(newFixedSession, true)) {
            removeSession(newFixedSession.getId());
            newFixedSession = null;
        } else {
            Toast.makeText(context, "The streaming session will be shown when data is retrievable", Toast.LENGTH_SHORT);
        }
    }

    public void removeSession(long sessionId) {
        sessionsForViewing.remove(sessionId);
    }

    public Session getSession(long sessionId) {
        return sessionsForViewing.get(sessionId);
    }

    public Session getStreamingSession() {
        return newFixedSession;
    }

    public boolean isSessionBeingViewed(long sessionId) {
        return sessionsForViewing.containsKey(sessionId);
    }

    public boolean isAnySessionFixed() {
        return !fixedSessions.isEmpty();
    }

    public MeasurementStream getMeasurementStream(long sessionId, String sensorName) {
        return sessionsForViewing.get(sessionId).getStream(sensorName);
    }

    public boolean anySessionPresent() {
        return !sessionsForViewing.isEmpty();
    }

    private void notifyNewSession(Session session) {
        state.dashboardState().populate();
        eventBus.post(new SessionLoadedForViewingEvent(session));
    }
}
