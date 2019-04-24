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
package pl.llp.aircasting.screens.stream.base;

import android.net.Uri;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.event.measurements.MeasurementEvent;
import pl.llp.aircasting.event.sensor.FixedSensorEvent;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.screens.common.helpers.FormatHelper;
import pl.llp.aircasting.screens.common.helpers.PhotoHelper;
import pl.llp.aircasting.screens.common.helpers.ResourceHelper;
import pl.llp.aircasting.screens.common.helpers.SelectSensorHelper;
import pl.llp.aircasting.screens.common.sessionState.SessionDataFactory;
import pl.llp.aircasting.screens.common.base.SimpleProgressTask;
import pl.llp.aircasting.screens.common.ToastHelper;
import pl.llp.aircasting.screens.common.sessionState.VisibleSession;
import pl.llp.aircasting.event.session.VisibleSessionUpdatedEvent;
import pl.llp.aircasting.event.sensor.AudioReaderErrorEvent;
import pl.llp.aircasting.event.sensor.ThresholdSetEvent;
import pl.llp.aircasting.event.ui.VisibleStreamUpdatedEvent;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.Sensor;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import pl.llp.aircasting.event.sensor.SensorConnectedEvent;
import pl.llp.aircasting.screens.stream.GaugeHelper;
import pl.llp.aircasting.screens.stream.TopBarHelper;
import roboguice.inject.InjectView;

import java.io.File;
import java.text.NumberFormat;
import java.util.concurrent.atomic.AtomicBoolean;

import static pl.llp.aircasting.Intents.triggerSync;

public abstract class AirCastingActivity extends AirCastingBaseActivity implements View.OnClickListener {
    public static final String NOTE_INDEX = "noteIndex";
    public static final String VISIBLE_SESSION_ID = "visibleSessionId";
    public static final String VISIBLE_SENSOR_ID = "visibleSensorId";

    @InjectView(R.id.gauge_container) View gauges;

    @InjectView(R.id.note_right) ImageButton noteRight;
    @InjectView(R.id.note_number) TextView noteNumber;
    @InjectView(R.id.note_left) ImageButton noteLeft;
    @InjectView(R.id.note_delete) Button noteDelete;
    @InjectView(R.id.note_viewer) View noteViewer;
    @InjectView(R.id.note_date) TextView noteDate;
    @InjectView(R.id.note_text) EditText noteText;
    @InjectView(R.id.note_save) Button noteSave;
    @InjectView(R.id.view_photo) View viewPhoto;
    @InjectView(R.id.top_bar) View topBar;

    @Inject public VisibleSession visibleSession;
    @Inject public ResourceHelper resourceHelper;

    @Inject SelectSensorHelper selectSensorHelper;
    @Inject TopBarHelper topBarHelper;
    @Inject PhotoHelper photoHelper;
    @Inject GaugeHelper gaugeHelper;
    @Inject SessionDataFactory sessionData;

    NumberFormat numberFormat = NumberFormat.getInstance();
    private boolean initialized = false;
    int noteIndex = -1;
    Note currentNote;
    int noteTotal;

    final AtomicBoolean noUpdateInProgress = new AtomicBoolean(true);

    private Handler handler = new Handler();

    private Thread pollServerTask = new Thread(new Runnable() {
        @Override
        public void run() {
            Intents.triggerStreamingSessionsSync(context);

            handler.postDelayed(pollServerTask, 60000);
        }
    });

    @Override
    protected void onResume() {
        super.onResume();

        initialize();

        initializeNoteViewer();
        startUpdatingFixedSessions();

        updateGauges();
        updateKeepScreenOn();
        topBarHelper.updateTopBar(visibleSession.getSensor(), topBar);
        Intents.startIOIO(context);
        Intents.startDatabaseWriterService(context);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(pollServerTask);
    }

    private void initialize() {
        if (!initialized) {
            zoomOut.setOnClickListener(this);
            zoomIn.setOnClickListener(this);
            topBar.setOnClickListener(this);

            noteDelete.setOnClickListener(this);
            noteRight.setOnClickListener(this);
            viewPhoto.setOnClickListener(this);
            noteLeft.setOnClickListener(this);
            noteSave.setOnClickListener(this);

            gauges.setOnClickListener(this);

            initialized = true;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(NOTE_INDEX, noteIndex);
        outState.putInt(VISIBLE_SESSION_ID, (int) visibleSession.getVisibleSessionId());
        outState.putString(VISIBLE_SENSOR_ID, visibleSession.getSensor().getSensorName());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        noteIndex = savedInstanceState.getInt(NOTE_INDEX, -1);
        int visibleSessionId = savedInstanceState.getInt(VISIBLE_SESSION_ID);
        String visibleSensorName = savedInstanceState.getString(VISIBLE_SENSOR_ID);

        sessionData.setVisibleSession(visibleSessionId);
        sessionData.setVisibleSensorFromName(visibleSessionId, visibleSensorName);
    }

    private void startUpdatingFixedSessions() {
        if (viewingSessionsManager.isAnySessionFixed()) {
            handler.post(pollServerTask);
        }
    }

    private void updateKeepScreenOn() {
        if (settingsHelper.isKeepScreenOn()) {
            getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    protected void updateGauges() {
        final Sensor sensor = visibleSession.getSensor();
        updateGaugeFaces(sensor);
    }

    private void updateGaugeFaces(final Sensor visibleSensor) {
        if (noUpdateInProgress.get()) {
            noUpdateInProgress.set(false);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    gaugeHelper.updateGauges(visibleSensor, gauges);
                    noUpdateInProgress.set(true);
                }
            });
        }
    }

