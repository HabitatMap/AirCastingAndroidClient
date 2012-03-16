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
package pl.llp.aircasting.helper;

import android.content.SharedPreferences;
import com.google.inject.Inject;
import pl.llp.aircasting.SoundLevel;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/10/11
 * Time: 12:59 PM
 */
public class SettingsHelper {
    public static final String AVERAGE_THRESHOLD = "average_threshold_int";
    public static final String LOUD_THRESHOLD = "loud_threshold_int";
    public static final String VERY_LOUD_THRESHOLD = "very_loud_threshold_int";
    public static final String TOO_LOUD_THRESHOLD = "too_loud_threshold";
    public static final String QUIET_THRESHOLD = "quiet_threshold";
    public static final String[] THRESHOLDS = new String[]{
            QUIET_THRESHOLD, AVERAGE_THRESHOLD, LOUD_THRESHOLD, VERY_LOUD_THRESHOLD, TOO_LOUD_THRESHOLD
    };

    public static final String AUTH_TOKEN = "auth_token";
    public static final String USER_LOGIN = "user_login";
    public static final String KEEP_SCREEN_ON = "keep_screen_on";
    public static final String OFFSET_60_DB = "offset_60_db";
    public static final String CALIBRATION = "calibration";
    public static final String SATELLITE = "satellite";
    public static final String BACKEND = "backend";
    public static final String BACKEND_PORT = "backend_port";
    public static final String HEAT_MAP_DENSITY = "heat_map_density";
    public static final String FIRST_LAUNCH = "first_launch";

    public static final int DEFAULT_CALIBRATION = 100;
    public static final boolean DEFAULT_SATELLITE = false;
    public static final String DEFAULT_BACKEND = "aircasting.herokuapp.com";
    public static final int DEFAULT_BACKEND_PORT = 80;
    public static final int DEFAULT_HEAT_MAP_DENSITY = 10;
    public static final boolean DEFAULT_KEEP_SCREEN_ON = false;
    public static final int DEFAULT_OFFSET_60_DB = 0;
    public static final int MIN_OFFSET_60_DB = -5;
    public static final int MAX_OFFSET_60_DB = 5;
    public static final int DEFAULT_QUIET_THRESHOLD = 20;
    public static final int DEFAULT_AVERAGE_THRESHOLD = 60;
    public static final int DEFAULT_LOUD_THRESHOLD = 70;
    public static final int DEFAULT_VERY_LOUD_THRESHOLD = 80;
    public static final int DEFAULT_TOO_LOUT_THRESHOLD = 100;
    public static final String SAMPLE_INTERVAL = "sample_interval";
    public static final int MIN_SAMPLE_INTERVAL = 1;
    public static final int MAX_SAMPLE_INTERVAL = 3600;
    public static final String AVERAGING_TIME = "averaging_time";
    private static final int MIN_AVERAGING_TIME = 1;
    private static final int MAX_AVERAGING_TIME = 3600;
    public static final String SYNC_ONLY_WIFI = "sync_only_wifi";
    public static final String SHOW_ROUTE = "show_route";
    public static final String SENSOR_ADDRESS = "sensor_address";

    @Inject SharedPreferences preferences;

    public int getCalibration() {
        return getInt(CALIBRATION, DEFAULT_CALIBRATION);
    }

    private int getInt(String key, int defaultValue) {
        String value = preferences.getString(key, "" + defaultValue);
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

    public int getThreshold(SoundLevel soundLevel) {
        switch (soundLevel) {
            case QUIET:
                return preferences.getInt(QUIET_THRESHOLD, DEFAULT_QUIET_THRESHOLD);
            case AVERAGE:
                return preferences.getInt(AVERAGE_THRESHOLD, DEFAULT_AVERAGE_THRESHOLD);
            case LOUD:
                return preferences.getInt(LOUD_THRESHOLD, DEFAULT_LOUD_THRESHOLD);
            case VERY_LOUD:
                return preferences.getInt(VERY_LOUD_THRESHOLD, DEFAULT_VERY_LOUD_THRESHOLD);
            case TOO_LOUD:
                return preferences.getInt(TOO_LOUD_THRESHOLD, DEFAULT_TOO_LOUT_THRESHOLD);
        }
        throw new RuntimeException("Could not provide a threshold for sound level " + soundLevel);
    }

    public void setThreshold(SoundLevel soundLevel, int value) {
        SharedPreferences.Editor editor = preferences.edit();

        switch (soundLevel) {
            case QUIET:
                editor.putInt(QUIET_THRESHOLD, value);
                break;
            case AVERAGE:
                editor.putInt(AVERAGE_THRESHOLD, value);
                break;
            case LOUD:
                editor.putInt(LOUD_THRESHOLD, value);
                break;
            case VERY_LOUD:
                editor.putInt(VERY_LOUD_THRESHOLD, value);
                break;
            case TOO_LOUD:
                editor.putInt(TOO_LOUD_THRESHOLD, value);
                break;
        }

        editor.commit();
    }

    public void setAuthToken(String token) {
        writeString(AUTH_TOKEN, token);
    }

    public String getAuthToken() {
        return preferences.getString(AUTH_TOKEN, "");
    }

    public void resetThresholds() {
        SharedPreferences.Editor editor = preferences.edit();

        for (String key : THRESHOLDS) {
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

    public void removeCredentials() {
        SharedPreferences.Editor editor = preferences.edit();

        editor.remove(AUTH_TOKEN);
        editor.remove(USER_LOGIN);

        editor.commit();
    }

    public boolean hasCredentials() {
        return preferences.contains(AUTH_TOKEN);
    }

    public boolean isKeepScreenOn() {
        return preferences.getBoolean(KEEP_SCREEN_ON, DEFAULT_KEEP_SCREEN_ON);
    }

    public int getOffset60DB() {
        return getInt(OFFSET_60_DB, DEFAULT_OFFSET_60_DB);
    }

    public boolean validateOffset60DB() {
        return validateRange(OFFSET_60_DB, getOffset60DB(), MIN_OFFSET_60_DB, MAX_OFFSET_60_DB);
    }

    private boolean validateRange(String key, int value, int min, int max) {
        if (value < min) {
            writeString(key, "" + min);
            return false;
        }
        if (value > max) {
            writeString(key, "" + max);
            return false;
        }

        return true;
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

    public void setSensorAddress(String address) {
        writeString(SENSOR_ADDRESS, address);
    }

    public String getSensorAddress() {
        return preferences.getString(SENSOR_ADDRESS, null);
    }
}
