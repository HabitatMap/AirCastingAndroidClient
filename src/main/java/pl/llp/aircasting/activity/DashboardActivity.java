package pl.llp.aircasting.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.adapter.StreamAdapterFactory;
import pl.llp.aircasting.activity.events.ToggleSessionReorderEvent;
import pl.llp.aircasting.activity.fragments.DashboardListFragment;
import pl.llp.aircasting.helper.VisibleSession;
import pl.llp.aircasting.model.*;
import pl.llp.aircasting.activity.events.SensorConnectedEvent;

import static pl.llp.aircasting.Intents.startSensors;
import static pl.llp.aircasting.Intents.stopSensors;

public class DashboardActivity extends DashboardBaseActivity {
    @Inject Context context;
    @Inject StreamAdapterFactory adapterFactory;
    @Inject CurrentSessionManager currentSessionManager;
    @Inject VisibleSession visibleSession;
    @Inject ViewingSessionsManager viewingSessionsManager;

    @Override
    protected Dialog onCreateDialog(int id) {
        return super.onCreateDialog(id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intents.startDatabaseWriterService(context);
        setContentView(R.layout.dashboard);
        initToolbar("Dashboard");
        initNavigationDrawer();

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }

            DashboardListFragment dashboardListFragment = DashboardListFragment.newInstance(adapterFactory, state);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, dashboardListFragment).commit();
        }
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
    }

    @Override
    public void onPostResume() {
        Intents.startDatabaseWriterService(context);
        startSensors(context);
        invalidateOptionsMenu();

        if (getIntent().hasExtra("startPopulated") && getIntent().getExtras().getBoolean("startPopulated")) {
            state.dashboardState().populate();
        }

        super.onPostResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopSensors(context);
    }

    @Subscribe
    public void onEvent(SensorConnectedEvent event) {
        invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuInflater inflater = getDelegate().getMenuInflater();

        if (viewingSessionsManager.anySessionPresent()) {
            inflater.inflate(R.menu.toolbar_session_rearrange_toggle, menu);
            MenuItem toggleReorderItem = menu.getItem(0);

            chooseToggleSessionsReorderIcon(toggleReorderItem);
        }

        if (currentSessionManager.isSessionIdle()) {
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

    public void reloadNavigationDrawer() {
        initNavigationDrawer();
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
            case R.id.session_rearrange_toggle:
                toggleSessionReorder(menuItem);
                break;
        }
        return true;
    }

    private void toggleSessionReorder(MenuItem menuItem) {
        state.dashboardState().toggleSessionReorder();
        chooseToggleSessionsReorderIcon(menuItem);
        eventBus.post(new ToggleSessionReorderEvent());
    }

    @Override
    public void onProfileClick(View view) {
        super.onProfileClick(view);
    }

    public void connectPhoneMicrophone() {
        currentSessionManager.startAudioSensor();
    }

    public void viewChartOptions(View view) {
        TextView sensorTitle = (TextView) view.findViewById(R.id.sensor_name);
        String sensorName = (String) sensorTitle.getText();
        Long sessionId = (Long) view.getTag(R.id.session_id_tag);

        visibleSession.setSession(sessionId);
        visibleSession.setSensor(sensorName);

        startActivity(new Intent(context, StreamOptionsActivity.class));
    }
}
