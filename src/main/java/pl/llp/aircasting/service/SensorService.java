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
package pl.llp.aircasting.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;

import android.content.Intent;
import android.os.IBinder;
import com.google.inject.Inject;
import pl.llp.aircasting.model.CurrentSessionManager;
import pl.llp.aircasting.model.CurrentSessionSensorManager;
import pl.llp.aircasting.sensor.external.ExternalSensors;
import roboguice.service.RoboService;

public class SensorService extends RoboService {
    public static final int ACTIVE_SENSORS_ID = 2;

    @Inject CurrentSessionSensorManager mCurrentSessionSensorManager;
    @Inject CurrentSessionManager mCurrentSesssionManager;
    @Inject ExternalSensors mExternalSensors;

    private NotificationManager mNotificationManager;
    private boolean mServiceStarted = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        switch (Intents.getSensorServiceTask(intent)) {
            case Intents.START_SENSORS:
                if (!mServiceStarted) {
                    mCurrentSessionSensorManager.startSensors();
                    startSensorService();
                    mServiceStarted = true;
                }
                break;
            case Intents.STOP_SENSORS:
                if (mServiceStarted) {
                    mCurrentSessionSensorManager.stopSensors();
                    if (!mCurrentSesssionManager.isSessionRecording()) {
                        stopSelf();
                        mServiceStarted = false;
                    }
                }
                break;
            case Intents.DISCONNECT_SENSORS:
                mCurrentSessionSensorManager.stopSensors();
                mExternalSensors.disconnectAllSensors();
                mServiceStarted = false;
                break;
            case Intents.RESTART_SENSORS:
                mCurrentSessionSensorManager.restartSensors();
                startSensorService();
                mServiceStarted = true;
                break;
        }

        return START_STICKY;
    }

    private void startSensorService() {
        if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder builder = new Notification.Builder(this, createNotificationChannel(mNotificationManager))
                    .setSmallIcon(R.drawable.aircasting)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.sensors_are_active))
                    .setAutoCancel(true);

            Notification notification = builder.build();

            startForeground(ACTIVE_SENSORS_ID, notification);
        } else {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.aircasting)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.sensors_are_active))
                    .setAutoCancel(true);

            Notification notification = builder.build();

            startForeground(ACTIVE_SENSORS_ID, notification);
        }
    }

    @RequiresApi(26)
    private String createNotificationChannel(NotificationManager notificationManager) {
        String channelId = "sensor_service_channel_id";
        String channelName = "AirCasting Sensor Service";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
        channel.setImportance(NotificationManager.IMPORTANCE_HIGH);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        notificationManager.createNotificationChannel(channel);
        return channelId;
    }
}
