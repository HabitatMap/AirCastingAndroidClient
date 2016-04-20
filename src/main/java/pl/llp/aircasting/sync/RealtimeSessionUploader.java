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
package pl.llp.aircasting.sync;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.R;
import pl.llp.aircasting.api.RealtimeSessionDriver;
import pl.llp.aircasting.api.data.CreateSessionResponse;
import pl.llp.aircasting.api.data.CreateRealtimeMeasurementResponse;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.RealtimeSession;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.events.RealtimeMeasurementEvent;
import pl.llp.aircasting.util.http.HttpResult;
import pl.llp.aircasting.util.http.Status;
import roboguice.inject.InjectResource;
import com.google.common.eventbus.Subscribe;

@Singleton
public class RealtimeSessionUploader
{
  @Inject ConnectivityManager connectivityManager;
  @Inject SettingsHelper settingsHelper;
  @Inject Context context;
  @Inject RealtimeSessionDriver realtimeSessionDriver;
  @Inject EventBus eventBus;

  @InjectResource(R.string.session_creation_failed) String session_creation_failed;

  @Inject
  public void init() {
    eventBus.register(this);
  }

  public boolean create(Session session) {
    try {
      if (canUpload()) {
        performCreateSession(session);
      } else {
        Toast.makeText(context, session_creation_failed, Toast.LENGTH_LONG).show();
        return (false);
      }
    } catch (SessionSyncException exception)
    {
      Toast.makeText(context, session_creation_failed, Toast.LENGTH_LONG).show();
      return (false);
    }

    return (true);
  }

  private void performCreateSession(Session session) throws SessionSyncException
  {
    HttpResult<CreateSessionResponse> result = realtimeSessionDriver.create(session);

    Status status = result.getStatus();
    if(status == Status.ERROR || status == Status.FAILURE)
      throw new SessionSyncException("Session creation failed");
    CreateSessionResponse createSessionResponse = result.getContent();
  }

  @Subscribe
  public void onEvent(RealtimeMeasurementEvent event) {
    if (canUpload()) {
      performCreateMeasurement(event.getRealtimeSession());
    }
  }

  private void performCreateMeasurement(RealtimeSession realtimeSession)
  {
    HttpResult<CreateRealtimeMeasurementResponse> result = realtimeSessionDriver.create(realtimeSession);
  }

  private boolean canUpload() {
    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

    return networkInfo != null
        && networkInfo.isConnected()
        && (!settingsHelper.isSyncOnlyWifi() || networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
        && settingsHelper.hasCredentials();
  }
}
