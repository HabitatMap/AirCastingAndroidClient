package pl.llp.aircasting.activity;

import android.content.Context;
import android.os.Bundle;
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

            DashboardListFragment dashboardListFragment = DashboardListFragment.newInstance(adapterFactory);
            dashboardListFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, dashboardListFragment).commit();
        }

        initToolbar("Dashboard");
        initNavigationDrawer();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intents.startDatabaseWriterService(context);

        startSensors(context);
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
    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.graph_button:
//                if (adapter.getCount() == 1) {
//                    startActivity(new Intent(this, GraphActivity.class));
//                } else {
//                    Toast.makeText(this, R.string.drag_to_graph_stream, Toast.LENGTH_SHORT).show();
//                }
//                break;
//            case R.id.heat_map_button:
//                if (adapter.getCount() == 1) {
//                    if (sessionManager.isLocationless()) {
//                        Toast.makeText(DashboardActivity.this, R.string.cant_map_without_gps, Toast.LENGTH_SHORT).show();
//                    } else {
//                        context.startActivity(new Intent(DashboardActivity.this, AirCastingMapActivity.class));
//                    }
//                } else {
//                    Toast.makeText(context, R.string.drag_to_map_stream, Toast.LENGTH_LONG).show();
//                }
//                break;
//        }
    }

    @Override
    public void onProfileClick(View view) {
        super.onProfileClick(view);
    }
}
