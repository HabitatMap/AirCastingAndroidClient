package pl.llp.aircasting.screens.dashboard;

import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Map;

import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.event.sensor.FixedSensorEvent;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.event.sensor.SessionSensorsLoadedEvent;
import pl.llp.aircasting.event.session.SessionStoppedEvent;
import pl.llp.aircasting.model.internal.SensorName;
import pl.llp.aircasting.screens.common.ToastHelper;
import pl.llp.aircasting.screens.common.helpers.ResourceHelper;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionSensorManager;
import pl.llp.aircasting.screens.dashboard.events.NewChartAveragesEvent;
import pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel;
import pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModelFactory;
import pl.llp.aircasting.screens.dashboard.views.DashboardViewMvc;
import pl.llp.aircasting.screens.dashboard.views.DashboardViewMvcImpl;
import pl.llp.aircasting.screens.extsens.ExternalSensorActivity;
import pl.llp.aircasting.screens.stream.graph.GraphActivity;
import pl.llp.aircasting.event.session.SessionLoadedForViewingEvent;
import pl.llp.aircasting.screens.common.sessionState.SessionDataFactory;
import pl.llp.aircasting.model.*;
import pl.llp.aircasting.event.sensor.SensorConnectedEvent;

import static pl.llp.aircasting.Intents.startSensors;
import static pl.llp.aircasting.screens.dashboard.DashboardChartManager.CURRENT_CHART;
import static pl.llp.aircasting.screens.dashboard.DashboardChartManager.STATIC_CHART;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.SENSOR;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.SESSION_ID;
import static pl.llp.aircasting.screens.dashboard.views.DashboardViewMvc.CURRENT_ITEM;
import static pl.llp.aircasting.screens.dashboard.views.DashboardViewMvc.VIEWING_ITEM;
import static pl.llp.aircasting.util.Constants.PERMISSIONS;
import static pl.llp.aircasting.util.Constants.PERMISSIONS_ALL;

public class DashboardActivity extends DashboardBaseActivity implements DashboardViewMvc.Listener {
    private static final long POLLING_INTERVAL = 60000;
    private static final String VIEWING_SESSIONS_IDS = "viewing_session_ids";
    private static final String DASHBOARD_BUNDLE = "dashboard_bundle";
    private static final String HIDDEN_STREAMS = "hidden_streams";

    @Inject CurrentSessionSensorManager currentSessionSensorManager;
    @Inject ResourceHelper mResourceHelper;
    @Inject SessionDataFactory sessionData;
    @Inject DashboardViewModelFactory mDashboardViewModelFactory;

    private Handler handler = new Handler() {};

    private Thread pollServerTask = new Thread(new Runnable() {
        @Override
        public void run() {
            Intents.triggerStreamingSessionsSync(context);

            handler.postDelayed(pollServerTask, POLLING_INTERVAL);
        }
    });

    private DashboardViewMvcImpl mDashboardViewMvc;
    private DashboardViewModel mDashboardViewModel;

