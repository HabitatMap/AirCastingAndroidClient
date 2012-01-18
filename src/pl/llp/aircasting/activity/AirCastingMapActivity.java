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
import android.widget.*;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.OverlayItem;
import com.google.inject.Inject;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.task.SimpleProgressTask;
import pl.llp.aircasting.event.DoubleTapEvent;
import pl.llp.aircasting.event.LocationEvent;
import pl.llp.aircasting.helper.LocationConversionHelper;
import pl.llp.aircasting.helper.PhotoHelper;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.SoundMeasurement;
import pl.llp.aircasting.view.AirCastingMapView;
import pl.llp.aircasting.view.overlay.LocationOverlay;
import pl.llp.aircasting.view.overlay.NoteOverlay;
import pl.llp.aircasting.view.overlay.SoundTraceOverlay;
import pl.llp.aircasting.view.presenter.MeasurementPresenter;
import roboguice.event.Observes;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

import java.io.File;
import java.text.NumberFormat;

import static pl.llp.aircasting.Intents.triggerSync;
import static pl.llp.aircasting.helper.LocationConversionHelper.boundingBox;
import static pl.llp.aircasting.helper.LocationConversionHelper.geoPoint;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/20/11
 * Time: 3:35 PM
 */
public class AirCastingMapActivity extends AirCastingActivity implements MeasurementPresenter.Listener {
    @InjectView(R.id.note_viewer) View noteViewer;
    @InjectView(R.id.note_date) TextView noteDate;
    @InjectView(R.id.note_number) TextView noteNumber;
    @InjectView(R.id.note_text) EditText noteText;
    @InjectView(R.id.note_left) ImageButton noteLeft;
    @InjectView(R.id.note_right) ImageButton noteRight;
    @InjectView(R.id.note_save) Button noteSave;
    @InjectView(R.id.note_delete) Button noteDelete;
    @InjectView(R.id.view_photo) View viewPhoto;

    @InjectView(R.id.mapview) AirCastingMapView mapView;

    @InjectView(R.id.spinner) ImageView spinner;
    @InjectResource(R.anim.spinner) Animation spinnerAnimation;

    @Inject NoteOverlay noteOverlay;
    @Inject LocationOverlay locationOverlay;
    @Inject SoundTraceOverlay soundTraceOverlay;
    @Inject PhotoHelper photoHelper;
    @Inject MeasurementPresenter measurementPresenter;

    NumberFormat numberFormat = NumberFormat.getInstance();
    boolean initialized = false;
    int noteIndex;
    int noteTotal;
    private Note currentNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        noteOverlay.setContext(this);
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
    }

    @Override
    protected void onPause() {
        super.onPause();

        measurementPresenter.unregisterListener(this);
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
            if (sessionManager.isSessionSaved()) {
                LocationConversionHelper.BoundingBox boundingBox = boundingBox(sessionManager.getSoundMeasurements());

                mapView.getController().zoomToSpan(boundingBox.getLatSpan(), boundingBox.getLonSpan());
                mapView.getController().animateTo(boundingBox.getCenter());
            } else {
                mapView.getOverlays().add(locationOverlay);

                setButton(contextButtonCenter, R.layout.context_button_locate);
            }

            hideNoteViewer();

            mapView.getOverlays().add(noteOverlay);

            noteLeft.setOnClickListener(this);
            noteRight.setOnClickListener(this);
            noteSave.setOnClickListener(this);
            noteDelete.setOnClickListener(this);
            viewPhoto.setOnClickListener(this);

            initialized = true;
        }
    }

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

    public void noteClicked(OverlayItem item, int index, int total) {
        mapView.getController().animateTo(item.getPoint());

        currentNote = sessionManager.getNote(index);

        showNoteViewer();
        noteDate.setText(item.getTitle());
        noteText.setText(currentNote.getText());
        noteNumber.setText(numberFormat.format(index + 1) + "/" + numberFormat.format(total));
        viewPhoto.setVisibility(photoHelper.photoExists(currentNote) ? View.VISIBLE : View.GONE);

        noteIndex = index;
        noteTotal = total;
    }

    private void showNoteViewer() {
        noteViewer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (noteViewer.getVisibility() == View.VISIBLE) {
            hideNoteViewer();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.note_left:
                if (noteIndex == 0) noteIndex = noteTotal;
                noteOverlay.onTap(noteIndex - 1);
                break;
            case R.id.note_right:
                noteOverlay.onTap((noteIndex + 1) % noteTotal);
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
            case R.id.note_save:
                saveNote();
                break;
            case R.id.view_photo:
                Intents.viewPhoto(this, photoUri());
                break;
            case R.id.note_delete:
                deleteNote();
                break;
            default:
                super.onClick(view);
        }
    }

    private void deleteNote() {
        //noinspection unchecked
        new SimpleProgressTask<Void, Void, Void>(this) {
            @Override
            protected Void doInBackground(Void... voids) {
                sessionManager.deleteNote(currentNote);
                triggerSync(context);

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                refreshNotes();
                hideNoteViewer();
            }
        }.execute();
    }

    private Uri photoUri() {
        if (photoHelper.photoExistsLocally(currentNote)) {
            File file = new File(currentNote.getPhotoPath());
            return Uri.fromFile(file);
        } else {
            return Uri.parse(currentNote.getPhotoPath());
        }
    }

    private void saveNote() {
        String text = noteText.getText().toString();
        currentNote.setText(text);

        //noinspection unchecked
        new SimpleProgressTask<Void, Void, Void>(this) {
            @Override
            protected Void doInBackground(Void... voids) {
                if (sessionManager.isSessionSaved()) {
                    sessionManager.saveChanges();
                    triggerSync(context);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                hideNoteViewer();
            }
        }.execute();
    }

    private void hideNoteViewer() {
        noteViewer.setVisibility(View.INVISIBLE);
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
    }

    private void updateLocation() {
        Location location = locationHelper.getLastLocation();
        if (!sessionManager.isSessionSaved()) {
            double value = measurementPresenter.getLastAveraged();

            locationOverlay.setLocation(location);
            locationOverlay.setValue(value);
        }

        mapView.invalidate();
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
            soundTraceOverlay.update(measurement);
        }
    }
}
