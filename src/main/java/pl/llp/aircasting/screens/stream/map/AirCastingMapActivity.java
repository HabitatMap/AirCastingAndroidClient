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
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.libraries.maps.CameraUpdateFactory;
import com.google.android.libraries.maps.GoogleMap;
import com.google.android.libraries.maps.OnMapReadyCallback;
import com.google.android.libraries.maps.SupportMapFragment;
import com.google.android.libraries.maps.model.LatLng;
import com.google.android.libraries.maps.model.LatLngBounds;
import com.google.android.libraries.maps.model.Polyline;
import com.google.android.libraries.maps.model.PolylineOptions;
import com.google.android.libraries.maps.model.RoundCap;
import com.google.android.libraries.maps.model.StyleSpan;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import pl.llp.aircasting.R;
import pl.llp.aircasting.event.session.VisibleSessionUpdatedEvent;
import pl.llp.aircasting.event.ui.DoubleTapEvent;
import pl.llp.aircasting.event.ui.VisibleStreamUpdatedEvent;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.screens.common.ToastHelper;
import pl.llp.aircasting.screens.common.helpers.LocationHelper;
import pl.llp.aircasting.screens.common.helpers.ResourceHelper;
import pl.llp.aircasting.screens.common.sessionState.VisibleSession;
import pl.llp.aircasting.screens.stream.MeasurementPresenter;
import pl.llp.aircasting.screens.stream.base.AirCastingActivity;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

import static pl.llp.aircasting.screens.common.helpers.LocationHelper.REQUEST_CHECK_SETTINGS;

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

    @InjectView(R.id.spinner) ImageView spinner;
    @InjectView(R.id.locate) Button centerMap;
    @InjectResource(R.anim.spinner) Animation spinnerAnimation;
    @Inject ConnectivityManager connectivityManager;
    @Inject VisibleSession visibleSession;
    @Inject ResourceHelper resourceHelper;

    public static final int SOUND_TRACE_UPDATE_TIMEOUT = 300;

    private static final int ACTION_TOGGLE = 1;
    private static final int ACTION_CENTER = 2;
    private static final int DEFAULT_ZOOM = 16;

    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private int requestsOutstanding = 0;
    private AsyncTask<Void, Void, Void> refreshTask;
    private boolean initialized = false;
    private boolean zoomToSession = true;
    private int mRequestedAction;

    private PolylineOptions options = new PolylineOptions()
            .width(20f)
            .startCap(new RoundCap());
    private Polyline measurementsLine;
    private ArrayList<LatLng> measurementPoints = new ArrayList<>();

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
        drawSession();
        animateCameraToSession();
    }

    private void drawSession() {
        Sensor sensor = visibleSession.getSensor();
        List<Measurement> measurements = visibleSession.getMeasurements(sensor);

        if (measurements.size() == 0) { return; }

        for (Measurement measurement : measurements) {
            int color = resourceHelper.getMeasurementColor(this, sensor, measurement.getValue());
            options.addSpan(new StyleSpan(color));
            measurementPoints.add(new LatLng(measurement.getLatitude(), measurement.getLongitude()));
        }

        measurementsLine = map.addPolyline(options.addAll(measurementPoints));
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        getDelegate().invalidateOptionsMenu();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        zoomToSession = false;
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

    @Override
    protected void onResume() {
        super.onResume();

        spinnerAnimation.start();
        measurementPresenter.registerListener(this);

        checkConnection();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // TODO: should we do sothing here?
    }

    @Override
    public void onStop() {
        super.onStop();
        measurementPresenter.unregisterListener(this);
    }

    private void initializeMap() {
        if (settingsHelper.isSatelliteView()) {
            map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
    }

    private void animateCameraToSession() {
        if (visibleSession.isVisibleSessionViewed() && zoomToSession) {
            LatLngBounds boundingBox = LocationConversionHelper.boundingBox(visibleSession.getSession());
            int padding = 100; // meters
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(boundingBox, padding));
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

    // on sensor changed
    @Subscribe
    @Override
    public void onEvent(VisibleStreamUpdatedEvent event) {
        super.onEvent(event);

        // TODO: should we do something here?
    }

    // visible session changed
    @Override
    @Subscribe
    public void onEvent(VisibleSessionUpdatedEvent event) {
        super.onEvent(event);

        // TODO: should we do something here?
    }

    @Subscribe
    public void onEvent(DoubleTapEvent event) {
        zoomIn();
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
    public void onAveragedMeasurement(final Measurement measurement) {
        if (currentSessionManager.isSessionRecording()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    measurementPoints.add(new LatLng(measurement.getLatitude(), measurement.getLongitude()));
                    measurementsLine.setPoints(measurementPoints);
                }
            });
        }
    }

    private void checkConnection() {
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo == null || !activeNetworkInfo.isConnectedOrConnecting()) {
            ToastHelper.show(this, R.string.no_internet, Toast.LENGTH_SHORT);
        }
    }

    private void refresh() {
        boolean complete = (requestsOutstanding == 0);
        if (complete) {
            stopSpinner();
        } else {
            startSpinner();
        }
    }
}
