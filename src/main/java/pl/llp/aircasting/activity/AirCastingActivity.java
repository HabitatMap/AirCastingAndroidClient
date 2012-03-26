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
import android.location.LocationManager;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.MarkerSize;
import pl.llp.aircasting.MeasurementLevel;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.menu.MainMenu;
import pl.llp.aircasting.activity.task.SimpleProgressTask;
import pl.llp.aircasting.event.sensor.AudioReaderErrorEvent;
import pl.llp.aircasting.event.sensor.MeasurementEvent;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.event.session.SessionChangeEvent;
import pl.llp.aircasting.helper.*;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.SessionManager;
import pl.llp.aircasting.receiver.SyncBroadcastReceiver;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;
import roboguice.inject.InjectView;

import java.text.NumberFormat;

import static java.lang.String.valueOf;
import static pl.llp.aircasting.Intents.triggerSync;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 9/30/11
 * Time: 3:18 PM
 */
public abstract class AirCastingActivity extends ButtonsActivity implements View.OnClickListener {
    public static final String NOTE_INDEX = "noteIndex";

    @InjectView(R.id.avg_gauge) TextView dbAvg;
    @InjectView(R.id.now_gauge) TextView dbNow;
    @InjectView(R.id.peak_gauge) TextView dbPeak;

    @InjectView(R.id.now_container) View dbNowContainer;
    @InjectView(R.id.avg_container) View dbAvgContainer;
    @InjectView(R.id.peak_container) View dbPeakContainer;

    @InjectView(R.id.context_button_left) FrameLayout contextButtonLeft;
    @InjectView(R.id.context_button_center) FrameLayout contextButtonCenter;
    @InjectView(R.id.context_button_right) FrameLayout contextButtonRight;

    @InjectView(R.id.zoom_in) Button zoomIn;
    @InjectView(R.id.zoom_out) Button zoomOut;

    @InjectView(R.id.top_bar) View topBar;
    @InjectView(R.id.top_bar_very_low) TextView topBarQuiet;
    @InjectView(R.id.top_bar_low) TextView topBarAverage;
    @InjectView(R.id.top_bar_mid) TextView topBarLoud;
    @InjectView(R.id.top_bar_high) TextView topBarVeryLoud;
    @InjectView(R.id.top_bar_very_high) TextView topBarTooLoud;
    @InjectView(R.id.note_viewer) View noteViewer;
    @InjectView(R.id.note_date) TextView noteDate;
    @InjectView(R.id.note_number) TextView noteNumber;
    @InjectView(R.id.note_text) EditText noteText;
    @InjectView(R.id.view_photo) View viewPhoto;
    @InjectView(R.id.note_save) Button noteSave;
    @InjectView(R.id.note_delete) Button noteDelete;
    @InjectView(R.id.note_left) ImageButton noteLeft;
    @InjectView(R.id.note_right) ImageButton noteRight;

    @Inject SessionManager sessionManager;

    @Inject LayoutInflater layoutInflater;

    @Inject LocationManager locationManager;
    @Inject LocationHelper locationHelper;
    @Inject CalibrationHelper calibrationHelper;
    @Inject SettingsHelper settingsHelper;
    @Inject ResourceHelper resourceHelper;
    @Inject PhotoHelper photoHelper;

    @Inject MainMenu mainMenu;

    @Inject SyncBroadcastReceiver syncBroadcastReceiver;

    NumberFormat numberFormat = NumberFormat.getInstance();
    private boolean initialized = false;
    int noteIndex = -1;
    int noteTotal;
    Note currentNote;

