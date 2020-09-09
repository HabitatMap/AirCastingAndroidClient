package pl.llp.aircasting.screens.stream.base;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.internal.Nullable;

import java.util.concurrent.TimeUnit;

import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.screens.common.ApplicationState;
import pl.llp.aircasting.screens.common.ToastHelper;
import pl.llp.aircasting.screens.common.ToggleAircastingManager;
import pl.llp.aircasting.screens.common.ToggleAircastingManagerFactory;
import pl.llp.aircasting.screens.common.base.RoboMapActivityWithProgress;
import pl.llp.aircasting.screens.common.helpers.LocationHelper;
import pl.llp.aircasting.screens.common.helpers.SettingsHelper;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionManager;
import pl.llp.aircasting.screens.common.sessionState.ViewingSessionsManager;
import pl.llp.aircasting.screens.dashboard.DashboardActivity;
import pl.llp.aircasting.screens.stream.MeasurementPresenter;
import pl.llp.aircasting.sessionSync.SyncBroadcastReceiver;
import pl.llp.aircasting.storage.UnfinishedSessionChecker;
import roboguice.inject.InjectView;

import static pl.llp.aircasting.Intents.startSensors;
import static pl.llp.aircasting.screens.common.helpers.LocationHelper.REQUEST_CHECK_SETTINGS;

/**
 * A common superclass for activities that want to display left/right
 * navigation arrows
 */

public abstract class AirCastingBaseActivityForMap extends RoboMapActivityWithProgress implements View.OnClickListener, LocationHelper.LocationSettingsListener {
    public static final long DELTA = TimeUnit.SECONDS.toMillis(15);

    @Inject public Context context;
    @Inject public LocationHelper locationHelper;
    @Inject public SettingsHelper settingsHelper;
    @Inject public CurrentSessionManager currentSessionManager;
    @Inject public MeasurementPresenter measurementPresenter;

    @Inject UnfinishedSessionChecker checker;
    @Inject ApplicationState state;
    @Inject ViewingSessionsManager viewingSessionsManager;
    @Inject EventBus eventBus;
    @Inject ToggleAircastingManagerFactory aircastingHelperFactory;

    @Inject
    SyncBroadcastReceiver syncBroadcastReceiver;
    SyncBroadcastReceiver registeredReceiver;

    @Nullable
    @InjectView(R.id.zoom_in) public Button zoomIn;

    @Nullable
    @InjectView(R.id.zoom_out) public Button zoomOut;

    private ToggleAircastingManager toggleAircastingManager;
    private boolean initialized = false;
    private long lastChecked = 0;

    @Override
    protected void onResume() {
        super.onResume();

        initialize();

        registerReceiver(syncBroadcastReceiver, SyncBroadcastReceiver.INTENT_FILTER);
        registeredReceiver = syncBroadcastReceiver;

        eventBus.register(this);
        checkForUnfinishedSessions();

        if (viewingSessionsManager.anySessionPresent() || currentSessionManager.anySensorConnected()) {
            startSensors(context);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (viewingSessionsManager.sessionsEmpty() && !currentSessionManager.isSessionRecording()) {
            Intents.stopSensors(this);
        }

//        if (!currentSessionManager.isSessionRecording()) {
//            locationHelper.stopLocationUpdates();
//        }

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
        toggleAircastingManager = aircastingHelperFactory.getAircastingHelper(this, getDelegate());

        if (!initialized) {
            initialized = true;
        }
    }

    @Override
    public void onProfileClick(View view) {
        super.onProfileClick(view);
    }

    public synchronized void toggleAirCasting() {
        toggleAircastingManager.toggleAirCasting();
        getDelegate().invalidateOptionsMenu();

        measurementPresenter.reset();
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
                    checker.finishIfNeeded(AirCastingBaseActivityForMap.this);
                    lastChecked = System.currentTimeMillis();
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

        long timeout = System.currentTimeMillis() - lastChecked;
        return timeout > DELTA;
    }
}
