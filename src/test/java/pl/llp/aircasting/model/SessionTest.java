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

import com.google.gson.Gson;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.InjectedTestRunner;

import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(InjectedTestRunner.class)
public class SessionTest {
    @Inject Gson gson;
    
    Session session = new Session();
    Date date = new Date();
    Session emptySession = new Session();
    Note note = new Note();

    @Before
    public void setup() {
        session.add(note);
    }

    @Test
    public void shouldAllowSettingStart() {
        emptySession.setStart(date);
        assertThat(emptySession.getStart(), equalTo(date));
    }

    @Test
    public void shouldAllowSettingEnd() {
        emptySession.setEnd(date);
        assertThat(emptySession.getEnd(), equalTo(date));
    }

    @Test
    public void shouldHaveUUID() {
        assertThat(session.getUUID(), not(equalTo(null)));
        assertThat(session.getUUID(), not(equalTo(new Session().getUUID())));
    }

    @Test
    public void shouldAlwaysHaveMeasurements() {
        session.measurements = null;

        assertThat(session.getMeasurements(), not(equalTo(null)));
    }

    @Test
    public void should_be_empty_if_all_streams_are_empty() {
        MeasurementStream stream1 = mock(MeasurementStream.class);
        when(stream1.isEmpty()).thenReturn(true);
        MeasurementStream stream2 = mock(MeasurementStream.class);
        when(stream2.isEmpty()).thenReturn(true);
        session.streams.put("s1", stream1);
        session.streams.put("s2", stream2);
        
        assertThat(session.isEmpty(), equalTo(true));
    }

    @Test
    public void should_not_be_empty_if_any_of_the_streams_are_not() {
        MeasurementStream stream1 = mock(MeasurementStream.class);
        when(stream1.isEmpty()).thenReturn(false);
        MeasurementStream stream2 = mock(MeasurementStream.class);
        when(stream2.isEmpty()).thenReturn(true);
        session.streams.put("s1", stream1);
        session.streams.put("s2", stream2);

        assertThat(session.isEmpty(), equalTo(false));
    }
}
