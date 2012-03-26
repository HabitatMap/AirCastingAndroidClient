package pl.llp.aircasting.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageButton;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.internal.Nullable;
import pl.llp.aircasting.R;
import pl.llp.aircasting.event.ui.TapEvent;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

/***
 * A common superclass for activities that want to display left/right
 * navigation arrows
 */
public abstract class ButtonsActivity extends RoboMapActivityWithProgress implements Animation.AnimationListener, View.OnClickListener {
    public static final String SHOW_BUTTONS = "showButtons";

    @Inject Context context;
    @Inject EventBus eventBus;

    // It seems it's impossible to inject these in the tests
    @Nullable @InjectResource(R.anim.fade_in) Animation fadeIn;
    @Nullable @InjectResource(R.anim.fade_out) Animation fadeOut;
    @InjectView(R.id.buttons) View buttons;

    @Nullable @InjectView(R.id.graph_button) ImageButton graphButton;
    @Nullable @InjectView(R.id.trace_button) ImageButton traceButton;
    @Nullable @InjectView(R.id.heat_map_button) ImageButton heatMapButton;
    @Nullable @InjectView(R.id.streams_button) ImageButton streamsButton;

    private boolean initialized = false;

    @Override
    protected void onResume() {
        super.onResume();

        initialize();

        eventBus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

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
        }
    }

    @Subscribe
    public void onEvent(TapEvent event) {
        toggleButtons();
    }
}
