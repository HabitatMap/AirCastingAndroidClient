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
package pl.llp.aircasting;

import android.content.Context;
import android.content.Intent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import pl.llp.aircasting.activity.SoundTraceActivity;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.SoundMeasurement;
import pl.llp.aircasting.service.SensorService;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/24/11
 * Time: 10:51 AM
 */
@RunWith(InjectedTestRunner.class)
public class IntentsTest {
    Session session;

    @Before
    public void setup() {
        session = new Session();
        session.add(new SoundMeasurement());
        session.add(new Note());
    }

    @Test
    public void shouldStartEditSession() {
        SoundTraceActivity activity = mock(SoundTraceActivity.class);

        Intents.editSession(activity, session);

        verify(activity).startActivityForResult(Mockito.any(Intent.class), eq(Intents.EDIT_SESSION));
    }

    @Test
    public void shouldStartMeasurements() {
        Context context = mock(Context.class);
        Intent expected = new Intent(context, SensorService.class);
        expected.putExtra(Intents.SESSION_SERVICE_TASK, Intents.START_SENSORS);

        Intents.startSensors(context);

        verify(context).startService(eq(expected));
    }

    @Test
    public void shouldStopMeasurements() {
        Context context = mock(Context.class);
        Intent expected = new Intent(context, SensorService.class);
        expected.putExtra(Intents.SESSION_SERVICE_TASK, Intents.STOP_SENSORS);

        Intents.stopSensors(context);

        verify(context).startService(eq(expected));
    }

    @Test
    public void shouldExtractSessionServiceTask() {
        Intent intent = new Intent(mock(Context.class), SensorService.class);
        intent.putExtra(Intents.SESSION_SERVICE_TASK, Intents.STOP_SENSORS);
        
        assertThat(Intents.getSessionServiceTask(intent), equalTo(Intents.STOP_SENSORS));
    }
}
