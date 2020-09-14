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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import com.google.android.libraries.maps.model.BitmapDescriptor;
import com.google.android.libraries.maps.model.BitmapDescriptorFactory;
import com.google.android.libraries.maps.model.LatLng;
import com.google.android.libraries.maps.model.LatLngBounds;
import com.google.android.libraries.maps.model.Marker;
import com.google.android.libraries.maps.model.MarkerOptions;
import com.google.android.libraries.maps.model.Polyline;
import com.google.android.libraries.maps.model.PolylineOptions;
import com.google.android.libraries.maps.model.RoundCap;
import com.google.android.libraries.maps.model.StyleSpan;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import pl.llp.aircasting.R;
import pl.llp.aircasting.event.sensor.HeatLegendUnitsChangedEvent;
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

    private static final int ACTION_TOGGLE = 1;
    private static final int ACTION_CENTER = 2;
    private static final int DEFAULT_ZOOM = 16;

    private GoogleMap map;
    private Marker lastMeasurementMarker = null;
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
        LatLng latestPoint = null;
        Integer latestColor = null;

        if (measurements.size() == 0) { return; }

        for (Measurement measurement : measurements) {
            latestColor = resourceHelper.getMeasurementColor(this, sensor, measurement.getValue());
            options.addSpan(new StyleSpan(latestColor));
            latestPoint = new LatLng(measurement.getLatitude(), measurement.getLongitude());
            measurementPoints.add(latestPoint);
        }

        measurementsLine = map.addPolyline(options.addAll(measurementPoints));
        drawLastMeasurementMarker(latestPoint, latestColor);
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
        if (zoomToSession) {
            LatLngBounds boundingBox = LocationConversionHelper.boundingBox(visibleSession.getSession());
            int padding = 100; // meters
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(boundingBox, padding));
        }
    }

    // on sensor changed
    @Subscribe
    @Override
    public void onEvent(VisibleStreamUpdatedEvent event) {
        super.onEvent(event);
        System.out.println("ANIA sensor changed");

        // TODO: refresh drawing
    }

    // on HLU changed
    @Subscribe
    public void onEvent(HeatLegendUnitsChangedEvent event) {
        System.out.println("ANIA HLU changed");
        // TODO: refresh drawing
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
        Sensor sensor = visibleSession.getSensor();
        final int color = resourceHelper.getMeasurementColor(this, sensor, measurement.getValue());

        if (currentSessionManager.isSessionRecording()) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LatLng point = new LatLng(measurement.getLatitude(), measurement.getLongitude());
                    measurementPoints.add(point);
                    measurementsLine.setPoints(measurementPoints);
                    drawLastMeasurementMarker(point, color);
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

    private void drawLastMeasurementMarker(LatLng point, Integer color) {
        if (map == null) return;

        if (lastMeasurementMarker != null) lastMeasurementMarker.remove();
        lastMeasurementMarker = map.addMarker(new MarkerOptions()
                .position(point)
                .icon(circleMarkerIcon(color)));
    }

    private BitmapDescriptor circleMarkerIcon(Integer color) {
        int CIRCLE_WIDTH = 30;
        int STROKE_WIDTH = 4;

        Bitmap bitmap = Bitmap.createBitmap(
                CIRCLE_WIDTH + 2 * STROKE_WIDTH,
                CIRCLE_WIDTH + 2 * STROKE_WIDTH,
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT);

        Paint strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setColor(Color.WHITE);
        strokePaint.setAntiAlias(true);
        strokePaint.setStrokeWidth(STROKE_WIDTH);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setAntiAlias(true);

        int radius = CIRCLE_WIDTH / 2;

        int padding = 0;

        // drawing stroke
        canvas.drawCircle(
                radius + STROKE_WIDTH,
                radius + STROKE_WIDTH,
                radius - padding,
                strokePaint
        );

        // drawing circle filled with proper color
        canvas.drawCircle(
                radius + STROKE_WIDTH,
                radius + STROKE_WIDTH,
                radius - padding,
                paint
        );

     return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}
