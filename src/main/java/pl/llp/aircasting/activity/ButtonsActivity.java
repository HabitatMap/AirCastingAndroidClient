package pl.llp.aircasting.activity;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.internal.Nullable;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.menu.MainMenu;
import pl.llp.aircasting.event.ui.TapEvent;
import pl.llp.aircasting.helper.LocationHelper;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.SessionManager;
import pl.llp.aircasting.receiver.SyncBroadcastReceiver;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

/**
 * A common superclass for activities that want to display left/right
 * navigation arrows
 */
public abstract class ButtonsActivity extends RoboMapActivityWithProgress implements Animation.AnimationListener, View.OnClickListener {
    public static final String SHOW_BUTTONS = "showButtons";

    @Inject Context context;
    @Inject EventBus eventBus;

    // It seems it's impossible to inject these in the tests
    @Nullable @InjectResource(R.anim.fade_out) Animation fadeOut;
    @Nullable @InjectResource(R.anim.fade_in) Animation fadeIn;
    @InjectView(R.id.buttons) View buttons;

    @Nullable @InjectView(R.id.heat_map_button) ImageButton heatMapButton;
    @Nullable @InjectView(R.id.streams_button) ImageButton streamsButton;
    @Nullable @InjectView(R.id.graph_button) ImageButton graphButton;
    @Nullable @InjectView(R.id.trace_button) ImageButton traceButton;

    @InjectView(R.id.context_button_center) FrameLayout contextButtonCenter;
    @InjectView(R.id.context_button_right) FrameLayout contextButtonRight;
    @InjectView(R.id.context_button_left) FrameLayout contextButtonLeft;

    @Inject LocationManager locationManager;
    @Inject SessionManager sessionManager;
    @Inject LayoutInflater layoutInflater;
    @Inject LocationHelper locationHelper;
    @Inject SettingsHelper settingsHelper;
    @Inject MainMenu mainMenu;

    private boolean suppressTap = false;
    private boolean initialized = false;
    @Inject SyncBroadcastReceiver syncBroadcastReceiver;
    @InjectView(R.id.zoom_in) Button zoomIn;
    @InjectView(R.id.zoom_out) Button zoomOut;

    @Override
    protected void onResume() {
        super.onResume();

        initialize();

        updateButtons();

        if (!sessionManager.isSessionSaved()) {
            Intents.startSensors(context);
        }

        registerReceiver(syncBroadcastReceiver, SyncBroadcastReceiver.INTENT_FILTER);
        eventBus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!sessionManager.isSessionSaved()) {
            Intents.stopSensors(context);
        }

        unregisterReceiver(syncBroadcastReceiver);
        eventBus.unregister(this);
    }

    @Override
    protected boolean isRouteDisplayed() {
        // The maps server needs to know if we are displaying any routes
        return false;
    }

    private void initialize() {
        if (!initialized) {
            fadeIn.setAnimationListener(this);
            fadeOut.setAnimationListener(this);

            if (showButtons) {
                buttons.setVisibility(View.VISIBLE);
            } else {
                buttons.setVisibility(View.GONE);
            }

            if (graphButton != null) graphButton.setOnClickListener(this);
            if (traceButton != null) traceButton.setOnClickListener(this);
            if (heatMapButton != null) heatMapButton.setOnClickListener(this);
            if (streamsButton != null) streamsButton.setOnClickListener(this);

            initialized = true;
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        showButtons = savedInstanceState.getBoolean(SHOW_BUTTONS, true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(SHOW_BUTTONS, showButtons);
    }

    protected boolean showButtons = true;

    protected void toggleButtons() {
        if (buttons.getVisibility() == View.VISIBLE) {
            hideButtons();
        } else {
            showButtons();
        }
    }

    /**
     * The next tap will not change the state of the toggleable
     * buttons. Use this if clicking something makes the toggleable
     * buttons toggle.
     */
    public void suppressNextTap() {
        suppressTap = true;
    }

    private void hideButtons() {
        buttons.startAnimation(fadeOut);
        showButtons = false;
    }

    private void showButtons() {
        buttons.startAnimation(fadeIn);
        showButtons = true;
    }

    @Override
    public void onAnimationStart(Animation animation) {
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if (buttons.getVisibility() == View.VISIBLE) {
            buttons.setVisibility(View.GONE);
        } else {
            buttons.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.graph_button:
                startActivity(new Intent(context, GraphActivity.class));
                break;
            case R.id.trace_button:
                startActivity(new Intent(context, SoundTraceActivity.class));
                break;
            case R.id.heat_map_button:
                startActivity(new Intent(context, HeatMapActivity.class));
                break;
            case R.id.streams_button:
                startActivity(new Intent(context, StreamsActivity.class));
                break;
            case R.id.toggle_aircasting:
                toggleAirCasting();
                break;
            case R.id.edit:
                Intents.editSession(this, sessionManager.getSession());
                break;
            case R.id.note:
                Intents.makeANote(this);
                break;
            case R.id.share:
                startActivity(new Intent(context, ShareSessionActivity.class));
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return mainMenu.create(this, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mainMenu.handleClick(this, item);
    }

    @Subscribe
    public void onEvent(TapEvent event) {
        if (suppressTap) {
            suppressTap = false;
        } else {
            toggleButtons();
        }
    }

    protected void updateButtons() {
        if (sessionManager.isSessionSaved()) {
            setButton(contextButtonLeft, R.layout.context_button_edit);
            setButton(contextButtonCenter, R.layout.context_button_share);
        } else if (sessionManager.isSessionStarted()) {
            setButton(contextButtonLeft, R.layout.context_button_stop);
            setButton(contextButtonRight, R.layout.context_button_note);
        } else if (!sessionManager.isSessionSaved()) {
            setButton(contextButtonLeft, R.layout.context_button_play);
            setButton(contextButtonRight, R.layout.context_button_placeholder);
        }
    }

    protected void setButton(FrameLayout layout, int id) {
        layout.removeAllViews();

        View view = layoutInflater.inflate(id, null);
        view.setOnClickListener(this);

        layout.addView(view);
    }

    private synchronized void toggleAirCasting() {
        if (sessionManager.isSessionStarted()) {
            stopAirCasting();
        } else {
            startAirCasting();
        }

        updateButtons();
    }

    private void stopAirCasting() {
        if (sessionManager.getSession().isEmpty()) {
            Toast.makeText(context, R.string.no_data, Toast.LENGTH_SHORT).show();
            sessionManager.discardSession();
        } else {
            Intent intent = new Intent(this, SaveSessionActivity.class);
            startActivityForResult(intent, Intents.SAVE_DIALOG);
        }
    }

    private void startAirCasting() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(context, R.string.gps_off_warning, Toast.LENGTH_LONG).show();
        } else if (!locationHelper.hasGPSFix()) {
            Toast.makeText(context, R.string.no_gps_fix_warning, Toast.LENGTH_LONG).show();
        }

        if (!settingsHelper.hasCredentials()) {
            Toast.makeText(context, R.string.account_reminder, Toast.LENGTH_LONG).show();
        }

        sessionManager.startSession();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Intents.SAVE_DIALOG:
                startActivity(new Intent(getApplicationContext(), SoundTraceActivity.class));
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
