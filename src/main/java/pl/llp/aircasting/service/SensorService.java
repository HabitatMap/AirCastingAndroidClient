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
package pl.llp.aircasting.service;

import android.app.Notification;
import android.support.v4.app.NotificationCompat;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.model.SessionManager;

import android.content.Intent;
import android.os.IBinder;
import com.google.inject.Inject;
import roboguice.service.RoboService;

public class SensorService extends RoboService
{
  public static final int ACTIVE_SENSORS_ID = 2;

  @Inject SessionManager sessionManager;

  @Override
  public IBinder onBind(Intent intent)
  {
    return null;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId)
  {
    super.onStartCommand(intent, flags, startId);

    switch (Intents.getSensorServiceTask(intent))
    {
      case Intents.START_SENSORS:
        sessionManager.startSensors();
        break;
      case Intents.STOP_SENSORS:
        sessionManager.stopSensors();
        if (!sessionManager.isSessionStarted())
        {
          stopSelf();
        }
        break;
      case Intents.RESTART_SENSORS:
        sessionManager.restartSensors();
        break;
    }

      NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
              .setSmallIcon(R.drawable.aircasting)
              .setContentTitle(getString(R.string.app_name))
              .setContentText(getString(R.string.sensors_are_active))
              .setAutoCancel(true);

      Notification notification = builder.build();
      startForeground(ACTIVE_SENSORS_ID, notification);

    return START_STICKY;
  }
}
