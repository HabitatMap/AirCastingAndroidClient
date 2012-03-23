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
package pl.llp.aircasting.model;

import android.location.Location;
import android.location.LocationManager;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.event.session.NoteCreatedEvent;
import pl.llp.aircasting.helper.LocationHelper;

import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/20/11
 * Time: 1:17 PM
 */
@RunWith(InjectedTestRunner.class)
public class SessionManagerMakeANoteTest {
    @Inject SessionManager sessionManager;
    private Location location;
    private Date date;
    private SessionManager.Listener listener;

    @Before
    public void setup() {
        location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(50);
        location.setLongitude(20);

        sessionManager.locationHelper = mock(LocationHelper.class);
        when(sessionManager.locationHelper.getLastLocation()).thenReturn(location);
        sessionManager.eventBus = mock(EventBus.class);

        listener = mock(SessionManager.Listener.class);

        date = new Date();
    }

    @Test
    public void shouldStoreNotes() {
        Note expected = new Note(date, "Note text", location, "some file", 0);

        sessionManager.makeANote(date, "Note text", "some file");

        assertThat(sessionManager.getSession().getNotes(), hasItem(equalTo(expected)));
    }

    @Test
    public void shouldReturnTheCreatedNote() {
        Note expected = new Note(date, "Note text", location, "some file", 0);

        assertThat(sessionManager.makeANote(date, "Note text", "some file"), equalTo(expected));
    }

    @Test
    public void shouldNotifyListeners() {
        sessionManager.makeANote(date, "Note text", "some file");

        Note note = new Note(date, "Note text", location, "some file", 0);
        NoteCreatedEvent expected = new NoteCreatedEvent(note);
        verify(sessionManager.eventBus).post(Mockito.eq(expected));
    }

    @Test
    public void shouldNumberNotes() {
        Note expected1 = new Note(date, "first", location, null, 0);
        Note expected2 = new Note(date, "second", location, null, 1);

        sessionManager.makeANote(date, "first", null);
        sessionManager.makeANote(date, "second", null);

        assertThat(sessionManager.session.getNotes(), hasItem(expected1));
        assertThat(sessionManager.session.getNotes(), hasItem(expected2));
    }
}
