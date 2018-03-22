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
package pl.llp.aircasting.helper;

import pl.llp.aircasting.event.sensor.ThresholdSetEvent;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.internal.MeasurementLevel;
import pl.llp.aircasting.sensor.ExternalSensorDescriptor;

import android.content.SharedPreferences;
import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@Singleton
public class SettingsHelper {
    public static final String AUTH_TOKEN = "auth_token";
    public static final String USER_LOGIN = "user_login";
    public static final String USER_EMAIL = "user_email";
    public static final String KEEP_SCREEN_ON = "keep_screen_on";
    public static final String OFFSET_60_DB = "offset_60_db";
    public static final String CALIBRATION = "calibration";
    public static final String SATELLITE = "satellite";
    public static final String BACKEND = "backend";
    public static final String BACKEND_PORT = "backend_port";
    public static final String HEAT_MAP_DENSITY = "heat_map_density";
    public static final String FIRST_LAUNCH = "first_launch";
    public static final String DISABLE_MAPS = "disable_maps";
    public static final String CONTRIBUTE_TO_CROWDMAP = "contribute_to_crowdmap";

    public static final int DEFAULT_CALIBRATION = 100;
    public static final boolean DEFAULT_SATELLITE = false;
    public static final String DEFAULT_BACKEND = "aircasting.org";
    public static final int DEFAULT_BACKEND_PORT = 80;
    public static final int DEFAULT_HEAT_MAP_DENSITY = 10;
    public static final boolean DEFAULT_KEEP_SCREEN_ON = false;
    public static final int DEFAULT_OFFSET_60_DB = 0;
    public static final int MIN_OFFSET_60_DB = -5;
    public static final int MAX_OFFSET_60_DB = 5;
    public static final String SAMPLE_INTERVAL = "sample_interval";
    public static final String AVERAGING_TIME = "averaging_time";
    private static final int MIN_AVERAGING_TIME = 1;
    private static final int MAX_AVERAGING_TIME = 3600;
    public static final String SYNC_ONLY_WIFI = "sync_only_wifi";
    public static final String SHOW_ROUTE = "show_route";
    public static final String SENSORS = "external_sensors_json";
    public static final boolean DEFAULT_CONTRIBUTE_TO_CROWDMAP = true;

    public static final String SHOW_GRAPH_METADATA = "show_graph_metadata";

    @Inject SharedPreferences preferences;
    @Inject Gson gson;
    @Inject EventBus eventBus;

    @Inject
    public void init() {
        eventBus.register(this);
    }

    @Subscribe
    public void onEvent(ThresholdSetEvent event) {
        setThreshold(event.getSensor(), event.getLevel(), event.getValue());
    }

    public int getCalibration() {
        return getInt(CALIBRATION, DEFAULT_CALIBRATION);
    }

