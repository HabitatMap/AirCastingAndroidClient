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

import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import com.google.android.maps.MapController;
import com.google.android.maps.OverlayItem;
import com.google.common.eventbus.Subscribe;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.events.SessionChangeEvent;
import pl.llp.aircasting.api.AveragesDriver;
import pl.llp.aircasting.event.sensor.LocationEvent;
import pl.llp.aircasting.event.session.NoteCreatedEvent;
import pl.llp.aircasting.event.ui.DoubleTapEvent;
import pl.llp.aircasting.event.ui.StreamUpdateEvent;
import pl.llp.aircasting.helper.LocationConversionHelper;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.internal.Region;
import pl.llp.aircasting.util.http.HttpResult;
import pl.llp.aircasting.view.AirCastingMapView;
import pl.llp.aircasting.view.MapIdleDetector;
import pl.llp.aircasting.view.overlay.*;

import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;
import com.google.inject.Inject;
import pl.llp.aircasting.view.presenter.MeasurementPresenter;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

import java.util.List;

import static java.lang.Math.min;
import static pl.llp.aircasting.helper.LocationConversionHelper.boundingBox;
import static pl.llp.aircasting.helper.LocationConversionHelper.geoPoint;
import static pl.llp.aircasting.view.MapIdleDetector.detectMapIdle;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/17/11
 * Time: 5:04 PM
 */
public class AirCastingMapActivity extends AirCastingActivity implements MapIdleDetector.MapIdleListener, MeasurementPresenter.Listener {

    @InjectView(R.id.mapview) AirCastingMapView mapView;
    @InjectView(R.id.spinner) ImageView spinner;
    @InjectResource(R.anim.spinner) Animation spinnerAnimation;
    @Inject HeatMapOverlay heatMapOverlay;
    @Inject AveragesDriver averagesDriver;
    @Inject ConnectivityManager connectivityManager;
    @Inject NoteOverlay noteOverlay;
    @Inject LocationOverlay locationOverlay;
    @Inject TraceOverlay traceOverlay;
    @Inject MeasurementPresenter measurementPresenter;
    @Inject RouteOverlay routeOverlay;

    public static final int HEAT_MAP_UPDATE_TIMEOUT = 500;
    public static final int SOUND_TRACE_UPDATE_TIMEOUT = 300;

    private boolean soundTraceComplete = true;
    private boolean heatMapVisible = false;
    private int requestsOutstanding = 0;
    private AsyncTask<Void, Void, Void> refreshTask;
    private MapIdleDetector heatMapDetector;
    private MapIdleDetector soundTraceDetector;
    private HeatMapUpdater updater;
    private boolean initialized = false;
    private Measurement lastMeasurement;
    private boolean zoomToSession = true;
    private MapIdleDetector routeRefreshDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        noteOverlay.setContext(this);

        setContentView(R.layout.heat_map);

        initToolbar("Heat map");
        initNavigationDrawer();

        mapView.getOverlays().add(routeOverlay);
        mapView.getOverlays().add(traceOverlay);

        if (!sessionManager.isSessionSaved()) {
            mapView.getOverlays().add(locationOverlay);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        zoomToSession = false;
    }

    private void toggleHeatMapVisibility() {
        if (heatMapVisible) {
            heatMapVisible = false;
            mapView.getOverlays().remove(heatMapOverlay);
            mapView.invalidate();
        } else {
            heatMapVisible = true;
            mapView.getOverlays().add(heatMapOverlay);
            mapView.invalidate();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toggle_heat_map_button:
                toggleHeatMapVisibility();
                updateButtons();
                break;
            case R.id.zoom_in:
                mapView.getController().zoomIn();
                break;
            case R.id.zoom_out:
                mapView.getController().zoomOut();
                break;
            case R.id.locate:
                centerMap();
                break;
            default:
                super.onClick(view);
        }
    }