    @Subscribe
    public void onEvent(MeasurementEvent event) {
        updateGauges();
    }

    @Subscribe
    public void onEvent(VisibleSessionUpdatedEvent event) {
        updateGauges();
    }

    @Subscribe
    public void onEvent(SensorEvent event) {
        updateGauges();
    }

    @Subscribe
    public void onEvent(FixedSensorEvent event) {
        updateGauges();
    }

    @Subscribe
    public void onEvent(ThresholdSetEvent event)
    {
        updateGauges();
    }

    @Subscribe
    public void onEvent(SensorConnectedEvent event) {
        invalidateOptionsMenu();
    }

    @Subscribe
    public void onEvent(AudioReaderErrorEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastHelper.show(context, R.string.mic_error, Toast.LENGTH_LONG);
            }
        });
    }

    @Subscribe
    public void onEvent(VisibleStreamUpdatedEvent event) {
        topBarHelper.updateTopBar(event.getSensor(), topBar);
        updateGauges();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuInflater inflater = getDelegate().getMenuInflater();

        if (currentSessionManager.isSessionIdle()) {
            inflater.inflate(R.menu.toolbar_start_recording, menu);
        } else if (currentSessionManager.isSessionRecording()){
            inflater.inflate(R.menu.toolbar_stop_recording, menu);
            inflater.inflate(R.menu.toolbar_make_note, menu);
        } else {
            return true;
        }

        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.top_bar:
                Intents.thresholdsEditor(this, visibleSession.getSensor());
                break;
            case R.id.note_save:
                saveNote();
                break;
            case R.id.note_delete:
                deleteNote();
                break;
            case R.id.note_left:
                noteClicked(noteIndex - 1);
                break;
            case R.id.note_right:
                noteClicked(noteIndex + 1);
                break;
            case R.id.gauge_container:
                showDialog(SelectSensorHelper.DIALOG_ID);
                break;
            case R.id.view_photo:
                Intents.viewPhoto(this, photoUri());
                break;
            default:
                break;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id){
            case SelectSensorHelper.DIALOG_ID: return selectSensorHelper.chooseSensor(this);
            default: return super.onCreateDialog(id);
        }
    }

    public void noteClicked(int index) {
        int total = visibleSession.getSessionNoteCount();
        if (total == 0) return;
        index = ((index % total) + total) % total;

        currentNote = visibleSession.getSessionNote(index);

        showNoteViewer();

        String title = FormatHelper.dateTime(currentNote.getDate()).toString();
        noteDate.setText(title);
        noteText.setText(currentNote.getText());
        noteNumber.setText(numberFormat.format(index + 1) + "/" + numberFormat.format(total));
        viewPhoto.setVisibility(photoHelper.photoExists(currentNote) ? View.VISIBLE : View.GONE);

        noteIndex = index;
        noteTotal = total;
    }

    private void showNoteViewer() {
        noteViewer.setVisibility(View.VISIBLE);
    }

    protected void initializeNoteViewer() {
        if (noteIndex == -1) {
            hideNoteViewer();
        } else {
            noteClicked(noteIndex);
        }
    }

    protected void hideNoteViewer() {
        noteViewer.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {
        if (noteViewer.getVisibility() == View.VISIBLE) {
            hideNoteViewer();
        } else {
            super.onBackPressed();
        }
    }

    private void saveNote() {
        String text = noteText.getText().toString();
        currentNote.setText(text);

        //noinspection unchecked
        new SimpleProgressTask<Void, Void, Void>(this) {
            @Override
            protected Void doInBackground(Void... voids) {
                if (visibleSession.isVisibleSessionViewed()) {
                    currentSessionManager.updateNote(currentNote);
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

    private void deleteNote() {
        //noinspection unchecked
        new SimpleProgressTask<Void, Void, Void>(this) {
            @Override
            protected Void doInBackground(Void... voids) {
                currentSessionManager.deleteNote(currentNote);
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

    protected abstract void refreshNotes();
}