    @Override
    protected void onResume() {
        super.onResume();

        initialize();

        initializeNoteViewer();

        if (!sessionManager.isSessionSaved()) {
            Intents.startSensors(context);
        }

        updateGauges();
        updateButtons();
        updateTopBar();
        updateKeepScreenOn();

        registerReceiver(syncBroadcastReceiver, SyncBroadcastReceiver.INTENT_FILTER);
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

    private void updateTopBar() {
        topBarQuiet.setText(valueOf(settingsHelper.getThreshold(MeasurementLevel.VERY_LOW)));
        topBarAverage.setText(valueOf(settingsHelper.getThreshold(MeasurementLevel.LOW)));
        topBarLoud.setText(valueOf(settingsHelper.getThreshold(MeasurementLevel.MID)));
        topBarVeryLoud.setText(valueOf(settingsHelper.getThreshold(MeasurementLevel.HIGH)));
        topBarTooLoud.setText(valueOf(settingsHelper.getThreshold(MeasurementLevel.VERY_HIGH)));
    }

    private void updateButtons() {
        if (sessionManager.isSessionStarted()) {
            setButton(contextButtonLeft, R.layout.context_button_stop);
            setButton(contextButtonRight, R.layout.context_button_note);
        } else if (!sessionManager.isSessionSaved()) {
            setButton(contextButtonLeft, R.layout.context_button_play);
            setButton(contextButtonRight, R.layout.context_button_placeholder);
        }
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

            if (sessionManager.isSessionSaved()) {
                setButton(contextButtonLeft, R.layout.context_button_edit);
                setButton(contextButtonCenter, R.layout.context_button_share);
            } else if (sessionManager.isSessionStarted()) {
                updateButtons();
            }

            initialized = true;
        }
    }

    protected void setButton(FrameLayout layout, int id) {
        layout.removeAllViews();

        View view = layoutInflater.inflate(id, null);
        view.setOnClickListener(this);

        layout.addView(view);
    }

    private void updateGauges() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dbNowContainer.setVisibility(sessionManager.isSessionSaved() ? View.GONE : View.VISIBLE);

                boolean hasStats = sessionManager.isSessionStarted() || sessionManager.isSessionSaved();
                int visibility = hasStats ? View.VISIBLE : View.GONE;
                dbAvgContainer.setVisibility(visibility);
                dbPeakContainer.setVisibility(visibility);

                updatePowerView(dbAvg, sessionManager.getDbAvg(), MarkerSize.SMALL);
                updatePowerView(dbNow, sessionManager.getNow(SimpleAudioReader.SENSOR_NAME), MarkerSize.BIG);
                updatePowerView(dbPeak, sessionManager.getDbPeak(), MarkerSize.SMALL);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (!sessionManager.isSessionSaved()) {
            Intents.stopSensors(context);
        }

        unregisterReceiver(syncBroadcastReceiver);
    }

    @Subscribe
    public void onEvent(MeasurementEvent event) {
        updateGauges();
    }

    private void updatePowerView(TextView view, double power, MarkerSize size) {
        power = calibrationHelper.calibrate(power, sessionManager.getSession());

        view.setText(NumberFormat.getInstance().format((int) power));
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, resourceHelper.getTextSize(power, size));
        view.setBackgroundDrawable(resourceHelper.getGaugeAbsolute(SimpleAudioReader.SENSOR_NAME, size, power));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return mainMenu.create(this, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mainMenu.handleClick(this, item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Intents.SAVE_DIALOG:
                startActivity(new Intent(getApplicationContext(), SoundTraceActivity.class));
                break;
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

    private synchronized void toggleAirCasting() {
        if (sessionManager.isSessionStarted()) {
            stopAirCasting();
        } else {
            startAirCasting();
        }

        updateButtons();
    }

    private void stopAirCasting() {
        if (sessionManager.getSoundMeasurements().isEmpty()) {
            Toast.makeText(context, R.string.no_data, Toast.LENGTH_SHORT).show();
            sessionManager.discardSession();
        } else {
            Intent intent = new Intent(this, SaveSessionActivity.class);
            startActivityForResult(intent, Intents.SAVE_DIALOG);
        }
    }

    private void startAirCasting() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(context, R.string.gps_off_warning, Toast.LENGTH_LONG).show();
        } else if (!locationHelper.hasGPSFix()) {
            Toast.makeText(context, R.string.no_gps_fix_warning, Toast.LENGTH_LONG).show();
        }

        if (!settingsHelper.hasCredentials()) {
            Toast.makeText(context, R.string.account_reminder, Toast.LENGTH_LONG).show();
        }

        sessionManager.startSession();
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toggle_aircasting:
                toggleAirCasting();
                break;
            case R.id.edit:
                Intents.editSession(this, sessionManager.getSession());
                break;
            case R.id.note:
                Intents.makeANote(this);
                break;
            case R.id.top_bar:
                startActivity(new Intent(context, ThresholdsActivity.class));
                break;
            case R.id.share:
                startActivity(new Intent(context, ShareSessionActivity.class));
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
