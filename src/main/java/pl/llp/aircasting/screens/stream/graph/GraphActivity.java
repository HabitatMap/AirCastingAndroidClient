/**
 * AirCasting - Share your Air!
 * Copyright (C) 2011-2012 HabitatMap, Inc.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * You can contact the authors by email at <info@habitatmap.org>
 */
package pl.llp.aircasting.screens.stream.graph;

import android.os.Handler;
import android.view.MenuItem;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.event.ui.VisibleStreamUpdatedEvent;
import pl.llp.aircasting.R;
import pl.llp.aircasting.event.ui.DoubleTapEvent;
import pl.llp.aircasting.event.ui.ScrollEvent;
import pl.llp.aircasting.event.ui.TapEvent;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.screens.common.sessionState.ViewingSessionsManager;
import pl.llp.aircasting.screens.stream.base.AirCastingActivity;
import pl.llp.aircasting.screens.stream.MeasurementPresenter;
import pl.llp.aircasting.sensor.common.ThresholdsHolder;

import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import roboguice.inject.InjectView;

import java.util.ArrayList;

import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Lists.newArrayList;

public class GraphActivity extends AirCastingActivity implements View.OnClickListener, MeasurementPresenter.Listener {
    @InjectView(R.id.noise_graph)
    NoisePlot plot;

    @InjectView(R.id.graph_begin_time)
    TextView graphBegin;
    @InjectView(R.id.graph_end_time)
    TextView graphEnd;

    @InjectView(R.id.suggest_scroll_left)
    View scrollLeft;
    @InjectView(R.id.suggest_scroll_right)
    View scrollRight;

    @Inject
    MeasurementPresenter measurementPresenter;
    @Inject
    ThresholdsHolder thresholdsHolder;
    @Inject
    ViewingSessionsManager viewingSessionsManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.graph);

        initToolbar("Graph");
        initNavigationDrawer();

        plot.initialize(this, settingsHelper, thresholdsHolder, resourceHelper);

        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
        measurementPresenter.registerListener(this);
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        getDelegate().invalidateOptionsMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        measurementPresenter.unregisterListener(this);
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
        switch (view.getId()) {
            case R.id.zoom_in:
                zoomIn();
                break;
            case R.id.zoom_out:
                zoomOut();
                break;
            default:
                super.onClick(view);
        }
    }

    @Override
    public void onEvent(VisibleStreamUpdatedEvent event) {
        super.onEvent(event);
        refresh();
    }

    @Override
    public void onViewUpdated() {
        refresh();
    }

    @Override
    public void onAveragedMeasurement(Measurement measurement) {
    }

    @Override
    protected void refreshNotes() {
        refresh();
    }

    private void refresh() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                zoomIn.setEnabled(measurementPresenter.canZoomIn());
                zoomOut.setEnabled(measurementPresenter.canZoomOut());

                ArrayList<Measurement> measurements = newArrayList(measurementPresenter.getTimelineView());
                ArrayList<Note> notes = newArrayList(visibleSession.getSessionNotes());

                plot.update(visibleSession.getSensor(), measurements, notes);

                updateLabels(measurements);
            }
        });
    }

    private void updateLabels(ArrayList<Measurement> measurements) {
        if (!measurements.isEmpty()) {
            graphBegin.setText(DateFormat.format("HH:mm:ss", measurements.get(0).getTime()));
            graphEnd.setText(DateFormat.format("HH:mm:ss", getLast(measurements).getTime()));
        }
    }

    private void zoomIn() {
        measurementPresenter.zoomIn();
    }

    private void zoomOut() {
        measurementPresenter.zoomOut();
    }

    @Subscribe
    public void onEvent(TapEvent event) {
        if (!plot.onTap(event.getMotionEvent())) {
            return;
        }
    }

    @Subscribe
    public void onEvent(DoubleTapEvent event) {
        zoomIn();
    }

    @Subscribe
    public void onEvent(ScrollEvent event) {
        float relativeScroll = event.getDistanceX() / plot.getWidth();
        measurementPresenter.scroll(relativeScroll);
    }
}
