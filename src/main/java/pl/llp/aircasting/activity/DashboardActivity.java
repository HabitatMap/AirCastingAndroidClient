package pl.llp.aircasting.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.*;
import com.google.inject.Inject;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.adapter.StreamAdapterFactory;
import pl.llp.aircasting.activity.fragments.DashboardListFragment;
import pl.llp.aircasting.model.*;

import static pl.llp.aircasting.Intents.startSensors;
import static pl.llp.aircasting.Intents.stopSensors;

public class DashboardActivity extends DashboardBaseActivity {
    @Inject Context context;
    @Inject StreamAdapterFactory adapterFactory;
    @Inject SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intents.startDatabaseWriterService(context);
        setContentView(R.layout.dashboard);

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }

            DashboardListFragment dashboardListFragment = DashboardListFragment.newInstance(adapterFactory, false);
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, dashboardListFragment).commit();
        }

        initToolbar("Dashboard");
        initNavigationDrawer();
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
    protected void onResume() {
        super.onResume();

        invalidateOptionsMenu();

        Intents.startDatabaseWriterService(context);
        startSensors(context);
    }

    @Override
    public void onResumeFragments() {
        super.onResumeFragments();

        if (getIntent().hasExtra("startPopulated")) {
            boolean startPopulated = getIntent().getExtras().getBoolean("startPopulated");
            DashboardListFragment dashboardListFragment = DashboardListFragment.newInstance(adapterFactory, startPopulated);
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, dashboardListFragment).commit();
        }
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

        if (listFragment.isAdapterSet() == false) {
            return true;
        }

        if (!sessionManager.isRecording()) {
            inflater.inflate(R.menu.toolbar_start_recording, menu);
        } else {
            inflater.inflate(R.menu.toolbar_stop_recording, menu);
            inflater.inflate(R.menu.toolbar_make_note, menu);
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
}
