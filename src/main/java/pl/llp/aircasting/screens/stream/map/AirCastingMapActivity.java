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
package pl.llp.aircasting.screens.stream.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import java.util.List;

import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.event.sensor.LocationEvent;
import pl.llp.aircasting.event.session.NoteCreatedEvent;
import pl.llp.aircasting.event.session.VisibleSessionUpdatedEvent;
import pl.llp.aircasting.event.ui.DoubleTapEvent;
import pl.llp.aircasting.event.ui.VisibleStreamUpdatedEvent;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.internal.Region;
import pl.llp.aircasting.networking.drivers.AveragesDriver;
import pl.llp.aircasting.networking.httpUtils.HttpResult;
import pl.llp.aircasting.screens.common.ToastHelper;
import pl.llp.aircasting.screens.common.helpers.LocationHelper;
import pl.llp.aircasting.screens.common.sessionState.VisibleSession;
import pl.llp.aircasting.screens.stream.MeasurementPresenter;
import pl.llp.aircasting.screens.stream.base.AirCastingActivity;
import pl.llp.aircasting.screens.stream.base.AirCastingActivityForMap;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

import static java.lang.Math.min;
import static pl.llp.aircasting.screens.common.helpers.LocationHelper.REQUEST_CHECK_SETTINGS;
import static pl.llp.aircasting.screens.stream.map.LocationConversionHelper.boundingBox;
import static pl.llp.aircasting.screens.stream.map.LocationConversionHelper.geoPoint;
import static pl.llp.aircasting.screens.stream.map.MapIdleDetector.detectMapIdle;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/17/11
 * Time: 5:04 PM
 */
public class AirCastingMapActivity extends AirCastingActivityForMap implements MapIdleDetector.MapIdleListener, MeasurementPresenter.Listener, LocationHelper.LocationSettingsListener {

    private static final String HEAT_MAP_VISIBLE = "heat_map_visible";

    @InjectView(R.id.mapview)
    AirCastingMapView mapView;
    @InjectView(R.id.spinner) ImageView spinner;
    @InjectView(R.id.locate) Button centerMap;
    @InjectResource(R.anim.spinner) Animation spinnerAnimation;
    @Inject HeatMapOverlay heatMapOverlay;
    @Inject AveragesDriver averagesDriver;
    @Inject ConnectivityManager connectivityManager;
    @Inject NoteOverlay noteOverlay;
    @Inject LocationOverlay locationOverlay;
    @Inject TraceOverlay traceOverlay;
    @Inject RouteOverlay routeOverlay;
    @Inject VisibleSession visibleSession;

    public static final int HEAT_MAP_UPDATE_TIMEOUT = 500;
    public static final int SOUND_TRACE_UPDATE_TIMEOUT = 300;

    private static final int ACTION_TOGGLE = 1;
    private static final int ACTION_CENTER = 2;

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
    private int mRequestedAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        noteOverlay.setContext(this);

        setContentView(R.layout.heat_map);

        initToolbar("Map");
        initNavigationDrawer();
        centerMap.setOnClickListener(this);

        mapView.getOverlays().add(routeOverlay);
        mapView.getOverlays().add(traceOverlay);

