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
import android.view.animation.Animation;
import android.widget.*;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.internal.Nullable;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.MarkerSize;
import pl.llp.aircasting.R;
import pl.llp.aircasting.SoundLevel;
import pl.llp.aircasting.activity.menu.MainMenu;
import pl.llp.aircasting.activity.task.SimpleProgressTask;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.event.ui.TapEvent;
import pl.llp.aircasting.guice.AirCastingApplication;
import pl.llp.aircasting.helper.*;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.SessionManager;
import pl.llp.aircasting.receiver.SyncBroadcastReceiver;
import roboguice.inject.InjectResource;
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
public abstract class AirCastingActivity extends RoboMapActivityWithProgress implements SessionManager.Listener, View.OnClickListener, Animation.AnimationListener {
    public static final String NOTE_INDEX = "noteIndex";
    public static final String SHOW_BUTTONS = "showButtons";

    // It seems it's impossible to inject these in the tests
    @Nullable @InjectResource(R.anim.fade_in) Animation fadeIn;
    @Nullable @InjectResource(R.anim.fade_out) Animation fadeOut;

    @InjectView(R.id.db_avg) TextView dbAvg;
    @InjectView(R.id.db_now) TextView dbNow;
    @InjectView(R.id.db_peak) TextView dbPeak;

    @InjectView(R.id.db_now_container) View dbNowContainer;
    @InjectView(R.id.db_avg_container) View dbAvgContainer;
    @InjectView(R.id.db_peak_container) View dbPeakContainer;

    @Nullable @InjectView(R.id.graph_button) ImageButton graphButton;
    @Nullable @InjectView(R.id.trace_button) ImageButton traceButton;
    @Nullable @InjectView(R.id.heat_map_button) ImageButton heatMapButton;

    @InjectView(R.id.context_button_left) FrameLayout contextButtonLeft;
    @InjectView(R.id.context_button_center) FrameLayout contextButtonCenter;
    @InjectView(R.id.context_button_right) FrameLayout contextButtonRight;

    @InjectView(R.id.zoom_in) Button zoomIn;
    @InjectView(R.id.zoom_out) Button zoomOut;
    @InjectView(R.id.buttons) View buttons;

    @InjectView(R.id.top_bar) View topBar;
    @InjectView(R.id.top_bar_quiet) TextView topBarQuiet;
    @InjectView(R.id.top_bar_average) TextView topBarAverage;
    @InjectView(R.id.top_bar_loud) TextView topBarLoud;
    @InjectView(R.id.top_bar_very_loud) TextView topBarVeryLoud;
    @InjectView(R.id.top_bar_too_loud) TextView topBarTooLoud;
    @InjectView(R.id.note_viewer) View noteViewer;
    @InjectView(R.id.note_date) TextView noteDate;
    @InjectView(R.id.note_number) TextView noteNumber;
    @InjectView(R.id.note_text) EditText noteText;
    @InjectView(R.id.view_photo) View viewPhoto;
    @InjectView(R.id.note_save) Button noteSave;
    @InjectView(R.id.note_delete) Button noteDelete;
    @InjectView(R.id.note_left) ImageButton noteLeft;
    @InjectView(R.id.note_right) ImageButton noteRight;

    @Inject AirCastingApplication context;

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

    @Inject EventBus eventBus;

    NumberFormat numberFormat = NumberFormat.getInstance();
    private boolean initialized = false;
    private boolean showButtons = true;
    int noteIndex = -1;
    int noteTotal;
    Note currentNote;

