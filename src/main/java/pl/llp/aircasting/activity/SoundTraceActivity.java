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

import pl.llp.aircasting.R;
import pl.llp.aircasting.event.ui.ViewStreamEvent;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.events.SessionStoppedEvent;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;
import pl.llp.aircasting.view.MapIdleDetector;

import android.os.AsyncTask;
import android.os.Bundle;
import com.google.common.eventbus.Subscribe;

import java.util.List;

import static pl.llp.aircasting.view.MapIdleDetector.detectMapIdle;

public class SoundTraceActivity extends AirCastingMapActivity implements MapIdleDetector.MapIdleListener {
    private static final long RECENTER_TIMEOUT = 10000;
    public static final int REFRESH_OVERLAY_TIMEOUT = 300;

    private AsyncTask<Void, Void, Void> refreshTask;
    private MapIdleDetector refreshDetector;
    private MapIdleDetector centerDetector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trace);

        mapView.getOverlays().add(routeOverlay);
        mapView.getOverlays().add(traceOverlay);
    }

  @Override
  protected void onResume() {
    super.onResume();

    if(getIntent().getBooleanExtra("startingAircasting", false))
    {
      List<Sensor> currentSensors = sensorManager.getSensors();
      Sensor visibleSensor = sensorManager.getVisibleSensor();
      if (!currentSensors.contains(visibleSensor))
      {
        eventBus.post(new ViewStreamEvent(SimpleAudioReader.getSensor()));
        updateGauges();
      }
    }
    eventBus.post(new SessionStoppedEvent(sessionManager.getSession()));
    refreshDetector = detectMapIdle(mapView, REFRESH_OVERLAY_TIMEOUT, this);

    if (!sessionManager.isSessionSaved()) {
      MapCenterer centerer = new MapCenterer();
      centerDetector = detectMapIdle(mapView, RECENTER_TIMEOUT, centerer);
    }
  }

    @Override
    protected void onPause() {
        super.onPause();

        if (centerDetector != null) centerDetector.stop();
        refreshDetector.stop();
    }

    @Subscribe
    @Override
    public void onEvent(ViewStreamEvent event) {
        super.onEvent(event);
        refreshSoundTrace();
    }

    @Override
    public void onMapIdle() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshSoundTrace();
            }
        });
    }

    private void refreshSoundTrace() {
        if (refreshTask != null && refreshTask.getStatus() != AsyncTask.Status.FINISHED) return;

        //noinspection unchecked
        refreshTask = new UpdateSoundTraceTask().execute();
    }

    class UpdateSoundTraceTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            startSpinner();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            traceOverlay.refresh(mapView);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            stopSpinner();
            mapView.invalidate();
        }
    }

    private class MapCenterer implements MapIdleDetector.MapIdleListener {
        @Override
        public void onMapIdle() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    centerMap();
                }
            });
        }
    }
}
