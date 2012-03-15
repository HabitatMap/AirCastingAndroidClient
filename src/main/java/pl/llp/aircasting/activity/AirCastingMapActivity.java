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

import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.OverlayItem;
import com.google.inject.Inject;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.event.DoubleTapEvent;
import pl.llp.aircasting.event.LocationEvent;
import pl.llp.aircasting.event.TapEvent;
import pl.llp.aircasting.helper.LocationConversionHelper;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.SoundMeasurement;
import pl.llp.aircasting.view.AirCastingMapView;
import pl.llp.aircasting.view.MapIdleDetector;
import pl.llp.aircasting.view.overlay.LocationOverlay;
import pl.llp.aircasting.view.overlay.NoteOverlay;
import pl.llp.aircasting.view.overlay.RouteOverlay;
import pl.llp.aircasting.view.overlay.SoundTraceOverlay;
import pl.llp.aircasting.view.presenter.MeasurementPresenter;
import roboguice.event.Observes;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

import java.io.File;

import static pl.llp.aircasting.helper.LocationConversionHelper.boundingBox;
import static pl.llp.aircasting.helper.LocationConversionHelper.geoPoint;
import static pl.llp.aircasting.view.MapIdleDetector.detectMapIdle;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/20/11
 * Time: 3:35 PM
 */
public class AirCastingMapActivity extends AirCastingActivity implements MeasurementPresenter.Listener {
    @InjectView(R.id.mapview) AirCastingMapView mapView;

    @InjectView(R.id.spinner) ImageView spinner;
    @InjectResource(R.anim.spinner) Animation spinnerAnimation;

    @Inject NoteOverlay noteOverlay;
    @Inject LocationOverlay locationOverlay;
    @Inject SoundTraceOverlay soundTraceOverlay;
    @Inject MeasurementPresenter measurementPresenter;

    @Inject RouteOverlay routeOverlay;

    boolean initialized = false;
    SoundMeasurement lastMeasurement;
    private boolean zoomToSession = true;
    private boolean suppressTap;
    MapIdleDetector routeRefreshDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        noteOverlay.setContext(this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        zoomToSession = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        initialize();

        refreshNotes();

        spinnerAnimation.start();

        locationHelper.setContext(this);

        initializeMap();

        initializeTraceOverlay();

        measurementPresenter.registerListener(this);

        initializeRouteOverlay();
    }

    private void initializeRouteOverlay() {
        routeOverlay.clear();

        if (shouldShowRoute()) {
            for (SoundMeasurement measurement : sessionManager.getSoundMeasurements()) {
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

    @Override
    protected void onPause() {
        super.onPause();

        measurementPresenter.unregisterListener(this);
        routeRefreshDetector.stop();
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

    private void initializeTraceOverlay() {
        soundTraceOverlay.setSaved(sessionManager.isSessionSaved());
        soundTraceOverlay.setCalibration(sessionManager.getSession().getCalibration());
        soundTraceOverlay.setOffset60DB(sessionManager.getSession().getOffset60DB());
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

            addLocationControls();

            mapView.getOverlays().add(noteOverlay);

            initialized = true;
        }
    }

    private void addLocationControls() {
        if (!sessionManager.isSessionSaved()) {
            mapView.getOverlays().add(locationOverlay);

            setButton(contextButtonCenter, R.layout.context_button_locate);
        }
    }

    private void showSession() {
        if (sessionManager.isSessionSaved() && zoomToSession) {
            LocationConversionHelper.BoundingBox boundingBox = boundingBox(sessionManager.getSoundMeasurements());

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

    @Override
    public void onNewMeasurement(SoundMeasurement measurement) {
        super.onNewMeasurement(measurement);

        if (!sessionManager.isSessionSaved()) {
            updateLocation();
        }

        mapView.invalidate();
    }

    public void noteClicked(OverlayItem item, int index) {
        suppressTap = true;

        mapView.getController().animateTo(item.getPoint());

        noteClicked(index);
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
                centerMap();
                break;
            case R.id.view_photo:
                Intents.viewPhoto(this, photoUri());
                break;
            default:
                super.onClick(view);
        }
    }

    private Uri photoUri() {
        if (photoHelper.photoExistsLocally(currentNote)) {
            File file = new File(currentNote.getPhotoPath());
            return Uri.fromFile(file);
        } else {
            return Uri.parse(currentNote.getPhotoPath());
        }
    }

    @Override
    public void onNewSession() {
        super.onNewSession();
        refreshNotes();
        mapView.invalidate();
    }

    @Override
    public void onNewNote(Note note) {
        super.onNewNote(note);
        refreshNotes();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(@Observes DoubleTapEvent event) {
        mapView.getController().zoomIn();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(@Observes MotionEvent event) {
        mapView.dispatchTouchEvent(event);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEvent(@Observes LocationEvent event) {
        updateLocation();
        updateRoute();

        mapView.invalidate();
    }

    private void updateRoute() {
        if (settingsHelper.isShowRoute() && sessionManager.isRecording()) {
            GeoPoint geoPoint = geoPoint(locationHelper.getLastLocation());
            routeOverlay.addPoint(geoPoint);
        }
    }

    @Override
    public void onEvent(@Observes TapEvent event) {
        if (suppressTap) {
            suppressTap = false;
        } else {
            super.onEvent(event);
        }
    }

    private void updateLocation() {
        Location location = locationHelper.getLastLocation();
        if (!sessionManager.isSessionSaved()) {
            if (!settingsHelper.isAveraging()) {
                locationOverlay.setValue(sessionManager.getDbNow());
            }

            locationOverlay.setLocation(location);
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
    public void onAveragedMeasurement(SoundMeasurement measurement) {
        if (sessionManager.isSessionStarted()) {
            if (!settingsHelper.isAveraging()) {
                soundTraceOverlay.update(measurement);
            } else if (lastMeasurement != null) {
                soundTraceOverlay.update(lastMeasurement);
            }
        }

        if (settingsHelper.isAveraging()) {
            locationOverlay.setValue(measurement.getValue());
            lastMeasurement = measurement;
        }

        mapView.invalidate();
    }
}