    private int getInt(String key, int defaultValue) {
        String value = preferences.getString(key, Integer.toString(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            remove(key);
            return defaultValue;
        }
    }

    public boolean isSatelliteView() {
        return preferences.getBoolean(SATELLITE, DEFAULT_SATELLITE);
    }

    public String getBackendURL() {
        return preferences.getString(BACKEND, DEFAULT_BACKEND);
    }

    public int getBackendPort() {
        return getInt(BACKEND_PORT, DEFAULT_BACKEND_PORT);
    }

    public int getHeatMapDensity() {
        return getInt(HEAT_MAP_DENSITY, DEFAULT_HEAT_MAP_DENSITY);
    }

    public int getThreshold(Sensor sensor, MeasurementLevel measurementLevel) {
        int defValue = sensor.getThreshold(measurementLevel);
        String key = thresholdKey(sensor, measurementLevel);
        return preferences.getInt(key, defValue);
    }

    public void setThreshold(Sensor sensor, MeasurementLevel measurementLevel, int value) {
        SharedPreferences.Editor editor = preferences.edit();

        String key = thresholdKey(sensor, measurementLevel);
        editor.putInt(key, value);

        editor.commit();
    }

    private String thresholdKey(Sensor sensor, MeasurementLevel level) {
        return sensor.getSensorName() + ";" + level;
    }

    public void setAuthToken(String token) {
        writeString(AUTH_TOKEN, token);
    }

    public String getAuthToken() {
        return preferences.getString(AUTH_TOKEN, "");
    }

    public void resetThresholds(Sensor sensor) {
        SharedPreferences.Editor editor = preferences.edit();

        for (MeasurementLevel level : MeasurementLevel.values()) {
            String key = thresholdKey(sensor, level);
            editor.remove(key);
        }

        editor.commit();
    }

    public void setUserLogin(String email) {
        writeString(USER_LOGIN, email);
    }

    public String getUserLogin() {
        return preferences.getString(USER_LOGIN, null);
    }

    public void setUserEmail(String email) {
        writeString(USER_EMAIL, email);
    }

    public String getUserEmail() {
        return preferences.getString(USER_EMAIL, null);
    }

    public void removeCredentials() {
        SharedPreferences.Editor editor = preferences.edit();

        editor.remove(AUTH_TOKEN);
        editor.remove(USER_LOGIN);
        editor.remove(USER_EMAIL);

        editor.commit();
    }

    public boolean hasCredentials() {
        return preferences.contains(AUTH_TOKEN);
    }

    public boolean hasNoCredentials() {
        return !hasCredentials();
    }

    public boolean isKeepScreenOn() {
        return preferences.getBoolean(KEEP_SCREEN_ON, DEFAULT_KEEP_SCREEN_ON);
    }

    public int getOffset60DB() {
        return getInt(OFFSET_60_DB, DEFAULT_OFFSET_60_DB);
    }

    public boolean validateOffset60DB(int newValue) {
        return validateRange(newValue, MIN_OFFSET_60_DB, MAX_OFFSET_60_DB);
    }

    private boolean validateRange(String key, int value, int min, int max) {
        if (value < min) {
            writeString(key, Integer.toString(min));
            return false;
        }
        if (value > max) {
            writeString(key, Integer.toString(max));
            return false;
        }

        return true;
    }

    private boolean validateRange(int value, int max, int min) {
        return value >= min && value <= max;
    }

    public boolean validateAveragingTime() {
        return validateRange(AVERAGING_TIME, getAveragingTime(), MIN_AVERAGING_TIME, MAX_AVERAGING_TIME);
    }

    public boolean isFirstLaunch() {
        return preferences.getBoolean(FIRST_LAUNCH, true);
    }

    public void setFirstLaunch(boolean value) {
        writeBoolean(FIRST_LAUNCH, value);
    }

    private boolean writeBoolean(String key, boolean value) {
        return preferences.edit().putBoolean(key, value).commit();
    }

    private void writeString(String key, String value) {
        preferences.edit().putString(key, value).commit();
    }

    public boolean validateFormat(String key) {
        if (isNumericPreference(key)) {
            try {
                Integer.parseInt(preferences.getString(key, "0"));
                return true;
            } catch (NumberFormatException e) {
                remove(key);
                return false;
            }
        }

        return true;
    }

    private void remove(String key) {
        preferences.edit().remove(key).commit();
    }

    private boolean isNumericPreference(String key) {
        return key.equals(CALIBRATION) ||
                key.equals(OFFSET_60_DB) ||
                key.equals(HEAT_MAP_DENSITY) ||
                key.equals(BACKEND_PORT) ||
                key.equals(SAMPLE_INTERVAL);
    }

    public int getAveragingTime() {
        return 1;
    }

    public boolean isAveraging() {
        return false;
    }

    public boolean isSyncOnlyWifi() {
        return preferences.getBoolean(SYNC_ONLY_WIFI, false);
    }

    public boolean isShowRoute() {
        return preferences.getBoolean(SHOW_ROUTE, true);
    }

    public void setBackendAddress(String backendAddress) {
        writeString(BACKEND, backendAddress);
    }

    public void setBackendPort(String backendAddress) {
        writeString(BACKEND_PORT, backendAddress);
    }

    public List<ExternalSensorDescriptor> knownSensors() {
        String json = getConnectedSensorsAsJSON();
        if (Strings.isNullOrEmpty(json)) {
            return newArrayList();
        }

        Type type = new TypeToken<List<ExternalSensorDescriptor>>() {
        }.getType();
        try {
            return gson.fromJson(json, type);
        } catch (JsonSyntaxException e) {
            writeString(SENSORS, "");
        }
        return newArrayList();
    }

    public void knownSensorsWithout(String addres) {
        List<ExternalSensorDescriptor> descriptors = knownSensors();
        for (Iterator<ExternalSensorDescriptor> iterator = descriptors.iterator(); iterator.hasNext(); ) {
            ExternalSensorDescriptor aDescriptor = iterator.next();
            if (addres.equals(aDescriptor.getAddress())) {
                iterator.remove();
            }
        }
        setExternalSensors(descriptors);
    }

    public String getConnectedSensorsAsJSON() {
        return preferences.getString(SENSORS, null);
    }

    public void setExternalSensors(List<ExternalSensorDescriptor> sensors) {
        String sensorsAsJson = gson.toJson(sensors);
        writeString(SENSORS, sensorsAsJson);
    }

    public boolean showGraphMetadata() {
        return preferences.getBoolean(SHOW_GRAPH_METADATA, false);
    }

    public void setDisableMaps(boolean checked) {
        writeBoolean(DISABLE_MAPS, checked);
    }

    public boolean areMapsDisabled() {
        return preferences.getBoolean(DISABLE_MAPS, false);
    }

    public void setContributeToCrowdmap(boolean checked) {
        writeBoolean(CONTRIBUTE_TO_CROWDMAP, checked);
    }

    public boolean isContributingToCrowdMap() {
        return preferences.getBoolean(CONTRIBUTE_TO_CROWDMAP, DEFAULT_CONTRIBUTE_TO_CROWDMAP);
    }
}
