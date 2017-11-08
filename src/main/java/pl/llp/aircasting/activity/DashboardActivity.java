package pl.llp.aircasting.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import com.google.inject.Inject;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.adapter.StreamAdapterFactory;
import pl.llp.aircasting.activity.fragments.DashboardListFragment;
import pl.llp.aircasting.helper.VisibleSession;
import pl.llp.aircasting.model.*;

import static pl.llp.aircasting.Intents.startSensors;
import static pl.llp.aircasting.Intents.stopSensors;

public class DashboardActivity extends DashboardBaseActivity {
    @Inject Context context;
    @Inject StreamAdapterFactory adapterFactory;
    @Inject CurrentSessionManager currentSessionManager;
    @Inject VisibleSession visibleSession;

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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuInflater inflater = getDelegate().getMenuInflater();
        DashboardListFragment listFragment = (DashboardListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (!listFragment.isAdapterSet()) {
            return true;
        }

        if (!currentSessionManager.isSessionRecording()) {
            inflater.inflate(R.menu.toolbar_start_recording, menu);
        } else if (currentSessionManager.isSessionRecording()){
            inflater.inflate(R.menu.toolbar_stop_recording, menu);
            inflater.inflate(R.menu.toolbar_make_note, menu);
        } else {
            return true;
        }

        return true;
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
        }
        return true;
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

        startActivity(new Intent(context, ChartOptionsActivity.class));
    }
}
