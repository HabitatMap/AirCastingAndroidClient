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

import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.storage.repository.SessionRepository;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/20/11
 * Time: 11:45 AM
 */
@RunWith(InjectedTestRunner.class)
public class SessionManagerUpdateSessionTest {
    @Inject SessionManager sessionManager;
    private Session session;

    @Before
    public void setup(){
        sessionManager.sessionRepository = mock(SessionRepository.class);
        sessionManager.session = new Session();
        sessionManager.session.add(new MeasurementStream());
        session = new Session();
        session.setTitle("New title");
    }

    @Test
    public void shouldUpdateTheSessionHeader(){
        sessionManager.updateSession(session);

        assertThat(sessionManager.session.getTitle(), equalTo("New title"));
    }

    @Test
    public void shouldKeepMeasurements(){
        sessionManager.updateSession(session);

        assertThat(sessionManager.getMeasurementStreams().size(), equalTo(1));
    }

    @Test
    public void shouldSaveTheSession(){
        sessionManager.updateSession(session);
        
        verify(sessionManager.sessionRepository).update(sessionManager.session);
    }
}
