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

import android.app.Application;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.google.inject.Inject;
import pl.llp.aircasting.R;
import pl.llp.aircasting.SoundLevel;
import pl.llp.aircasting.helper.SettingsHelper;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/8/11
 * Time: 2:37 PM
 */
public class ThresholdsActivity extends RoboActivity implements View.OnClickListener, View.OnFocusChangeListener, SeekBar.OnSeekBarChangeListener {
    @InjectView(R.id.color_scale_too_loud) EditText tooLoudEdit;
    @InjectView(R.id.color_scale_very_loud) EditText veryLoudEdit;
    @InjectView(R.id.color_scale_loud) EditText loudEdit;
    @InjectView(R.id.color_scale_average) EditText averageEdit;
    @InjectView(R.id.color_scale_quiet) EditText quietEdit;

    @InjectView(R.id.color_scale_very_loud_slider) SeekBar veryLoudSlider;
    @InjectView(R.id.color_scale_loud_slider) SeekBar loudSlider;
    @InjectView(R.id.color_scale_average_slider) SeekBar averageSlider;

    @InjectView(R.id.top_bar_too_loud) TextView topBarTooLoud;
    @InjectView(R.id.top_bar_very_loud) TextView topBarVeryLoud;
    @InjectView(R.id.top_bar_loud) TextView topBarLoud;
    @InjectView(R.id.top_bar_average) TextView topBarAverage;
    @InjectView(R.id.top_bar_quiet) TextView topBarQuiet;

    @InjectView(R.id.save) Button save;
    @InjectView(R.id.reset) Button reset;

    @Inject SettingsHelper settingsHelper;
    @Inject Application context;

