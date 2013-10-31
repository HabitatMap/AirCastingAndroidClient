/**
 AirCasting - Share your Air!
 Copyright (C) 2011-2012 HabitatMap, Inc.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 You can contact the authors by email at <info@habitatmap.org>
 */
package pl.llp.aircasting.activity;

import pl.llp.aircasting.model.internal.MeasurementLevel;
import pl.llp.aircasting.R;
import pl.llp.aircasting.event.ui.DoubleTapEvent;
import pl.llp.aircasting.event.ui.ScrollEvent;
import pl.llp.aircasting.event.ui.TapEvent;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.sensor.ThresholdsHolder;
import pl.llp.aircasting.view.NoisePlot;
import pl.llp.aircasting.view.presenter.MeasurementPresenter;

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
    @InjectView(R.id.noise_graph) NoisePlot plot;

    @InjectView(R.id.graph_begin_time) TextView graphBegin;
    @InjectView(R.id.graph_end_time) TextView graphEnd;

    @InjectView(R.id.suggest_scroll_left) View scrollLeft;
    @InjectView(R.id.suggest_scroll_right) View scrollRight;

    @Inject MeasurementPresenter measurementPresenter;
  @Inject ThresholdsHolder thresholdsHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph);

        plot.initialize(this, settingsHelper, thresholdsHolder, resourceHelper);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
        measurementPresenter.registerListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        measurementPresenter.unregisterListener(this);
    }

    @Override
    protected void addContextSpecificButtons() {
        addButton(R.layout.context_button_dashboard);
    }

  private void refresh() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        zoomIn.setEnabled(measurementPresenter.canZoomIn());
        zoomOut.setEnabled(measurementPresenter.canZoomOut());

        ArrayList<Measurement> measurements = newArrayList(measurementPresenter.getTimelineView());
        ArrayList<Note> notes = newArrayList(sessionManager.getNotes());

        plot.update(sensorManager.getVisibleSensor(), measurements, notes);

        scrollLeft.setVisibility(measurementPresenter.canScrollLeft() ? View.VISIBLE : View.GONE);
        scrollRight.setVisibility(measurementPresenter.canScrollRight() ? View.VISIBLE : View.GONE);

        updateLabels(measurements);
      }
    });
  }

  private void updateLabels(ArrayList<Measurement> measurements) {
    if (!measurements.isEmpty()) {
      graphBegin.setText(DateFormat.format("hh:mm:ss", measurements.get(0).getTime()));
      graphEnd.setText(DateFormat.format("hh:mm:ss", getLast(measurements).getTime()));
    }
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

  private void zoomIn() {
    measurementPresenter.zoomIn();
  }

  private void zoomOut() {
    measurementPresenter.zoomOut();
  }

  @Override
  public void onViewUpdated() {
    refresh();
  }

  @Override
  public void onAveragedMeasurement(Measurement measurement) {
  }

  @Subscribe
  public void onEvent(TapEvent event) {
    if (!plot.onTap(event.getMotionEvent())) {
      super.onEvent(event);
    }
  }

  @Override
  protected void refreshNotes() {
    refresh();
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