    @Override
    protected Dialog onCreateDialog(int id) {
        return super.onCreateDialog(id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intents.startDatabaseWriterService(context);

        mDashboardViewMvc = new DashboardViewMvcImpl(this, null, mResourceHelper);
        mDashboardViewMvc.registerListener(this);
        setContentView(mDashboardViewMvc.getRootView());
        initToolbar("Dashboard");
        initNavigationDrawer();

        mDashboardViewModel = ViewModelProviders.of(this, mDashboardViewModelFactory).get(DashboardViewModel.class);
        mDashboardViewModel.init();
        observeViewModel();

        if (savedInstanceState != null) {
            Bundle bundle = savedInstanceState.getBundle(DASHBOARD_BUNDLE);
            mDashboardViewMvc.restoreAdapterState(bundle);
            mDashboardViewModel.restoreViewingSensors((ArrayList<String>) bundle.getSerializable(HIDDEN_STREAMS), bundle.getLongArray(VIEWING_SESSIONS_IDS));
        }
    }

    private void observeViewModel() {
        mDashboardViewModel.getRecentMeasurements().observe(this, new Observer<Map<String, Double>>() {
            @Override
            public void onChanged(@Nullable Map<String, Double> recentMeasurements) {
                mDashboardViewMvc.bindRecentMeasurements(mDashboardViewModel.getRecentMeasurements().getValue());
            }
        });

        mDashboardViewModel.getCurrentSensors().observe(this, new Observer<Map<SensorName, Sensor>>() {
            @Override
            public void onChanged(@Nullable Map<SensorName, Sensor> currentSensors) {
                mDashboardViewMvc.bindSensorData(mDashboardViewModel.getCurrentDashboardData().getValue());
            }
        });

        mDashboardViewModel.getLiveCharts().observe(this, new Observer<Map<String, LineChart>>() {
            @Override
            public void onChanged(@Nullable Map<String, LineChart> liveCharts) {
                mDashboardViewMvc.bindChartData(mDashboardViewModel.getLiveCharts().getValue());
            }
        });

        mDashboardViewModel.getViewingSensors().observe(this, new Observer<Map<Long, Map<SensorName, Sensor>>>() {
            @Override
            public void onChanged(@Nullable Map<Long, Map<SensorName, Sensor>> viewingSensors) {
                mDashboardViewModel.refreshChartAverages();
                mDashboardViewMvc.bindViewingSensorsData(mDashboardViewModel.getViewingDashboardData().getValue());
                toggleProgress();
            }
        });

        mDashboardViewModel.getStaticCharts().observe(this, new Observer<Map<String, LineChart>>() {
            @Override
            public void onChanged(@Nullable Map<String, LineChart> staticChartsMap) {
                mDashboardViewMvc.bindStaticChartData(mDashboardViewModel.getStaticCharts().getValue());
            }
        });

        mDashboardViewModel.getRecentFixedMeasurements().observe(this, new Observer<Map<String, Measurement>>() {
            @Override
            public void onChanged(@Nullable Map<String, Measurement> recentFixedMeasurements) {
                mDashboardViewMvc.bindViewingSensorsData(mDashboardViewModel.getViewingDashboardData().getValue());
            }
        });
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        eventBus.register(this);

        mDashboardViewModel.refreshCurrentSensors();
        mDashboardViewModel.refreshViewingSensors();
        mDashboardViewModel.refreshLiveCharts();
        mDashboardViewModel.refreshStaticCharts();
        mDashboardViewModel.refreshRecentFixedMeasurements();

        if (viewingSessionsManager.anySessionsLoading()) {
            ToastHelper.show(this, R.string.session_is_loading, Toast.LENGTH_SHORT);
        }
        toggleProgress();

        startUpdatingFixedSessions();

        if (viewingSessionsManager.anySessionPresent() || currentSessionManager.anySensorConnected()) {
            startSensors(context);
        }

        invalidateOptionsMenu();
    }

    private void toggleProgress() {
        if (viewingSessionsManager.anySessionsLoading()) {
            mDashboardViewMvc.showProgress();
        } else {
            mDashboardViewMvc.hideProgress();
        }
    }

    private void startUpdatingFixedSessions() {
        if (viewingSessionsManager.isAnySessionFixed()) {
            handler.removeCallbacks(pollServerTask);
            handler.post(pollServerTask);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(pollServerTask);

        if (viewingSessionsManager.sessionsEmpty() && !currentSessionManager.isSessionRecording()) {
            Intents.stopSensors(this);
        }

        if (!currentSessionManager.anySensorConnected()) {
            locationHelper.stopLocationUpdates();
        }

        if (registeredReceiver != null) {
            unregisterReceiver(syncBroadcastReceiver);
            registeredReceiver = null;
        }

        eventBus.unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle bundle = new Bundle();
        bundle = mDashboardViewMvc.saveAdapterState(bundle);
        bundle.putLongArray(VIEWING_SESSIONS_IDS, mDashboardViewModel.getViewingSessionIds());
        bundle.putSerializable(HIDDEN_STREAMS, mDashboardViewModel.getHiddenStreams());
        outState.putBundle(DASHBOARD_BUNDLE, bundle);
    }

    @Subscribe
    public void onEvent(SensorConnectedEvent event) {
        mDashboardViewModel.refreshCurrentSensors();
        invalidateOptionsMenu();
    }

    @Subscribe
    public void onEvent(SensorEvent event) {
        mDashboardViewModel.refreshRecentMeasurements();
    }

    @Subscribe
    public void onEvent(FixedSensorEvent event) {
        mDashboardViewModel.refreshRecentFixedMeasurements();
    }

    @Subscribe
    public void onEvent(NewChartAveragesEvent event) {
        switch (event.getChartType()) {
            case CURRENT_CHART:
                mDashboardViewModel.refreshLiveCharts();
            case STATIC_CHART:
                mDashboardViewModel.refreshStaticCharts();
                mDashboardViewModel.refreshRecentFixedMeasurements();
        }
    }

    @Subscribe
    public void onEvent(SessionStoppedEvent event) {
        mDashboardViewModel.refreshCurrentSensors();
    }

    @Subscribe
    public void onEvent(SessionSensorsLoadedEvent event) {
        mDashboardViewModel.refreshViewingSensors();
        invalidateOptionsMenu();
    }

    @Subscribe
    public void onEvent(SessionLoadedForViewingEvent event) {
        startUpdatingFixedSessions();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();

        if (viewingSessionsManager.anySessionPresent()) {
            inflater.inflate(R.menu.toolbar_clear_dashboard, menu);
        }

        if (viewingSessionsManager.getSessionsCount() > 1) {
            inflater.inflate(R.menu.toolbar_session_rearrange_toggle, menu);
            MenuItem toggleReorderItem = menu.getItem(1);

            chooseToggleSessionsReorderIcon(toggleReorderItem);
        }

        if (currentSessionManager.anySensorConnected() && !currentSessionManager.isSessionRecording()) {
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
                handler.removeCallbacks(pollServerTask);
                mDashboardViewModel.clearAllViewingSensors();
                mDashboardViewMvc.resetAdapterState();
                break;
            case R.id.session_rearrange_toggle:
                toggleSessionReorder(menuItem);
                break;
        }
        return true;
    }

    private void toggleSessionReorder(MenuItem menuItem) {
        state.dashboardState().toggleSessionReorder();
        chooseToggleSessionsReorderIcon(menuItem);
        mDashboardViewModel.refreshViewingSensors();
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

    @Override
    public void onStreamClicked(View view) {
        TextView sensorTitle = view.findViewById(R.id.sensor_name);
        String sensorName = (String) sensorTitle.getText();
        Long sessionId = (Long) view.getTag(R.id.session_id_tag);

        Session session = sessionData.getSession(sessionId);
        Sensor sensor = sessionData.getSensor(sensorName, sessionId);

        sessionData.setVisibleSession(sessionId);
        sessionData.setVisibleSensor(sensor);

        if (session.isFixed() && session.isIndoor()) {
            startActivity(new Intent(context, GraphActivity.class));
        } else {
            startActivity(new Intent(context, StreamOptionsActivity.class));
        }
    }

    @Override
    public void onDashboardButtonClicked(View view) {
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSIONS_ALL);
        }

        switch(view.getId()) {
            case R.id.dashboard_microphone:
                connectPhoneMicrophone();
                break;
            case R.id.dashboard_sensors:
                startActivity(new Intent(this, ExternalSensorActivity.class));
                break;
            case R.id.configure_airbeam2:
                if (settingsHelper.hasCredentials()) {
                    Intents.startAirbeam2Configuration(this);
                } else {
                    ToastHelper.show(context, R.string.sign_in_to_configure, Toast.LENGTH_SHORT);
                }
                break;
        }
    }

    @Override
    public void onItemSwipe(int position, Map dataItem, boolean noStreamsLeft, int direction, int itemType) {
        if (direction == ItemTouchHelper.START) {
            deleteStreamIfPossible(position, dataItem, itemType, noStreamsLeft);
        } else if (direction == ItemTouchHelper.END) {
            hideStreamIfPossible(position, dataItem, itemType, noStreamsLeft);
        }
    }

    private void deleteStreamIfPossible(int position, Map dataItem, int itemType, boolean noStreamsLeft) {
        if (itemType == CURRENT_ITEM) {
            unableToDeleteStreamMessage();
        } else if (itemType == VIEWING_ITEM) {
            long sessionId = (long) dataItem.get(SESSION_ID);

            if (sessionData.getSession(sessionId).getStreamsSize() > 1) {
                confirmStreamDelete(position, dataItem, noStreamsLeft);
            } else {
                confirmSessionDelete(position, dataItem, noStreamsLeft);
            }
        }
    }

    private void hideStreamIfPossible(int position, Map dataItem, int itemType, boolean noStreamsLeft) {
        if (itemType == CURRENT_ITEM) {
            unableToDeleteStreamMessage();
        } else if (itemType == VIEWING_ITEM) {
            mDashboardViewModel.hideStream(dataItem);
            removeOneOrAll(position, noStreamsLeft);
        }
    }

    private void removeOneOrAll(int position, boolean noStreamsLeft) {
        if (noStreamsLeft) {
            mDashboardViewModel.clearAllViewingSensors();
        } else {
            mDashboardViewMvc.itemRemoved(position);
        }
    }

    private void confirmStreamDelete(final int position, final Map dataItem, final boolean noStreamsLeft) {
        AlertDialog.Builder b = new AlertDialog.Builder(context);
        b.setMessage("Delete stream?").
                setCancelable(true).
                setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Sensor sensor = (Sensor) dataItem.get(SENSOR);
                        long sessionId = (long) dataItem.get(SESSION_ID);
                        sessionData.deleteSensorStream(sensor, sessionId);
                        Intents.triggerSync(context);
                        removeOneOrAll(position, noStreamsLeft);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDashboardViewMvc.cancelSwipe(position);
                    }
                });
        AlertDialog dialog = b.create();
        dialog.show();
    }

    private void confirmSessionDelete(final int position, final Map dataItem, final boolean noStreamsLeft) {
        AlertDialog.Builder b = new AlertDialog.Builder(context);
        b.setMessage("This is the only stream, delete session?").
                setCancelable(true).
                setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Sensor sensor = (Sensor) dataItem.get(SENSOR);
                        long sessionId = (long) dataItem.get(SESSION_ID);
                        sessionData.deleteSensorStream(sensor, sessionId);
                        sessionData.deleteSession(sessionId);
                        Intents.triggerSync(context);
                        removeOneOrAll(position, noStreamsLeft);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDashboardViewMvc.cancelSwipe(position);
                    }
                });
        AlertDialog dialog = b.create();
        dialog.show();
    }

    private void unableToDeleteStreamMessage() {
        ToastHelper.show(this, R.string.wrong_session_type, Toast.LENGTH_SHORT);
    }
}