    private int tooLoud;
    private int veryLoud;
    private int loud;
    private int average;
    private int quiet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.color_scale);

        tooLoudEdit.setText("" + settingsHelper.getThreshold(SoundLevel.TOO_LOUD));
        veryLoudEdit.setText("" + settingsHelper.getThreshold(SoundLevel.VERY_LOUD));
        loudEdit.setText("" + settingsHelper.getThreshold(SoundLevel.LOUD));
        averageEdit.setText("" + settingsHelper.getThreshold(SoundLevel.AVERAGE));
        quietEdit.setText("" + settingsHelper.getThreshold(SoundLevel.QUIET));

        veryLoudEdit.setOnFocusChangeListener(this);
        loudEdit.setOnFocusChangeListener(this);
        averageEdit.setOnFocusChangeListener(this);
        tooLoudEdit.setOnFocusChangeListener(this);
        quietEdit.setOnFocusChangeListener(this);

        veryLoudSlider.setOnSeekBarChangeListener(this);
        loudSlider.setOnSeekBarChangeListener(this);
        averageSlider.setOnSeekBarChangeListener(this);

        save.setOnClickListener(this);
        reset.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save:
                saveThresholds();
                break;
            case R.id.reset:
                finish();
                settingsHelper.resetThresholds();
                break;
        }
    }

    private void saveThresholds() {
        calculateThresholds();
        fixThresholds();       

        settingsHelper.setThreshold(SoundLevel.TOO_LOUD, tooLoud);
        settingsHelper.setThreshold(SoundLevel.VERY_LOUD, veryLoud);
        settingsHelper.setThreshold(SoundLevel.LOUD, loud);
        settingsHelper.setThreshold(SoundLevel.AVERAGE, average);
        settingsHelper.setThreshold(SoundLevel.QUIET, quiet);

        finish();
    }

    private void fixThresholds() {
        View focus = getCurrentFocus();
        int id = focus == null ? 0 : focus.getId();
        SoundLevel soundLevel = toSoundLevel(id);

        fixThresholds(soundLevel);
    }

    private void calculateThresholds() {
        try {
            tooLoud = tooLoudEdit.getText().length() == 0 ? 0 : Integer.parseInt(tooLoudEdit.getText().toString());
            veryLoud = veryLoudEdit.getText().length() == 0 ? 0 : Integer.parseInt(veryLoudEdit.getText().toString());
            loud = loudEdit.getText().length() == 0 ? 0 : Integer.parseInt(loudEdit.getText().toString());
            average = averageEdit.getText().length() == 0 ? 0 : Integer.parseInt(averageEdit.getText().toString());
            quiet = quietEdit.getText().length() == 0 ? 0 : Integer.parseInt(quietEdit.getText().toString());
        } catch (NumberFormatException e) {
            updateEdits();
            Toast.makeText(context, R.string.setting_error, Toast.LENGTH_LONG).show();
        }
    }

    private void updateEdits() {
        tooLoudEdit.setText("" + tooLoud);
        veryLoudEdit.setText("" + veryLoud);
        loudEdit.setText("" + loud);
        averageEdit.setText("" + average);
        quietEdit.setText("" + quiet);
    }

    private SoundLevel toSoundLevel(int id) {
        switch (id) {
            case R.id.color_scale_quiet:
                return SoundLevel.QUIET;
            case R.id.color_scale_average:
            case R.id.color_scale_average_slider:
                return SoundLevel.AVERAGE;
            case R.id.color_scale_loud:
            case R.id.color_scale_loud_slider:
                return SoundLevel.LOUD;
            case R.id.color_scale_very_loud:
            case R.id.color_scale_very_loud_slider:
                return SoundLevel.VERY_LOUD;
            default:
                return SoundLevel.TOO_LOUD;
        }
    }

    private void fixThresholds(SoundLevel fixed) {
        switch (fixed) {
            case QUIET:
                if (average <= quiet) average = quiet + 1;
            case AVERAGE:
                if (loud <= average) loud = average + 1;
            case LOUD:
                if (veryLoud <= loud) veryLoud = loud + 1;
            case VERY_LOUD:
                if (tooLoud <= veryLoud) tooLoud = veryLoud + 1;
        }

        switch (fixed) {
            case TOO_LOUD:
                if (veryLoud >= tooLoud) veryLoud = tooLoud - 1;
            case VERY_LOUD:
                if (loud >= veryLoud) loud = veryLoud - 1;
            case LOUD:
                if (average >= loud) average = loud - 1;
            case AVERAGE:
                if (quiet >= average) quiet = average - 1;
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        calculateThresholds();
        fixThresholds(toSoundLevel(view.getId()));
        updateViews();
    }

    private void updateViews() {
        updateSliders();
        updateEdits();
        updateTopBar();
    }

    private void updateTopBar() {
        topBarQuiet.setText("" + quiet);
        topBarAverage.setText("" + average);
        topBarLoud.setText("" + loud);
        topBarVeryLoud.setText("" + veryLoud);
        topBarTooLoud.setText("" + tooLoud);
    }

    private void updateSliders() {
        veryLoudSlider.setProgress((int) (veryLoudSlider.getMax() * project(veryLoud)));
        loudSlider.setProgress((int) (loudSlider.getMax() * project(loud)));
        averageSlider.setProgress((int) (averageSlider.getMax() * project(average)));
    }

    private double project(double value) {
        return (value - quiet) / (tooLoud - quiet);
    }

    @Override
    public void onProgressChanged(SeekBar bar, int i, boolean b) {
        int value = unProject(bar.getProgress(), bar.getMax());
        SoundLevel soundLevel = toSoundLevel(bar.getId());
        switch (soundLevel) {
            case AVERAGE:
                averageEdit.setText("" + value);
                break;
            case LOUD:
                loudEdit.setText("" + value);
                break;
            case VERY_LOUD:
                veryLoudEdit.setText("" + value);
                break;
        }
    }

    private int unProject(int progress, int max) {
        return (int) (quiet + progress / (double) max * (tooLoud - quiet));
    }

    @Override
    public void onStartTrackingTouch(SeekBar bar) {
        bar.requestFocus();
    }

    @Override
    public void onStopTrackingTouch(SeekBar bar) {
        calculateThresholds();
        SoundLevel soundLevel = toSoundLevel(bar.getId());
        fixThresholds(soundLevel);
        updateViews();
    }
}
