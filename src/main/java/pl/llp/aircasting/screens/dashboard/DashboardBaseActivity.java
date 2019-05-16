package pl.llp.aircasting.screens.dashboard;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.screens.common.ToastHelper;
import pl.llp.aircasting.screens.common.helpers.LocationHelper;
import pl.llp.aircasting.screens.common.helpers.SettingsHelper;
import pl.llp.aircasting.screens.common.ToggleAircastingManager;
import pl.llp.aircasting.screens.common.ToggleAircastingManagerFactory;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionManager;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.screens.common.sessionState.ViewingSessionsManager;
import pl.llp.aircasting.screens.common.ApplicationState;
import pl.llp.aircasting.screens.common.base.RoboActivityWithProgress;
import pl.llp.aircasting.sessionSync.SyncBroadcastReceiver;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import pl.llp.aircasting.storage.UnfinishedSessionChecker;

import static pl.llp.aircasting.screens.common.helpers.LocationHelper.REQUEST_CHECK_SETTINGS;
import static pl.llp.aircasting.util.Constants.PERMISSIONS;
import static pl.llp.aircasting.util.Constants.PERMISSIONS_ALL;

/**
 * A common superclass for activities that want to display left/right
 * navigation arrows
 */
public abstract class DashboardBaseActivity extends RoboActivityWithProgress implements LocationHelper.LocationSettingsListener {
    @Inject Context context;
    @Inject EventBus eventBus;
    @Inject CurrentSessionManager currentSessionManager;
    @Inject ViewingSessionsManager viewingSessionsManager;
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
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        initialize();

        registerReceiver(syncBroadcastReceiver, SyncBroadcastReceiver.INTENT_FILTER);
        registeredReceiver = syncBroadcastReceiver;

        checkForUnfinishedSessions();
    }

    private void initialize() {
        toggleAircastingManager = aircastingHelperFactory.getAircastingHelper(this, getDelegate());

        if (!initialized) {
            initialized = true;
        }
    }

    public synchronized void toggleAirCasting() {
        if (!currentSessionManager.isSessionRecording()) {
            locationHelper.checkLocationSettings(this);
        } else {
            toggleSessionRecording();
        }
    }

    @Override
    public void onLocationSettingsSatisfied() {
        toggleSessionRecording();
    }

    private void toggleSessionRecording() {
        toggleAircastingManager.toggleAirCasting();
        invalidateOptionsMenu();
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
            case REQUEST_CHECK_SETTINGS:
                if (resultCode != RESULT_OK) {
                    ToastHelper.showText(this, "You need to enable location settings to record an AirCasting session", Toast.LENGTH_LONG);
                } else {
                    locationHelper.startLocationUpdates();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                    onLocationSettingsSatisfied();
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
