/**
 * AirCasting - Share your Air!
 * Copyright (C) 2011-2012 HabitatMap, Inc.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * You can contact the authors by email at <info@habitatmap.org>
 */
package pl.llp.aircasting.screens.settings;

import android.preference.CheckBoxPreference;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.screens.common.ApplicationState;
import pl.llp.aircasting.screens.common.helpers.SettingsHelper;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.widget.Toast;
import com.google.inject.Inject;
import pl.llp.aircasting.screens.common.ToastHelper;
import roboguice.activity.RoboPreferenceActivity;

public class SettingsActivity extends RoboPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String ACCOUNT_KEY = "account";
    public static final String MEASUREMENT_STREAMS_KEY = "measurement_streams";
    public static final String BACKEND_SETTINGS_KEY = "backend_settings";
    public static final String DISABLE_MAPS_KEY = "disable_maps";
    public static final String CONTRIBUTE_TO_CROWDMAP = "contribute_to_crowdmap";

    @Inject Application context;

    @Inject SharedPreferences sharedPreferences;
    @Inject SettingsHelper settingsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        final CheckBoxPreference alertPreference = (CheckBoxPreference) getPreferenceScreen().findPreference(SettingsHelper.STREAMING_ALERT);

        alertPreference.setOnPreferenceChangeListener(new StreamingAlertListener());
    }

    // preferences screen behaves differently than the others, so we have to use this workaround to add the toolbar
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar toolbar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.toolbar, root, false);
        root.addView(toolbar, 0); // insert at top

        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.abc_ic_ab_back_material));
        toolbar.setContentInsetStartWithNavigation(0);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        toolbar.setTitle("Settings");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (MEASUREMENT_STREAMS_KEY.equals(preference.getKey())) {
            Intents.startDashboardActivity(this);
            return true;
        } else if (BACKEND_SETTINGS_KEY.equals(preference.getKey())) {
            startActivity(new Intent(this, BackendSettingsActivity.class));
            return true;
        } else if (DISABLE_MAPS_KEY.equals(preference.getKey())) {
            startActivity(new Intent(this, DisableMapSettingsActivity.class));
            return true;
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (!settingsHelper.validateFormat(key)) {
            ToastHelper.show(context, R.string.setting_error, Toast.LENGTH_LONG);
        } else if (key.equals(SettingsHelper.AVERAGING_TIME) && !settingsHelper.validateAveragingTime()) {
            ToastHelper.show(context, R.string.averaging_time_error, Toast.LENGTH_LONG);
        }
    }
}