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

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionManager;
import pl.llp.aircasting.storage.repository.SessionRepository;
import pl.llp.aircasting.tracking.ContinuousTracker;

import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(InjectedTestRunner.class)
public class SessionManagerUpdateVisibleSessionTest
{
  public static final int ANY_ID = 3;
  public static final String NEW_TITLE = "New title";

  @Inject
  CurrentSessionManager currentSessionManager;
  @Inject ContinuousTracker tracker;
  private Session session;

  @Before
  public void setup()
  {
    currentSessionManager.sessionRepository = mock(SessionRepository.class);
    currentSessionManager.currentSession = new Session();
    currentSessionManager.currentSession.add(new MeasurementStream());
    session = new Session();
    session.setTitle(NEW_TITLE);
    session.setId(ANY_ID);

    tracker = spy(tracker);
    currentSessionManager.tracker = tracker;
  }

  @Test
  public void shouldKeepMeasurements()
  {
    currentSessionManager.updateSession(session);

    assertThat(currentSessionManager.getMeasurementStreams().size(), equalTo(1));
  }

  @Test
  public void shouldSaveTheSession()
  {
    currentSessionManager.updateSession(session);

    verify(tracker).setTitle(anyLong(), anyString());
  }
}
