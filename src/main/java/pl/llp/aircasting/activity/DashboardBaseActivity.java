package pl.llp.aircasting.activity;

import android.os.AsyncTask;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.helper.LocationHelper;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.helper.ToggleAircastingHelper;
import pl.llp.aircasting.helper.ToggleAircastingHelperFactory;
import pl.llp.aircasting.model.CurrentSessionManager;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.receiver.SyncBroadcastReceiver;

import android.content.Context;
import android.content.Intent;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import pl.llp.aircasting.storage.UnfinishedSessionChecker;

import java.util.concurrent.TimeUnit;

/**
 * A common superclass for activities that want to display left/right
 * navigation arrows
 */
public abstract class DashboardBaseActivity extends RoboActivityWithProgress {
    @Inject Context context;
    @Inject EventBus eventBus;
    @Inject
    CurrentSessionManager currentSessionManager;
    @Inject LocationHelper locationHelper;
    @Inject SettingsHelper settingsHelper;
    @Inject UnfinishedSessionChecker checker;
    @Inject ApplicationState state;
    @Inject ToggleAircastingHelperFactory aircastingHelperFactory;
    @Inject
    SyncBroadcastReceiver syncBroadcastReceiver;
    SyncBroadcastReceiver registeredReceiver;

    private ToggleAircastingHelper toggleAircastingHelper;
    private boolean initialized = false;
    private long lastChecked = 0;
    public static final long DELTA = TimeUnit.SECONDS.toMillis(15);

    @Override
    protected void onResume() {
        super.onResume();

        initialize();
        locationHelper.start();

        if (currentSessionManager.isSessionPresent()) {
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

        if (currentSessionManager.isSessionPresent()) {
            Intents.stopSensors(context);
        }

        locationHelper.stop();

        if (registeredReceiver != null) {
            unregisterReceiver(syncBroadcastReceiver);
            registeredReceiver = null;
        }
        eventBus.unregister(this);
    }

    private void initialize() {
        toggleAircastingHelper = aircastingHelperFactory.getAircastingHelper(this, getDelegate());

        if (!initialized) {
            initialized = true;
        }
    }

    public synchronized void toggleAirCasting() {
        toggleAircastingHelper.toggleAirCasting();
        getDelegate().invalidateOptionsMenu();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Intents.CHOOSE_SESSION_TYPE:
                if (resultCode == R.id.mobile_session_button) {
                    toggleAircastingHelper.startMobileAirCasting();
                } else {
                    toggleAircastingHelper.startFixedAirCasting();
                }
                break;
            case Intents.SAVE_DIALOG:
                startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                break;
            case Intents.EDIT_SESSION:
                if (resultCode == R.id.save_button) {
                    Session session = Intents.editSessionResult(data);
                    currentSessionManager.updateSession(session);
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
                    checker.check(DashboardBaseActivity.this);
                    lastChecked = System.currentTimeMillis();
                    return null;
                }
            }.execute();
        }
    }

    private boolean shouldCheckForUnfinishedSessions()
    {
        if(currentSessionManager.isSessionRecording())
            return false;

        if(state.saving().isSaving())
            return false;

        long timeout = System.currentTimeMillis() - lastChecked;
        return timeout > DELTA;
    }
}
