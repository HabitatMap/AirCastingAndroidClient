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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.task.SimpleProgressTask;
import pl.llp.aircasting.event.sensor.AudioReaderErrorEvent;
import pl.llp.aircasting.event.sensor.MeasurementEvent;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.event.session.SessionChangeEvent;
import pl.llp.aircasting.event.ui.ViewStreamEvent;
import pl.llp.aircasting.helper.*;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.SensorManager;
import pl.llp.aircasting.model.Session;
import roboguice.inject.InjectView;

import java.text.NumberFormat;

import static pl.llp.aircasting.Intents.triggerSync;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 9/30/11
 * Time: 3:18 PM
 */
public abstract class AirCastingActivity extends ButtonsActivity implements View.OnClickListener {
    public static final String NOTE_INDEX = "noteIndex";

    @InjectView(R.id.gauge_container) View gauges;
    @InjectView(R.id.now_gauge) View nowGauge;

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

    @Inject SelectSensorHelper selectSensorHelper;
    @Inject ResourceHelper resourceHelper;
    @Inject SensorManager sensorManager;
    @Inject TopBarHelper topBarHelper;
    @Inject PhotoHelper photoHelper;
    @Inject GaugeHelper gaugeHelper;

    NumberFormat numberFormat = NumberFormat.getInstance();
    private boolean initialized = false;
    int noteIndex = -1;
    Note currentNote;
    int noteTotal;

    @Override
    protected void onResume() {
        super.onResume();

        initialize();

        initializeNoteViewer();

        updateGauges();
        updateKeepScreenOn();
        topBarHelper.updateTopBar(sensorManager.getVisibleSensor(), topBar);
    }

    private void initialize() {
        if (!initialized) {
            zoomIn.setOnClickListener(this);
            zoomOut.setOnClickListener(this);
            topBar.setOnClickListener(this);

            noteLeft.setOnClickListener(this);
            noteRight.setOnClickListener(this);
            noteSave.setOnClickListener(this);
            noteDelete.setOnClickListener(this);
            viewPhoto.setOnClickListener(this);

            nowGauge.setOnClickListener(this);

            initialized = true;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(NOTE_INDEX, noteIndex);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        noteIndex = savedInstanceState.getInt(NOTE_INDEX, -1);
    }

    private void updateKeepScreenOn() {
        if (settingsHelper.isKeepScreenOn()) {
            getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void updateGauges() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gaugeHelper.updateGauges(sensorManager.getVisibleSensor(), gauges);
            }
        });
    }

    @Subscribe
    public void onEvent(MeasurementEvent event) {
        updateGauges();
    }

    @Subscribe
    public void onEvent(SessionChangeEvent event) {
        updateGauges();
    }

    @Subscribe
    public void onEvent(SensorEvent event) {
        updateGauges();
    }

    @Subscribe
    public void onEvent(AudioReaderErrorEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, R.string.mic_error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Subscribe
    public void onEvent(ViewStreamEvent event) {
        topBarHelper.updateTopBar(sensorManager.getVisibleSensor(), topBar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Intents.EDIT_SESSION:
                if (resultCode == R.id.save_button) {
                    Session session = Intents.editSessionResult(data);

                    sessionManager.updateSession(session);
                    Intents.triggerSync(context);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.top_bar:
                Intents.thresholdsEditor(this, sensorManager.getVisibleSensor());
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
            case R.id.now_gauge:
                selectSensorHelper.chooseSensor(this);
                break;
            default:
                super.onClick(view);
                break;
        }
    }

    public void noteClicked(int index) {
        int total = sessionManager.getNoteCount();
        if (total == 0) return;
        index = ((index % total) + total) % total;

        currentNote = sessionManager.getNote(index);

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

    protected abstract void refreshNotes();
}
