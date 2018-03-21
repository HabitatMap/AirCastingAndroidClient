package pl.llp.aircasting.helper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;
import pl.llp.aircasting.activity.ApplicationState;
import pl.llp.aircasting.model.CurrentSessionManager;
import pl.llp.aircasting.model.ViewingSessionsManager;
import pl.llp.aircasting.util.Constants;

/**
 * Created by radek on 08/11/17.
 */
@Singleton
public class SessionState {
    @Inject ApplicationState state;
    @Inject CurrentSessionManager currentSessionManager;
    @Inject ViewingSessionsManager viewingSessionsManager;

    public boolean isSessionCurrent(@NotNull long sessionId) {
        return sessionId == Constants.CURRENT_SESSION_FAKE_ID;
    }

    public boolean sessionHasNowValue(@NotNull long sessionId) {
        return isSessionCurrent(sessionId) || isSessionBeingViewed(sessionId);
    }

    public boolean isCurrentSessionIdle() {
        return currentSessionManager.isSessionIdle();
    }

    public boolean isSessionBeingViewed(@NotNull long sessionId) {
        return viewingSessionsManager.isSessionBeingViewed(sessionId);
    }

    private boolean isSessionRecording(@NotNull long sessionId) {
        return sessionId == Constants.CURRENT_SESSION_FAKE_ID && state.recording().isRecording();
    }

    public boolean sessionHasColoredBackground(long sessionId) {
        return isSessionRecording(sessionId) || isSessionBeingViewed(sessionId);
    }
}
