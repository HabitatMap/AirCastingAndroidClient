package pl.llp.aircasting.model;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.internal.Preconditions;
import org.jetbrains.annotations.NotNull;
import pl.llp.aircasting.activity.ApplicationState;
import pl.llp.aircasting.event.session.ViewSessionEvent;
import pl.llp.aircasting.storage.ProgressListener;
import pl.llp.aircasting.storage.repository.SessionRepository;

import java.util.Map;

import static com.google.inject.internal.Maps.newHashMap;

/**
 * Created by radek on 10/10/17.
 */
@Singleton
public class ViewingSessionsManager {
    @Inject ApplicationState state;
    @Inject SessionRepository sessionRepository;
    @Inject EventBus eventBus;

    private static Map<Long, Session> sessionsForViewing = newHashMap();

    @Inject
    public void init() {
        eventBus.register(this);
    }

    public void loadSessionForViewing(long sessionId, @NotNull ProgressListener listener) {
        Preconditions.checkNotNull(listener);
        Session newSession = sessionRepository.loadFully(sessionId, listener);
        sessionsForViewing.put(sessionId, newSession);
        notifyNewSession(newSession);
    }

    public Session getSession(long sessionId) {
        return sessionsForViewing.get(sessionId);
    }

    public MeasurementStream getMeasurementStream(String sensorName, long sessionId) {
        return sessionsForViewing.get(sessionId).getStream(sensorName);
    }

    private void notifyNewSession(Session session) {
        eventBus.post(new ViewSessionEvent(session));
    }
}
