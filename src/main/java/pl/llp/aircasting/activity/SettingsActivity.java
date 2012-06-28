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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.google.inject.Inject;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.extsens.ExternalSensorActivity;
import pl.llp.aircasting.activity.menu.MainMenu;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.SensorManager;
import roboguice.activity.RoboPreferenceActivity;
import roboguice.inject.InjectResource;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/10/11
 * Time: 12:45 PM
 */
public class SettingsActivity extends RoboPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String ACCOUNT_KEY = "account";
    public static final String COLOR_SCALE_KEY = "color_scale";
    public static final String EXTERNAL_SENSOR_KEY = "external_sensor";
    public static final String MEASUREMENT_STREAMS_KEY = "measurement_streams";

    @Inject Application context;

    @Inject SharedPreferences sharedPreferences;
    @Inject SettingsHelper settingsHelper;
    @Inject SensorManager sensorManager;
    @Inject MainMenu mainMenu;

    @InjectResource(R.string.profile_template) String profileTemplate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Preference account = getPreferenceScreen().findPreference(ACCOUNT_KEY);

        if (settingsHelper.hasCredentials()) {
            String email = settingsHelper.getUserLogin();
            String text = String.format(profileTemplate, email);
            account.setSummary(text);
        } else {
            account.setSummary(R.string.profile_summary);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals(COLOR_SCALE_KEY)) {
            Intents.thresholdsEditor(this, sensorManager.getVisibleSensor());
            return true;
        } else if (preference.getKey().equals(ACCOUNT_KEY)) {
            signInOrOut();
            return true;
        } else if (preference.getKey().equals(EXTERNAL_SENSOR_KEY)) {
            startActivity(new Intent(this, ExternalSensorActivity.class));
            return true;
        } else if (preference.getKey().equals(MEASUREMENT_STREAMS_KEY)) {
            Intents.startStreamsActivity(this);
            return true;
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }

    private void signInOrOut() {
        if (settingsHelper.hasCredentials()) {
            startActivity(new Intent(this, SignOutActivity.class));
        } else {
            startActivity(new Intent(this, ProfileActivity.class));
        }
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
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (!settingsHelper.validateFormat(key)) {
            Toast.makeText(context, R.string.setting_error, Toast.LENGTH_LONG).show();
        } else if (key.equals(SettingsHelper.OFFSET_60_DB) && !settingsHelper.validateOffset60DB()) {
            Toast.makeText(context, R.string.offset_error, Toast.LENGTH_LONG).show();
        } else if (key.equals(SettingsHelper.AVERAGING_TIME) && !settingsHelper.validateAveragingTime()) {
            Toast.makeText(context, R.string.averaging_time_error, Toast.LENGTH_LONG).show();
        }
    }
}
