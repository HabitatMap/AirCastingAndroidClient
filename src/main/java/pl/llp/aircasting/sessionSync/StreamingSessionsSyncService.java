package pl.llp.aircasting.sessionSync;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;
import com.google.inject.Inject;
import pl.llp.aircasting.R;
import pl.llp.aircasting.networking.drivers.FixedSessionDriver;
import pl.llp.aircasting.screens.common.helpers.NoOp;
import pl.llp.aircasting.screens.common.ToastHelper;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.screens.common.sessionState.ViewingSessionsManager;
import roboguice.service.RoboIntentService;

import java.util.ArrayList;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by radek on 23/01/18.
 */
public class StreamingSessionsSyncService extends RoboIntentService {
    @Inject ConnectivityManager connectivityManager;
    @Inject ViewingSessionsManager viewingSessionsManager;
    @Inject FixedSessionDriver driver;

    public StreamingSessionsSyncService() {
        super(StreamingSessionsSyncService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            if (canDownload()) {
                 sync();
            }
        } catch (SessionSyncException exception) {
            ToastHelper.show(getBaseContext(), R.string.measurement_sync_failed, Toast.LENGTH_LONG);
        }
    }

    private void sync() {
        final ArrayList<Session> sessions = newArrayList(viewingSessionsManager.getFixedSessions());
        int size = sessions.size();

        synchronized (sessions) {
            for (int i = 0; i < size; i++) {
                driver.downloadNewData(sessions.get(i), NoOp.progressListener());
            }
        }
    }

    private boolean canDownload() {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null
                && networkInfo.isConnected();
    }
}