    @Override
    protected void addContextSpecificButtons() {
        if (!sessionManager.isSessionSaved()) {
            addButton(R.layout.context_button_locate);
        }

        if (heatMapVisible) {
            addButton(R.layout.context_button_crowdmap_active);
        } else {
            addButton(R.layout.context_button_crowdmap_inactive);
        }

        addButton(R.layout.context_button_dashboard);
    }

    @Override
    protected void onResume() {
        super.onResume();

        initialize();
        refreshNotes();
        spinnerAnimation.start();
        initializeMap();
        measurementPresenter.registerListener(this);
        initializeRouteOverlay();
        traceOverlay.startDrawing();

        checkConnection();

        updater = new HeatMapUpdater();
        heatMapDetector = detectMapIdle(mapView, HEAT_MAP_UPDATE_TIMEOUT, updater);
        soundTraceDetector = detectMapIdle(mapView, SOUND_TRACE_UPDATE_TIMEOUT, this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        measurementPresenter.unregisterListener(this);
        routeRefreshDetector.stop();
        traceOverlay.stopDrawing(mapView);
        heatMapDetector.stop();
        soundTraceDetector.stop();
    }

    private void initializeRouteOverlay() {
        routeOverlay.clear();

        if (shouldShowRoute()) {
            Sensor sensor = sensorManager.getVisibleSensor();
            List<Measurement> measurements = sessionManager.getMeasurements(sensor);

            for (Measurement measurement : measurements) {
                GeoPoint geoPoint = geoPoint(measurement);
                routeOverlay.addPoint(geoPoint);
            }
        }

        routeRefreshDetector = detectMapIdle(mapView, 100, new MapIdleDetector.MapIdleListener() {
            @Override
            public void onMapIdle() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        routeOverlay.invalidate();
                        mapView.invalidate();
                    }
                });
            }
        });
    }

    private boolean shouldShowRoute() {
        return settingsHelper.isShowRoute() &&
                (sessionManager.isRecording() || sessionManager.isSessionSaved());
    }

    private void initializeMap() {
        mapView.setSatellite(settingsHelper.isSatelliteView());

        if (settingsHelper.isFirstLaunch()) {
            mapView.getController().setZoom(16);

            Location location = locationHelper.getLastLocation();
            if (location != null) {
                GeoPoint geoPoint = geoPoint(location);
                mapView.getController().setCenter(geoPoint);
            }

            settingsHelper.setFirstLaunch(false);
        }
    }

    protected void startSpinner() {
        if (spinner.getVisibility() != View.VISIBLE) {
            spinner.setVisibility(View.VISIBLE);
            spinner.setAnimation(spinnerAnimation);
        }
    }

    protected void stopSpinner() {
        spinner.setVisibility(View.INVISIBLE);
        spinner.setAnimation(null);
    }

    private void initialize() {
        if (!initialized) {
            showSession();

            mapView.getOverlays().add(noteOverlay);

            initialized = true;
        }
    }

    private void showSession() {
        if (sessionManager.isSessionSaved() && zoomToSession) {
            LocationConversionHelper.BoundingBox boundingBox = boundingBox(sessionManager.getSession());

            mapView.getController().zoomToSpan(boundingBox.getLatSpan(), boundingBox.getLonSpan());
            mapView.getController().animateTo(boundingBox.getCenter());
        }
    }

    @Override
    protected void refreshNotes() {
        noteOverlay.clear();
        for (Note note : sessionManager.getNotes()) {
            noteOverlay.add(note);
        }
    }

    public void noteClicked(OverlayItem item, int index) {
        suppressNextTap();

        mapView.getController().animateTo(item.getPoint());

        noteClicked(index);
    }


    @Subscribe
    @Override
    public void onEvent(StreamUpdateEvent event) {
        super.onEvent(event);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mapView.invalidate();
            }
        });

        updater.onMapIdle();
        onMapIdle();
    }

    @Override
    @Subscribe
    public void onEvent(SessionChangeEvent event) {
        super.onEvent(event);
        refreshNotes();
        mapView.invalidate();
    }

    @Subscribe
    public void onEvent(NoteCreatedEvent event) {
        refreshNotes();
    }

    @Subscribe
    public void onEvent(DoubleTapEvent event) {
        mapView.getController().zoomIn();
    }

    @Subscribe
    public void onEvent(MotionEvent event) {
        mapView.dispatchTouchEvent(event);
    }

    @Subscribe
    public void onEvent(LocationEvent event) {
        updateRoute();

        mapView.invalidate();
    }

    private void updateRoute() {
        if (settingsHelper.isShowRoute() && sessionManager.isRecording()) {
            GeoPoint geoPoint = geoPoint(locationHelper.getLastLocation());
            routeOverlay.addPoint(geoPoint);
        }
    }

    protected void centerMap() {
        if (locationHelper.getLastLocation() != null) {
            GeoPoint geoPoint = geoPoint(locationHelper.getLastLocation());
            MapController controller = mapView.getController();
            controller.animateTo(geoPoint);
        }
    }

    @Override
    public void onViewUpdated() {
    }

    @Override
    public void onAveragedMeasurement(Measurement measurement) {
        if (sessionManager.isSessionStarted()) {
            if (!settingsHelper.isAveraging()) {
                traceOverlay.update(measurement);
            } else if (lastMeasurement != null) {
                traceOverlay.update(lastMeasurement);
            }
        }

        if (settingsHelper.isAveraging()) {
            lastMeasurement = measurement;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mapView.invalidate();
            }
        });
    }

    private void checkConnection() {
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnectedOrConnecting()) {
            Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    private void refresh() {
        boolean complete = (requestsOutstanding == 0) && soundTraceComplete;
        if (complete) {
            stopSpinner();
        } else {
            startSpinner();
        }
        if (!complete) mapView.invalidate();
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

        soundTraceComplete = false;
        refresh();

        traceOverlay.refresh(mapView);

        soundTraceComplete = true;
        mapView.invalidate();
        refresh();
    }

    class HeatMapDownloader extends AsyncTask<Void, Void, HttpResult<Iterable<Region>>> {
        public static final int MAP_BUFFER_SIZE = 3;

        @Override
        protected void onPreExecute() {
            requestsOutstanding += 1;
            refresh();
        }

        @Override
        protected HttpResult<Iterable<Region>> doInBackground(Void... voids) {
            Projection projection = mapView.getProjection();

            // We want to download data that's off screen so the user can see something while panning
            GeoPoint northWest = projection.fromPixels(-mapView.getWidth(), -mapView.getHeight());
            GeoPoint southEast = projection.fromPixels(2 * mapView.getWidth(), 2 * mapView.getHeight());

            Location northWestLoc = LocationConversionHelper.location(northWest);
            Location southEastLoc = LocationConversionHelper.location(southEast);

            int size = min(mapView.getWidth(), mapView.getHeight()) / settingsHelper.getHeatMapDensity();
            if (size < 1) size = 1;

            int gridSizeX = MAP_BUFFER_SIZE * mapView.getWidth() / size;
            int gridSizeY = MAP_BUFFER_SIZE * mapView.getHeight() / size;

            return averagesDriver.index(sensorManager.getVisibleSensor(), northWestLoc.getLongitude(), northWestLoc.getLatitude(),
                    southEastLoc.getLongitude(), southEastLoc.getLatitude(), gridSizeX, gridSizeY);
        }

        @Override
        protected void onPostExecute(HttpResult<Iterable<Region>> regions) {
            requestsOutstanding -= 1;

            if (regions.getContent() != null) {
                heatMapOverlay.setRegions(regions.getContent());
            }

            mapView.invalidate();
            refresh();
        }
    }

    private class HeatMapUpdater implements MapIdleDetector.MapIdleListener {
        @Override
        public void onMapIdle() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //noinspection unchecked
                    new HeatMapDownloader().execute();
                }
            });
        }
    }
}