    @Override
    protected void onResume() {
        super.onResume();

        if (graphButton != null) graphButton.setOnClickListener(this);
        if (traceButton != null) traceButton.setOnClickListener(this);
        if (heatMapButton != null) heatMapButton.setOnClickListener(this);

        initialize();

        initializeNoteViewer();

        if (!sessionManager.isSessionSaved()) {
            Intents.startSensors(context);
        }

        sessionManager.registerListener(this);

        updateGauges();
        updateButtons();
        updateTopBar();
        updateKeepScreenOn();

        registerReceiver(syncBroadcastReceiver, SyncBroadcastReceiver.INTENT_FILTER);

        eventBus.register(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(NOTE_INDEX, noteIndex);
        outState.putBoolean(SHOW_BUTTONS, showButtons);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        noteIndex = savedInstanceState.getInt(NOTE_INDEX, -1);
        showButtons = savedInstanceState.getBoolean(SHOW_BUTTONS, true);
    }

    private void updateKeepScreenOn() {
        if (settingsHelper.isKeepScreenOn()) {
            getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void updateTopBar() {
        topBarQuiet.setText(valueOf(settingsHelper.getThreshold(SoundLevel.QUIET)));
        topBarAverage.setText(valueOf(settingsHelper.getThreshold(SoundLevel.AVERAGE)));
        topBarLoud.setText(valueOf(settingsHelper.getThreshold(SoundLevel.LOUD)));
        topBarVeryLoud.setText(valueOf(settingsHelper.getThreshold(SoundLevel.VERY_LOUD)));
        topBarTooLoud.setText(valueOf(settingsHelper.getThreshold(SoundLevel.TOO_LOUD)));
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

            fadeIn.setAnimationListener(this);
            fadeOut.setAnimationListener(this);

            if (showButtons) {
                buttons.setVisibility(View.VISIBLE);
            } else {
                buttons.setVisibility(View.GONE);
            }

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
        dbNowContainer.setVisibility(sessionManager.isSessionSaved() ? View.GONE : View.VISIBLE);

        boolean hasStats = sessionManager.isSessionStarted() || sessionManager.isSessionSaved();
        int visibility = hasStats ? View.VISIBLE : View.GONE;
        dbAvgContainer.setVisibility(visibility);
        dbPeakContainer.setVisibility(visibility);

        updatePowerView(dbAvg, sessionManager.getDbAvg(), MarkerSize.SMALL);
        updatePowerView(dbNow, sessionManager.getNow("Phone microphone"), MarkerSize.BIG);
        updatePowerView(dbPeak, sessionManager.getDbPeak(), MarkerSize.SMALL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sessionManager.unregisterListener(this);
        eventBus.unregister(this);

        if (!sessionManager.isSessionSaved()) {
            Intents.stopSensors(context);
        }

        unregisterReceiver(syncBroadcastReceiver);
    }

    @Override
    public void onNewMeasurement(Measurement measurement) {
        updateGauges();
    }

    private void updatePowerView(TextView view, double power, MarkerSize size) {
        power = calibrationHelper.calibrate(power, sessionManager.getSession());

        view.setText(NumberFormat.getInstance().format((int) power));
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, resourceHelper.getTextSize(power, size));
        view.setBackgroundDrawable(resourceHelper.getGaugeAbsolute(power, size));
    }

    @Override
    protected boolean isRouteDisplayed() {
        // The maps server needs to know if we are displaying any routes
        return false;
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

    @Override
    public void onNewSession() {
        updateGauges();
    }

    @Override
    public void onNewNote(Note note) {
    }

    @Subscribe
    public void onEvent(SensorEvent event) {
        updateGauges();
    }

    @Override
    public void onError() {
        Toast.makeText(context, R.string.mic_error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.graph_button:
                startActivity(new Intent(context, GraphActivity.class));
                break;
            case R.id.trace_button:
                startActivity(new Intent(context, SoundTraceActivity.class));
                break;
            case R.id.heat_map_button:
                startActivity(new Intent(context, HeatMapActivity.class));
                break;
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
        }
    }

    private void toggleButtons() {
        if (buttons.getVisibility() == View.VISIBLE) {
            hideButtons();
        } else {
            showButtons();
        }
    }

    private void hideButtons() {
        buttons.startAnimation(fadeOut);
        showButtons = false;
    }

    private void showButtons() {
        buttons.startAnimation(fadeIn);
        showButtons = true;
    }

    @Subscribe
    public void onEvent(TapEvent event) {
        toggleButtons();
    }

    @Override
    public void onAnimationStart(Animation animation) {
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        if (buttons.getVisibility() == View.VISIBLE) {
            buttons.setVisibility(View.GONE);
        } else {
            buttons.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
    }

    public void noteClicked(int index) {
        int total = sessionManager.getNoteCount();
        if(total == 0) return;
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