        if (!visibleSession.isVisibleSessionViewed()) {
            mapView.getOverlays().add(locationOverlay);
        }
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        getDelegate().invalidateOptionsMenu();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(HEAT_MAP_VISIBLE, heatMapVisible);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        zoomToSession = false;
        heatMapVisible = savedInstanceState.getBoolean(HEAT_MAP_VISIBLE);
    }

    private void toggleHeatMapVisibility(MenuItem menuItem) {
        if (heatMapVisible) {
            heatMapVisible = false;
            mapView.getOverlays().remove(heatMapOverlay);
            mapView.invalidate();
            menuItem.setIcon(R.drawable.toolbar_crowd_map_icon_inactive);
        } else {
            heatMapVisible = true;
            mapView.getOverlays().add(0, heatMapOverlay);
            mapView.invalidate();
            menuItem.setIcon(R.drawable.toolbar_crowd_map_icon_active);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuInflater inflater = getDelegate().getMenuInflater();
        inflater.inflate(R.menu.toolbar_crowd_map_toggle, menu);

        if (heatMapVisible) {
            menu.getItem(menu.size() - 1).setIcon(R.drawable.toolbar_crowd_map_icon_active);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        super.onOptionsItemSelected(menuItem);

        switch (menuItem.getItemId()) {
            case R.id.toggle_aircasting:
                mRequestedAction = ACTION_TOGGLE;

                if (!settingsHelper.areMapsDisabled()) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        locationHelper.checkLocationSettings(this);
                    }
                } else {
                    toggleSessionRecording();
                }

                break;
            case R.id.make_note:
                Intents.makeANote(this);
                break;
            case R.id.toggle_heat_map_button:
                toggleHeatMapVisibility(menuItem);
                break;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.zoom_in:
                mapView.getController().zoomIn();
                break;
            case R.id.zoom_out:
                mapView.getController().zoomOut();
                break;
            case R.id.locate:
                mRequestedAction = ACTION_CENTER;

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationHelper.checkLocationSettings(this);
                }
                break;
            default:
                super.onClick(view);
        }
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
        traceOverlay.refresh(mapView);

        checkConnection();

        updater = new HeatMapUpdater();
        heatMapDetector = detectMapIdle(mapView, HEAT_MAP_UPDATE_TIMEOUT, updater);
        soundTraceDetector = detectMapIdle(mapView, SOUND_TRACE_UPDATE_TIMEOUT, this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        routeRefreshDetector.stop();
        traceOverlay.stopDrawing(mapView);
        heatMapDetector.stop();
        soundTraceDetector.stop();
    }

    @Override
    public void onStop() {
        super.onStop();
        measurementPresenter.unregisterListener(this);
    }

    private void initializeRouteOverlay() {
        routeOverlay.clear();

        if (shouldShowRoute()) {
            Sensor sensor = visibleSession.getSensor();
            List<Measurement> measurements = visibleSession.getMeasurements(sensor);

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
                (visibleSession.isVisibleSessionRecording() || visibleSession.isVisibleSessionViewed());
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

    @Override
    public void onLocationSettingsSatisfied() {
        if (mRequestedAction == ACTION_TOGGLE) {
            toggleSessionRecording();
        } else if (mRequestedAction == ACTION_CENTER) {
            centerMap();
        }
    }

    private void toggleSessionRecording() {
        toggleAirCasting();

        measurementPresenter.reset();
        traceOverlay.refresh(mapView);
        routeOverlay.clear();
        routeOverlay.invalidate();
        mapView.invalidate();
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
        if (visibleSession.isVisibleSessionViewed() && zoomToSession) {
            LocationConversionHelper.BoundingBox boundingBox = boundingBox(visibleSession.getSession());

            mapView.getController().zoomToSpan(boundingBox.getLatSpan(), boundingBox.getLonSpan());
            mapView.getController().animateTo(boundingBox.getCenter());
        }
    }

    @Override
    protected void refreshNotes() {
        noteOverlay.clear();
        for (Note note : visibleSession.getSessionNotes()) {
            noteOverlay.add(note);
        }
    }

    public void noteClicked(OverlayItem item, int index) {
//        suppressNextTap();

        mapView.getController().animateTo(item.getPoint());

        noteClicked(index);
    }


    @Subscribe
    @Override
    public void onEvent(VisibleStreamUpdatedEvent event) {
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
    public void onEvent(VisibleSessionUpdatedEvent event) {
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
        if (settingsHelper.isShowRoute() && visibleSession.isVisibleSessionRecording()) {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                if (resultCode != RESULT_OK) {
                    ToastHelper.show(this, R.string.enable_location, Toast.LENGTH_LONG);
                } else {
                    locationHelper.startLocationUpdates();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                    }
                    onLocationSettingsSatisfied();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onViewUpdated() {
    }

    @Override
    public void onAveragedMeasurement(Measurement measurement) {
        if (currentSessionManager.isSessionRecording()) {
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
            ToastHelper.show(this, R.string.no_internet, Toast.LENGTH_SHORT);
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

            return averagesDriver.index(visibleSession.getSensor(), northWestLoc.getLongitude(), northWestLoc.getLatitude(),
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
