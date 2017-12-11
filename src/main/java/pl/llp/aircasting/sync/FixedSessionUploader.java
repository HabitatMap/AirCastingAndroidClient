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
package pl.llp.aircasting.sync;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.R;
import pl.llp.aircasting.api.FixedSessionDriver;
import pl.llp.aircasting.api.data.CreateSessionResponse;
import pl.llp.aircasting.api.data.CreateFixedSessionsMeasurementResponse;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.FixedSessionsMeasurement;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.events.FixedSessionsMeasurementEvent;
import pl.llp.aircasting.util.http.HttpResult;
import pl.llp.aircasting.util.http.Status;
import roboguice.inject.InjectResource;
import com.google.common.eventbus.Subscribe;

@Singleton
public class FixedSessionUploader {
    @Inject ConnectivityManager connectivityManager;
    @Inject SettingsHelper settingsHelper;
    @Inject Context context;
    @Inject FixedSessionDriver fixedSessionDriver;
    @Inject EventBus eventBus;

    @InjectResource(R.string.fixed_session_creation_failed)
    String fixed_session_creation_failed;

    @Inject
    public void init() {
        eventBus.register(this);
    }

    public boolean create(Session session) {
        try {
            if (canUpload()) {
//        enableStrictMode();
                performCreateSession(session);
                return (true);
            } else {
                Toast.makeText(context, fixed_session_creation_failed, Toast.LENGTH_LONG).show();
                return (false);
            }
        } catch (SessionSyncException exception) {
            Toast.makeText(context, fixed_session_creation_failed, Toast.LENGTH_LONG).show();
            return (false);
        }
    }

    // This is BAD but easy workaround to the fixed sessions creation error, use only for dev purposes.
    // It enables doing network operations in the main thread of the app. It should be done in a background task.
    // TODO:
    // Fix this later
    public void enableStrictMode() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
    }

    private void performCreateSession(Session session) throws SessionSyncException {
        HttpResult<CreateSessionResponse> result = fixedSessionDriver.create(session);

        Status status = result.getStatus();
        if (status == Status.ERROR || status == Status.FAILURE)
            throw new SessionSyncException("Session creation failed");
        CreateSessionResponse createSessionResponse = result.getContent();
    }

    @Subscribe
    public void onEvent(FixedSessionsMeasurementEvent event) {
        if (canUpload()) {
            performCreateMeasurement(event.getFixedSessionsMeasurement());
        }
    }

    private void performCreateMeasurement(FixedSessionsMeasurement fixedSessionsMeasurement) {
        HttpResult<CreateFixedSessionsMeasurementResponse> result = fixedSessionDriver.create_measurement(fixedSessionsMeasurement);
    }

    private boolean canUpload() {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null
                && networkInfo.isConnected()
                && (!settingsHelper.isSyncOnlyWifi() || networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
                && settingsHelper.hasCredentials();
    }
}
