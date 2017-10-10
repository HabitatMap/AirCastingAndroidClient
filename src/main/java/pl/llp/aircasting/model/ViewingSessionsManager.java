package pl.llp.aircasting.model;

import com.google.inject.Inject;
import com.google.inject.internal.Preconditions;
import org.jetbrains.annotations.NotNull;
import pl.llp.aircasting.activity.ApplicationState;
import pl.llp.aircasting.storage.ProgressListener;
import pl.llp.aircasting.storage.repository.SessionRepository;

import java.util.Map;

import static com.google.inject.internal.Maps.newHashMap;

/**
 * Created by radek on 10/10/17.
 */
public class ViewingSessionsManager {
    @Inject ApplicationState state;
    @Inject SessionRepository sessionRepository;

    private Map<Long, Session> sessionsForViewing = newHashMap();

    public void loadSessionForViewing(long sessionId, @NotNull ProgressListener listener) {
        Preconditions.checkNotNull(listener);
        Session newSession = sessionRepository.loadFully(sessionId, listener);
        state.recording().startShowingOldSession();
        sessionsForViewing.put(sessionId, newSession);
    }
}
