package pl.llp.aircasting.storage;

import android.widget.Toast;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.ApplicationState;
import pl.llp.aircasting.helper.NoOp;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.helper.ToastHelper;
import pl.llp.aircasting.model.CurrentSessionManager;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.ViewingSessionsManager;
import pl.llp.aircasting.storage.repository.SessionRepository;

import android.app.Activity;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ags on 28/03/2013 at 18:46
 */
@Singleton
public class UnfinishedSessionChecker {
    @Inject SessionRepository repo;
    @Inject ApplicationState state;
    @Inject ViewingSessionsManager viewingSessionsManager;
    @Inject CurrentSessionManager currentSessionManager;
    @Inject SettingsHelper settingsHelper;

    AtomicBoolean checkInProgress = new AtomicBoolean(false);

    public void finishIfNeeded(Activity context) {
        if (checkInProgress.compareAndSet(false, true)) {
            try {
                List<Session> unfinishedSessions = repo.unfinishedSessions();

                for (Session unfinishedSession : unfinishedSessions) {
                    if (state.saving().isSaving(unfinishedSession.getId())) {
                        continue;
                    }

                    if (!unfinishedSession.isFixed()) {
                        finishMobileSession(context, unfinishedSession);
                    } else {
                        finishFixedSession(unfinishedSession);
                    }
                }
            } finally {
                checkInProgress.set(false);
            }
        }
    }

    private void finishMobileSession(final Activity context, Session unfinishedSession) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastHelper.show(context, R.string.session_retrieved, Toast.LENGTH_SHORT);
            }
        });

        long sessionId = unfinishedSession.getId();

        if (unfinishedSession.isLocationless()) {
            currentSessionManager.finishSession(sessionId, false);
        } else if (!settingsHelper.isContributingToCrowdMap()) {
            Intents.contribute(context, sessionId);
        } else if (settingsHelper.isContributingToCrowdMap()) {
            currentSessionManager.finishSession(sessionId, true);
        }
        repo.complete(unfinishedSession.getId());
    }

    private void finishFixedSession(Session unfinishedSession) {
        viewingSessionsManager.viewAndStartSyncing(unfinishedSession.getId(), NoOp.progressListener());
        repo.completeFixedSession(unfinishedSession.getId());
    }
}