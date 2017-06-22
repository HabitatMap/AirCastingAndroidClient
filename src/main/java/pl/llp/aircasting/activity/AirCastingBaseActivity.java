package pl.llp.aircasting.activity;

import android.os.AsyncTask;
import android.view.*;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.helper.LocationHelper;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.helper.ToggleAircastingHelper;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.SessionManager;
import pl.llp.aircasting.receiver.SyncBroadcastReceiver;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.widget.Button;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.internal.Nullable;
import pl.llp.aircasting.storage.UnfinishedSessionChecker;
import roboguice.inject.InjectView;

import java.util.concurrent.TimeUnit;

/**
 * A common superclass for activities that want to display left/right
 * navigation arrows
 */

public abstract class AirCastingBaseActivity extends RoboMapActivityWithProgress implements View.OnClickListener {
    public static final long DELTA = TimeUnit.SECONDS.toMillis(15);

    @Inject Context context;
    @Inject UnfinishedSessionChecker checker;
    @Inject ApplicationState state;
    @Inject LocationManager locationManager;
    @Inject SessionManager sessionManager;
    @Inject LocationHelper locationHelper;
    @Inject SettingsHelper settingsHelper;
    @Inject EventBus eventBus;

    @Inject
    SyncBroadcastReceiver syncBroadcastReceiver;
    SyncBroadcastReceiver registeredReceiver;

    @Nullable
    @InjectView(R.id.zoom_in) Button zoomIn;

    @Nullable
    @InjectView(R.id.zoom_out) Button zoomOut;


    private ToggleAircastingHelper toggleAircastingHelper;
    private boolean initialized = false;
    private long lastChecked = 0;

    @Override
    protected void onResume() {
        super.onResume();

        initialize();
        locationHelper.start();

        if (!sessionManager.isSessionSaved()) {
            Intents.startSensors(context);
        }

        registerReceiver(syncBroadcastReceiver, SyncBroadcastReceiver.INTENT_FILTER);
        registeredReceiver = syncBroadcastReceiver;

        eventBus.register(this);
        checkForUnfinishedSessions();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!sessionManager.isSessionSaved()) {
            Intents.stopSensors(context);
        }

        locationHelper.stop();

        if (registeredReceiver != null) {
            unregisterReceiver(syncBroadcastReceiver);
            registeredReceiver = null;
        }
        eventBus.unregister(this);
    }

    @Override
    protected boolean isRouteDisplayed() {
        // The maps server needs to know if we are displaying any routes
        return false;
    }

    private void initialize() {
        toggleAircastingHelper = new ToggleAircastingHelper(this,
                sessionManager, settingsHelper, locationManager, locationHelper, getDelegate(), context);

        if (!initialized) {
            initialized = true;
        }
    }

    @Override
    public void onProfileClick(View view) {
        super.onProfileClick(view);
    }

    public synchronized void toggleAirCasting() {
        toggleAircastingHelper.toggleAirCasting();
        getDelegate().invalidateOptionsMenu();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Intents.SAVE_DIALOG:
                startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                break;
            case Intents.EDIT_SESSION:
                if (resultCode == R.id.save_button) {
                    Session session = Intents.editSessionResult(data);
                    sessionManager.updateSession(session);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void checkForUnfinishedSessions()
    {
        if (shouldCheckForUnfinishedSessions())
        {
            new AsyncTask<Void, Void, Void>(){
                @Override
                protected Void doInBackground(Void... voids)
                {
                    checker.check(AirCastingBaseActivity.this);
                    lastChecked = System.currentTimeMillis();
                    return null;
                }
            }.execute();
        }
    }

    private boolean shouldCheckForUnfinishedSessions()
    {
        if(sessionManager.isRecording())
            return false;

        if(state.saving().isSaving())
            return false;

        long timeout = System.currentTimeMillis() - lastChecked;
        return timeout > DELTA;
    }
}
