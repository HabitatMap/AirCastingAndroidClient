package pl.llp.aircasting.sync;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import pl.llp.aircasting.R;
import pl.llp.aircasting.android.Logger;
import pl.llp.aircasting.api.FixedSessionDriver;
import pl.llp.aircasting.helper.NoOp;
import pl.llp.aircasting.helper.ToastHelper;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.ViewingSessionsManager;
import pl.llp.aircasting.model.events.SensorEvent;
import roboguice.service.RoboIntentService;

import java.util.Collection;

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
        Collection<Session> sessions = viewingSessionsManager.getFixedSessions();

        synchronized (sessions) {
            for (Session session : sessions) {
                driver.downloadNewData(session, NoOp.progressListener());
            }
        }
   }

    private boolean canDownload() {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null
                && networkInfo.isConnected();
    }
}
