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

import android.content.Intent;
import android.os.IBinder;
import com.google.inject.Inject;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.model.SessionManager;
import roboguice.service.RoboService;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/31/11
 * Time: 1:47 PM
 */
public class SensorService extends RoboService {
    @Inject SessionManager sessionManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        switch (Intents.getSessionServiceTask(intent)) {
            case Intents.START_SENSORS:
                sessionManager.startSensors();
                break;
            case Intents.STOP_SENSORS:
                sessionManager.stopSensors();
                if(!sessionManager.isSessionStarted()){
                    stopSelf();
                }
                break;
        }

        return START_STICKY;
    }
}
