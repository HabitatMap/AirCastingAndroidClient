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

import com.google.android.libraries.maps.CameraUpdateFactory;
import com.google.android.libraries.maps.GoogleMap;
import com.google.android.libraries.maps.OnMapReadyCallback;
import com.google.android.libraries.maps.Projection;
import com.google.android.libraries.maps.SupportMapFragment;
import com.google.android.libraries.maps.model.LatLng;
import com.google.android.libraries.maps.model.MapStyleOptions;
import com.google.android.libraries.maps.model.MarkerOptions;
import com.google.android.libraries.maps.model.PolygonOptions;
import com.google.android.libraries.maps.model.VisibleRegion;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.OverlayItem;
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
public class AirCastingMapActivity extends AirCastingActivity implements
        MeasurementPresenter.Listener,
        LocationHelper.LocationSettingsListener,
        OnMapReadyCallback {

    private static final String HEAT_MAP_VISIBLE = "heat_map_visible";

    @InjectView(R.id.spinner) ImageView spinner;
    @InjectView(R.id.locate) Button centerMap;
    @InjectResource(R.anim.spinner) Animation spinnerAnimation;
    @Inject AveragesDriver averagesDriver;
    @Inject ConnectivityManager connectivityManager;
    @Inject VisibleSession visibleSession;

    public static final int HEAT_MAP_UPDATE_TIMEOUT = 500;
    public static final int SOUND_TRACE_UPDATE_TIMEOUT = 300;

    private static final int ACTION_TOGGLE = 1;
    private static final int ACTION_CENTER = 2;
    private static final int DEFAULT_ZOOM = 16;

    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private boolean soundTraceComplete = true;
    private boolean heatMapVisible = false;
    private int requestsOutstanding = 0;
    private AsyncTask<Void, Void, Void> refreshTask;
    private MapIdleDetector heatMapDetector;
    private MapIdleDetector soundTraceDetector;
    private HeatMapUpdater heatMapUpdater;
    private SoundTraceUpdater soundTraceUpdater;
    private boolean initialized = false;
    private Measurement lastMeasurement;
    private boolean zoomToSession = true;
    private MapIdleDetector routeRefreshDetector;
    private int mRequestedAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.heat_map);

        initToolbar("Map");
        initNavigationDrawer();
        centerMap.setOnClickListener(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapview);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        initializeMap();

        heatMapUpdater = new HeatMapUpdater();
        heatMapDetector = MapIdleDetector.detectMapIdle(map, HEAT_MAP_UPDATE_TIMEOUT, heatMapUpdater);

        soundTraceUpdater = new SoundTraceUpdater();
        soundTraceDetector = MapIdleDetector.detectMapIdle(map, SOUND_TRACE_UPDATE_TIMEOUT, soundTraceUpdater);

        startDetectors();
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
            menuItem.setIcon(R.drawable.toolbar_crowd_map_icon_inactive);
        } else {
            heatMapVisible = true;
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

    private void zoom(int step) {
        float zoom = map.getCameraPosition().zoom + step;
        LatLng position = map.getCameraPosition().target;
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoom));
    }

    private void zoomIn() {
        zoom(1);
    }

    private void zoomOut() {
        zoom(-1);
    }

    private void locate() {
        mRequestedAction = ACTION_CENTER;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationHelper.checkLocationSettings(this);
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
            case R.id.locate:
                locate();
                break;
            default:
                super.onClick(view);
        }
    }

    private void startDetectors() {
        if (routeRefreshDetector != null) routeRefreshDetector.start();
        if (heatMapDetector != null) heatMapDetector.start();
        if (soundTraceDetector != null) soundTraceDetector.start();
    }

    private void stopDetectors() {
        routeRefreshDetector.stop();
        heatMapDetector.stop();
        soundTraceDetector.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshNotes();
        spinnerAnimation.start();
        measurementPresenter.registerListener(this);
        initializeRouteOverlay();

        checkConnection();

        startDetectors();
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopDetectors();
    }

    @Override
    public void onStop() {
        super.onStop();
        measurementPresenter.unregisterListener(this);
    }

    private void initializeRouteOverlay() {
        if (shouldShowRoute()) {
            Sensor sensor = visibleSession.getSensor();
            List<Measurement> measurements = visibleSession.getMeasurements(sensor);

            for (Measurement measurement : measurements) {
//                GeoPoint geoPoint = geoPoint(measurement);
//                routeOverlay.addPoint(geoPoint);
            }
        }
    }

    private boolean shouldShowRoute() {
        return settingsHelper.isShowRoute() &&
                (visibleSession.isVisibleSessionRecording() || visibleSession.isVisibleSessionViewed());
    }

    private void initializeMap() {
        if (settingsHelper.isSatelliteView()) {
            map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }

        LatLng position = new LatLng(visibleSession.getSession().getLatitude(), visibleSession.getSession().getLongitude());
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, DEFAULT_ZOOM));
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

    @Override
    protected void refreshNotes() {
//        noteOverlay.clear();
        for (Note note : visibleSession.getSessionNotes()) {
//            noteOverlay.add(note);
        }
    }

    @Subscribe
    @Override
    public void onEvent(VisibleStreamUpdatedEvent event) {
        super.onEvent(event);

        heatMapUpdater.onMapIdle();
        soundTraceUpdater.onMapIdle();
    }

    @Override
    @Subscribe
    public void onEvent(VisibleSessionUpdatedEvent event) {
        super.onEvent(event);
        refreshNotes();
    }

    @Subscribe
    public void onEvent(NoteCreatedEvent event) {
        refreshNotes();
    }

    @Subscribe
    public void onEvent(DoubleTapEvent event) {
        zoomIn();
    }


    @Subscribe
    public void onEvent(LocationEvent event) {
        updateRoute();
    }

    private void updateRoute() {
        if (settingsHelper.isShowRoute() && visibleSession.isVisibleSessionRecording()) {
//            GeoPoint geoPoint = geoPoint(locationHelper.getLastLocation());
//            routeOverlay.addPoint(geoPoint);
        }
    }

    protected void centerMap() {
        if (locationHelper.getLastLocation() != null) {
            Location location = locationHelper.getLastLocation();
            LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, DEFAULT_ZOOM));
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
//                traceOverlay.update(measurement);
            } else if (lastMeasurement != null) {
//                traceOverlay.update(lastMeasurement);
            }
        }

        if (settingsHelper.isAveraging()) {
            lastMeasurement = measurement;
        }
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
    }

    private void refreshSoundTrace() {
        soundTraceComplete = false;
        refresh();

        soundTraceComplete = true;
        refresh();
    }

    class HeatMapDownloader extends AsyncTask<Void, Void, HttpResult<Iterable<Region>>> {
        LatLng northWest;
        LatLng southEast;
        int gridSizeX;
        int gridSizeY;

        public HeatMapDownloader(LatLng northWest, LatLng southEast, int gridSizeX, int gridSizeY) {
            this.northWest = northWest;
            this.southEast = southEast;
            this.gridSizeX = gridSizeX;
            this.gridSizeY = gridSizeY;
        }

        @Override
        protected void onPreExecute() {
            requestsOutstanding += 1;
            refresh();
        }

        @Override
        protected HttpResult<Iterable<Region>> doInBackground(Void... voids) {
            return averagesDriver.index(visibleSession.getSensor(), northWest.longitude, northWest.latitude,
                    southEast.longitude, southEast.latitude, gridSizeX, gridSizeY);
        }

        @Override
        protected void onPostExecute(HttpResult<Iterable<Region>> regions) {
            requestsOutstanding -= 1;

            if (regions.getContent() != null) {
                for (Region region : regions.getContent()) {
                    map.addPolygon(new PolygonOptions().add(
                            new LatLng(region.getNorth(), region.getWest()),
                            new LatLng(region.getNorth(), region.getEast()),
                            new LatLng(region.getSouth(), region.getWest()),
                            new LatLng(region.getSouth(), region.getEast())
                    ));
                }
//                heatMapOverlay.setRegions(regions.getContent());
            }

            refresh();
        }
    }

    private class SoundTraceUpdater implements MapIdleDetector.MapIdleListener {
        @Override
        public void onMapIdle() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshSoundTrace();
                }
            });
        }
    }

    private class HeatMapUpdater implements MapIdleDetector.MapIdleListener {
        public static final int MAP_BUFFER_SIZE = 3;

        @Override
        public void onMapIdle() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Projection projection = map.getProjection();
                    VisibleRegion visibleRegion = projection.getVisibleRegion();

                    // We want to download data that's off screen so the user can see something while panning
                    LatLng northWest = visibleRegion.farLeft;
                    LatLng southEast = visibleRegion.nearRight;

                    View mapView = mapFragment.getView();
                    int size = min(mapView.getWidth(), mapView.getHeight()) / settingsHelper.getHeatMapDensity();
                    if (size < 1) size = 1;

                    int gridSizeX = MAP_BUFFER_SIZE * mapView.getWidth() / size;
                    int gridSizeY = MAP_BUFFER_SIZE * mapView.getHeight() / size;

                    //noinspection unchecked
                    new HeatMapDownloader(northWest, southEast, gridSizeX, gridSizeY).execute();
                }
            });
        }
    }
}
