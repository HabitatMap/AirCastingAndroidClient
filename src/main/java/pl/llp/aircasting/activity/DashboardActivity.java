package pl.llp.aircasting.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.*;
import android.widget.TextView;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.adapter.StreamAdapterFactory;
import pl.llp.aircasting.activity.events.SessionLoadedForViewingEvent;
import pl.llp.aircasting.activity.events.ToggleSessionReorderEvent;
import pl.llp.aircasting.activity.fragments.DashboardListFragment;
import pl.llp.aircasting.helper.SessionDataFactory;
import pl.llp.aircasting.helper.VisibleSession;
import pl.llp.aircasting.model.*;
import pl.llp.aircasting.activity.events.SensorConnectedEvent;

import static pl.llp.aircasting.Intents.startSensors;
import static pl.llp.aircasting.Intents.stopSensors;

public class DashboardActivity extends DashboardBaseActivity {
    public static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 2;

    private static final long POLLING_INTERVAL = 60000;
    private static final String LIST_FRAGMENT_TAG = "list_fragment";
    private static final String VIEWING_SESSIONS_IDS = "viewing_session_ids";

    @Inject Context context;
    @Inject StreamAdapterFactory adapterFactory;
    @Inject CurrentSessionManager currentSessionManager;
    @Inject ViewingSessionsManager viewingSessionsManager;
    @Inject SessionDataFactory sessionData;

    private Handler handler = new Handler() {};

    private Thread pollServerTask = new Thread(new Runnable() {
        @Override
        public void run() {
            Intents.triggerStreamingSessionsSync(context);

            handler.postDelayed(pollServerTask, POLLING_INTERVAL);
        }
    });

    @Override
    protected Dialog onCreateDialog(int id) {
        return super.onCreateDialog(id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            viewingSessionsManager.restoreSessions(savedInstanceState.getLongArray(VIEWING_SESSIONS_IDS));
        }

        Intents.startDatabaseWriterService(context);
        setContentView(R.layout.dashboard);
        initToolbar("Dashboard");
        initNavigationDrawer();

        if (findViewById(R.id.fragment_container) != null) {
            DashboardListFragment dashboardListFragment = DashboardListFragment.newInstance(settingsHelper);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, dashboardListFragment, LIST_FRAGMENT_TAG).commit();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @NotNull
    public StreamAdapterFactory getAdapterFactory() {
        return adapterFactory;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPostResume() {
        startUpdatingFixedSessions();

        if (currentSessionSensorManager.anySensorConnected()) {
            startSensors(context);
        }

        invalidateOptionsMenu();

        super.onPostResume();
    }

    private void startUpdatingFixedSessions() {
        if (viewingSessionsManager.isAnySessionFixed()) {
            handler.post(pollServerTask);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        stopSensors(context);
        handler.removeCallbacks(pollServerTask);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putLongArray(VIEWING_SESSIONS_IDS, viewingSessionsManager.getSessionIdsArray());
    }

    @Subscribe
    public void onEvent(SensorConnectedEvent event) {
        invalidateOptionsMenu();
    }

    @Subscribe
    public void onEvent(SessionLoadedForViewingEvent event) {
        startUpdatingFixedSessions();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuInflater inflater = getDelegate().getMenuInflater();

        if (viewingSessionsManager.anySessionPresent()) {
            inflater.inflate(R.menu.toolbar_clear_dashboard, menu);
        }

        if (viewingSessionsManager.getSessionsCount() > 1) {
            inflater.inflate(R.menu.toolbar_session_rearrange_toggle, menu);
            MenuItem toggleReorderItem = menu.getItem(1);

            chooseToggleSessionsReorderIcon(toggleReorderItem);
        }

        if (currentSessionSensorManager.anySensorConnected() && !currentSessionManager.isSessionRecording()) {
            inflater.inflate(R.menu.toolbar_start_recording, menu);
        } else if (currentSessionManager.isSessionRecording()){
            inflater.inflate(R.menu.toolbar_stop_recording, menu);
            inflater.inflate(R.menu.toolbar_make_note, menu);
        } else {
            return true;
        }

        return true;
    }

    private void chooseToggleSessionsReorderIcon(MenuItem item) {
        if (state.dashboardState().isSessionReorderInProgress()) {
            item.setIcon(R.drawable.sessions_rearrange_active);
        } else {
            item.setIcon(R.drawable.sessions_rearrange_inactive);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        super.onOptionsItemSelected(menuItem);

        switch (menuItem.getItemId()) {
            case R.id.toggle_aircasting:
                super.toggleAirCasting();
                break;
            case R.id.make_note:
                Intents.makeANote(this);
                break;
            case R.id.clear_dashboard_button:
                sessionData.clearAllViewingSessions();
                eventBus.post(new ToggleSessionReorderEvent(true));
                break;
            case R.id.session_rearrange_toggle:
                toggleSessionReorder(menuItem);
                break;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    connectPhoneMicrophone();
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationHelper.start();
                }
                return;
        }
    }

    private void toggleSessionReorder(MenuItem menuItem) {
        state.dashboardState().toggleSessionReorder();
        chooseToggleSessionsReorderIcon(menuItem);
        eventBus.post(new ToggleSessionReorderEvent(false));
    }

    @Override
    public void onProfileClick(View view) {
        super.onProfileClick(view);
    }

    public void connectPhoneMicrophone() {
        currentSessionManager.setSession(new Session());
        currentSessionSensorManager.startAudioSensor();
        reloadNavigationDrawer();
        invalidateOptionsMenu();
    }

    private void reloadNavigationDrawer() {
        initNavigationDrawer();
    }

    public void viewChartOptions(View view) {
        TextView sensorTitle = (TextView) view.findViewById(R.id.sensor_name);
        String sensorName = (String) sensorTitle.getText();
        Long sessionId = (Long) view.getTag(R.id.session_id_tag);

        Session session = sessionData.getSession(sessionId);
        Sensor sensor = sessionData.getSensor(sensorName, sessionId);

        if (!sessionState.isSessionCurrent(sessionId)) {
            viewingSessionsManager.setVisibleSession(sessionId);
            viewingSessionsManager.setVisibleSensor(sensor);
        } else {
            currentSessionManager.setVisibleSession(session);
            currentSessionManager.setVisibleSensor(sensor);
        }

        if (session.isFixed() && session.isIndoor()) {
            startActivity(new Intent(context, GraphActivity.class));
        } else {
            startActivity(new Intent(context, StreamOptionsActivity.class));
        }
    }
}
