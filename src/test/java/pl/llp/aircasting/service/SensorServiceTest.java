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

import android.content.Context;
import android.content.Intent;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.model.CurrentSessionManager;

import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/31/11
 * Time: 2:10 PM
 */
@RunWith(InjectedTestRunner.class)
public class SensorServiceTest {
    @Inject SensorService sensorService;
    Intent intent;

    @Before
    public void setup() {
        sensorService.mCurrentSesssionManager = mock(CurrentSessionManager.class);
        intent = new Intent(mock(Context.class), SensorService.class);
    }

    @Test
    public void shouldStartMeasurements() {
        intent.putExtra(Intents.SESSION_SERVICE_TASK, Intents.START_SENSORS);

        sensorService.onStartCommand(intent, 0, 0);

        verify(sensorService.mCurrentSessionSensorManager).startSensors();
    }

    @Test
    public void shouldStopMeasurementsWithoutASession() {
        intent.putExtra(Intents.SESSION_SERVICE_TASK, Intents.STOP_SENSORS);
        when(sensorService.mCurrentSesssionManager.isSessionRecording()).thenReturn(false);

        sensorService.onStartCommand(intent, 0, 0);

        verify(sensorService.mCurrentSessionSensorManager).stopSensors();
    }

    @Test
    public void shouldStopSelfWithoutASession() {
        intent.putExtra(Intents.SESSION_SERVICE_TASK, Intents.STOP_SENSORS);
        when(sensorService.mCurrentSesssionManager.isSessionRecording()).thenReturn(false);
        sensorService = spy(sensorService);

        sensorService.onStartCommand(intent, 0, 0);

        verify(sensorService).stopSelf();
    }
}
