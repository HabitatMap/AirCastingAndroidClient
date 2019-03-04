package pl.llp.aircasting.screens.common.sessionState;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.internal.Preconditions;
import org.jetbrains.annotations.NotNull;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.screens.common.helpers.NoOp;
import pl.llp.aircasting.event.session.SessionLoadedForViewingEvent;
import pl.llp.aircasting.networking.drivers.FixedSessionDriver;
import pl.llp.aircasting.storage.ProgressListener;
import pl.llp.aircasting.storage.repository.SessionRepository;
import pl.llp.aircasting.sync.SyncService;
import pl.llp.aircasting.tracking.ContinuousTracker;

import java.util.*;

import static com.google.inject.internal.Maps.newHashMap;
import static pl.llp.aircasting.screens.common.sessionState.CurrentSessionManager.TOTALLY_FAKE_COORDINATE;
import static pl.llp.aircasting.screens.common.sessionState.ViewingSessionsSensorManager.PLACEHOLDER_SENSOR_NAME;

/**
 * Created by radek on 10/10/17.
 */
@Singleton
public class ViewingSessionsManager {
    @Inject SessionRepository sessionRepository;
    @Inject EventBus eventBus;
    @Inject ContinuousTracker tracker;
    @Inject FixedSessionDriver fixedSessionDriver;
    @Inject Context context;
    @Inject SyncService syncService;

    private static Map<Long, Session> sessionsForViewing = newHashMap();
    private static Map<Long, Session> fixedSessions = newHashMap();
    private static Session newFixedSession;

    @Inject
    public void init() {
        eventBus.register(this);
    }

    public void viewAndStartSyncing(Long sessionId, ProgressListener progressListener) {
        if (sessionsForViewing.containsKey(sessionId)) {
            return;
        }
        Preconditions.checkNotNull(progressListener);
        Session session = sessionRepository.loadShallow(sessionId);
        sessionsForViewing.put(sessionId, session);
        addFixedSession(session);
        notifyNewSession(session, true);
    }

    public void view(Long sessionId, @NotNull final ProgressListener progressListener) {
        Preconditions.checkNotNull(progressListener);

        Session session = sessionRepository.loadFully(sessionId, progressListener);
        String sessionUUID = String.valueOf(session.getUUID());

        if (session.isIncomplete()) {
            syncService.downloadSessionMeasurements(sessionId, sessionUUID);
            session = sessionRepository.loadFully(sessionId, progressListener);
        }

        if (session.hasStream(PLACEHOLDER_SENSOR_NAME)) {
            MeasurementStream stream = getMeasurementStream(sessionId, PLACEHOLDER_SENSOR_NAME);
            session.removeStream(stream);
        }

        if (session.isFixed()) {
//            new AsyncTask<Void, Void, Void>() {
//                @Override
//                protected Void doInBackground(Void... voids) {
//                    fixedSessionDriver.downloadNewData(session, progressListener);
//                    return null;
//                }
//            };

            syncService.downloadSessionMeasurements(sessionId, sessionUUID);
            addFixedSession(session);
        }

        sessionsForViewing.put(sessionId, session);
        notifyNewSession(session, false);
    }


    public void addFixedSession(Session session) {
        fixedSessions.put(session.getId(), session);
        Intents.triggerSync(context);
    }

    public void restoreSessions(long[] savedIds) {
        if (savedIds == null || savedIds.length == 0) {
            return;
        }

        for (int i = 0; i < savedIds.length; i++) {
            view(savedIds[i], NoOp.progressListener());
        }

        for (Map.Entry<Long, Session> entry : sessionsForViewing.entrySet()) {
            notifyNewSession(entry.getValue(), false);
        }
    }

    public void createAndSetFixedSession() {
        newFixedSession = new Session(true);
    }

    public void startFixedSession(String title, String tags, boolean isIndoor, LatLng latlng) {
        if (newFixedSession == null) {
            createAndSetFixedSession();
        }
        newFixedSession.setTitle(title);
        newFixedSession.setTags(tags);
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
        }
    }

    public void removeSession(long sessionId) {
        sessionsForViewing.remove(sessionId);
        fixedSessions.remove(sessionId);
    }

    public Session getSession(long sessionId) {
        return sessionsForViewing.get(sessionId);
    }

    public Collection<Session> getFixedSessions() {
        return fixedSessions.values();
    }

    public long[] getSessionIdsArray() {
        long[] l = new long[sessionsForViewing.size()];
        Set<Long> set = sessionsForViewing.keySet();
        List<Long> list = new ArrayList<Long>(set);

        for (int i = 0; i < sessionsForViewing.size(); i++) {
            l[i] = list.get(i);
        }

        return l;
    }

    public List getSessionIdsList() {
        Set<Long> set = sessionsForViewing.keySet();
        List<Long> list = new ArrayList<Long>(set);

        return list;
    }

    public void setStreamingSession(Session session) {
        newFixedSession = session;
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

    public int getSessionsCount() {
        return sessionsForViewing.size();
    }

    public MeasurementStream getMeasurementStream(long sessionId, String sensorName) {
        return sessionsForViewing.get(sessionId).getStream(sensorName);
    }

    public boolean anySessionPresent() {
        return !sessionsForViewing.isEmpty();
    }

    public boolean sessionsEmpty() {
        return sessionsForViewing.isEmpty();
    }

    private void notifyNewSession(Session session, boolean newFixedSession) {
        eventBus.post(new SessionLoadedForViewingEvent(session, newFixedSession));
    }

    public void removeAllSessions() {
        sessionsForViewing.clear();
        fixedSessions.clear();
    }
}
