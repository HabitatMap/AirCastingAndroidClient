package pl.llp.aircasting.activity;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.helper.LocationHelper;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.helper.ToggleAircastingManager;
import pl.llp.aircasting.helper.ToggleAircastingManagerFactory;
import pl.llp.aircasting.model.CurrentSessionManager;
import pl.llp.aircasting.model.CurrentSessionSensorManager;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.receiver.SyncBroadcastReceiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import pl.llp.aircasting.storage.UnfinishedSessionChecker;

import static pl.llp.aircasting.util.Constants.PERMISSIONS;
import static pl.llp.aircasting.util.Constants.PERMISSIONS_ALL;

/**
 * A common superclass for activities that want to display left/right
 * navigation arrows
 */
public abstract class DashboardBaseActivity extends RoboActivityWithProgress {
    @Inject Context context;
    @Inject EventBus eventBus;
    @Inject CurrentSessionManager currentSessionManager;
    @Inject LocationHelper locationHelper;
    @Inject SettingsHelper settingsHelper;
    @Inject UnfinishedSessionChecker checker;
    @Inject ApplicationState state;
    @Inject ToggleAircastingManagerFactory aircastingHelperFactory;
    @Inject SyncBroadcastReceiver syncBroadcastReceiver;
    SyncBroadcastReceiver registeredReceiver;

    private ToggleAircastingManager toggleAircastingManager;
    private boolean initialized = false;

    @Override
    protected void onStart() {
        super.onStart();

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_ALL);
        } else {
            locationHelper.start();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        initialize();

        registerReceiver(syncBroadcastReceiver, SyncBroadcastReceiver.INTENT_FILTER);
        registeredReceiver = syncBroadcastReceiver;

        eventBus.register(this);
        checkForUnfinishedSessions();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!currentSessionManager.isSessionRecording()) {
            locationHelper.stop();
        }

        if (registeredReceiver != null) {
            unregisterReceiver(syncBroadcastReceiver);
            registeredReceiver = null;
        }
        eventBus.unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!currentSessionManager.isSessionRecording()) {
            Intents.stopSensors(this);
        }
    }

    private void initialize() {
        toggleAircastingManager = aircastingHelperFactory.getAircastingHelper(this, getDelegate());

        if (!initialized) {
            initialized = true;
        }
    }

    public synchronized void toggleAirCasting() {
        toggleAircastingManager.toggleAirCasting();
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
                    currentSessionManager.updateSession(session);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void checkForUnfinishedSessions() {
        if (shouldCheckForUnfinishedSessions()) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    checker.finishIfNeeded(DashboardBaseActivity.this);
                    return null;
                }
            }.execute();
        }
    }

    private boolean shouldCheckForUnfinishedSessions() {
        if (currentSessionManager.isSessionRecording()) {
            return false;
        }

        if (state.saving().isSaving()) {
            return false;
        }

        return true;
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
}
