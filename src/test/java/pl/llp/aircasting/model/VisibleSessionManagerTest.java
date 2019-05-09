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
import pl.llp.aircasting.New;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionManager;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionSensorManager;
import pl.llp.aircasting.event.session.VisibleSessionUpdatedEvent;
import pl.llp.aircasting.screens.common.helpers.LocationHelper;
import pl.llp.aircasting.screens.common.sessionState.VisibleSession;
import pl.llp.aircasting.model.events.MeasurementEvent;
import pl.llp.aircasting.model.events.SensorEvent;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;
import pl.llp.aircasting.sensor.external.ExternalSensors;
import pl.llp.aircasting.storage.ProgressListener;
import pl.llp.aircasting.storage.repository.SessionRepository;

import android.location.Location;
import android.location.LocationManager;
import com.google.common.collect.Iterables;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.IsCollectionContaining.hasItem;
import static org.mockito.Mockito.*;

@RunWith(InjectedTestRunner.class)
public class VisibleSessionManagerTest
{
  @Inject
  CurrentSessionManager currentSessionManager;
  @Inject VisibleSession visibleSession;

  Location location;
  Sensor sensor;

  SensorEvent lastEvent;
  ProgressListener progressListener;

  private String title;
  private String tags;
  private String description;

  private void mockSensors()
  {
    currentSessionManager.locationHelper = mock(LocationHelper.class);
    currentSessionManager.audioReader = mock(SimpleAudioReader.class);
    currentSessionManager.eventBus = mock(EventBus.class);
    currentSessionManager.currentSessionSensorManager = mock(CurrentSessionSensorManager.class);

    sensor = mock(Sensor.class);
    when(sensor.isEnabled()).thenReturn(true);
    when(sensor.getSensorName()).thenReturn("LHC");

    when(currentSessionManager.locationHelper.getLastLocation()).thenReturn(location);
    when(currentSessionManager.currentSessionSensorManager.getSensorByName(Mockito.any(String.class))).thenReturn(sensor);
  }

  @Before
  public void setUp() throws Exception
  {
    location = new Location(LocationManager.GPS_PROVIDER);
    location.setLatitude(50);
    location.setLongitude(20);
    currentSessionManager.sessionRepository = mock(SessionRepository.class);

    mockSensors();
    progressListener = mock(ProgressListener.class);
  }

  private void triggerMeasurement(String name, double value)
  {

    lastEvent = New.sensorEvent(name, value);
    currentSessionManager.onEvent(lastEvent);
  }

  private void triggerMeasurement(double value)
  {
    triggerMeasurement("LHC", value);
  }

  private void triggerMeasurement()
  {
    triggerMeasurement(10);
  }

  @Test
  public void shouldCreateMeasurementStreams()
  {
    currentSessionManager.startMobileSession(title, tags, false);

    triggerMeasurement();

    MeasurementStream expected = lastEvent.stream();
    Collection<MeasurementStream> streams = currentSessionManager.getMeasurementStreams();

    assertThat(streams, hasItem(expected));
  }

  @Test
  public void shouldCreateOnlyOneStreamPerSensor()
  {
    currentSessionManager.startMobileSession(title, tags, false);

    triggerMeasurement();
    triggerMeasurement();

    assertThat(currentSessionManager.getMeasurementStreams().size(), equalTo(1));
  }

  @Test
  public void shouldCreateAStreamForEachSensor()
  {
    currentSessionManager.startMobileSession(title, tags, false);

    triggerMeasurement();
    SensorEvent event = new SensorEvent("CERN", "LHC2", "Siggh boson", "SB", "number", "#", 1, 2, 3, 4, 5, 10);
    currentSessionManager.onEvent(event);

    MeasurementStream expected = event.stream();
    assertThat(currentSessionManager.getMeasurementStreams(), hasItem(expected));
  }

  @Test
  public void shouldAllowAccessToAParticularStream()
  {
    currentSessionManager.startMobileSession(title, tags, false);

    triggerMeasurement();

    MeasurementStream expected = Iterables.getOnlyElement(currentSessionManager.getMeasurementStreams());
    assertThat(currentSessionManager.getMeasurementStream("LHC") == expected, equalTo(true));
  }

  @Test
  public void shouldStoreLastMeasurementForEachSensor()
  {
    triggerMeasurement("LHC", 150);
    triggerMeasurement("LHC2", 123);

    Sensor sensor2 = mock(Sensor.class);
    when(sensor2.getSensorName()).thenReturn("LHC2");

    assertThat(currentSessionManager.getNow(sensor), equalTo(150.0));
    assertThat(currentSessionManager.getNow(sensor2), equalTo(123.0));
  }

  @Test
  public void shouldAssumeLastMeasurementIsZeroByDefault()
  {
    assertThat(currentSessionManager.getNow(sensor), equalTo(0.0));
  }

  @Test
  public void shouldProvideAvgForEachStream()
  {
    MeasurementStream stream = mock(MeasurementStream.class);
    when(stream.getAvg()).thenReturn(10.0);
    String name = sensor.getSensorName();
    when(stream.getSensorName()).thenReturn(name);
    currentSessionManager.currentSession.add(stream);

    assertThat(visibleSession.getAvg(sensor), equalTo(10.0));
  }

  @Test
  public void shouldProvidePeakForEachStream()
  {
    MeasurementStream stream = mock(MeasurementStream.class);
    when(stream.getPeak()).thenReturn(11.0);
    String name = sensor.getSensorName();
    when(stream.getSensorName()).thenReturn(name);
    currentSessionManager.currentSession.add(stream);

    assertThat(visibleSession.getPeak(sensor), equalTo(11.0));
  }

  @Test
  public void shouldStoreMeasurements()
  {
    currentSessionManager.startMobileSession(title, tags, false);

    triggerMeasurement(22);

    Measurement expected = new Measurement(location.getLatitude(), location.getLongitude(), 22);
    org.fest.assertions.Assertions.assertThat(currentSessionManager.getMeasurementStream("LHC").getMeasurements())
                       .contains(expected);
  }

  @Test
  public void measurements_withoutLocation_should_get_a_fake()
  {
    currentSessionManager.startMobileSession(title, tags, false);
    currentSessionManager.currentSession.setLocationless(true);
    when(currentSessionManager.locationHelper.getLastLocation()).thenReturn(null);

    triggerMeasurement();

    assertThat(currentSessionManager.getMeasurementStreams().isEmpty(), equalTo(false));
    assertThat(currentSessionManager.getCurrentSession().isLocationless(), equalTo(true));
  }

  @Test
  public void shouldSkipMeasurementsFromDisabledStreams()
  {
    currentSessionManager.state.recording().startRecording();
    when(sensor.isEnabled()).thenReturn(false);

    triggerMeasurement();

    assertThat(currentSessionManager.getMeasurementStreams().isEmpty(), equalTo(true));
  }

  @Test
  public void shouldNotifyListenersAboutMeasurements()
  {
    triggerMeasurement(11);

    verify(currentSessionManager.eventBus).post(Mockito.any(MeasurementEvent.class));
  }

  @Test
  public void shouldStartSensors()
  {
    currentSessionManager.currentSessionSensorManager.startSensors();

    verify(currentSessionManager.locationHelper).start();
    verify(currentSessionManager.audioReader).start();
  }

  @Test
  public void shouldOnlyStartSensorsOnce()
  {
    currentSessionManager.currentSessionSensorManager.startSensors();
    currentSessionManager.currentSessionSensorManager.startSensors();

    verify(currentSessionManager.locationHelper, atMost(1)).start();
    verify(currentSessionManager.audioReader, atMost(1)).start();
  }

  @Test
  public void shouldStopSensors()
  {
    currentSessionManager.currentSessionSensorManager.startSensors();
    currentSessionManager.currentSessionSensorManager.stopSensors();

    verify(currentSessionManager.audioReader).stop();
    verify(currentSessionManager.locationHelper).stop();
    assertThat(currentSessionManager.isSessionRecording(), equalTo(false));
  }

  @Test
  public void shouldNotStopSensorsDuringASession()
  {
    currentSessionManager.startMobileSession(title, tags, false);
    currentSessionManager.currentSessionSensorManager.stopSensors();

    verify(currentSessionManager.locationHelper, never()).stop();
    verify(currentSessionManager.audioReader, never()).stop();
    assertThat(currentSessionManager.isSessionRecording(), equalTo(true));
  }

  @Test
  public void shouldStartASession()
  {
    currentSessionManager.startMobileSession(title, tags, false);

    verify(currentSessionManager.locationHelper, times(2)).start();
    verify(currentSessionManager.audioReader).start();
    assertThat(currentSessionManager.isSessionRecording(), equalTo(true));
  }

  @Test
  public void shouldDiscardASession()
  {
    currentSessionManager.startMobileSession(title, tags, false);
    currentSessionManager.getCurrentSession().setId(1234);

    triggerMeasurement(13.5);
    currentSessionManager.discardSession();

    verify(currentSessionManager.audioReader, never()).stop();
    verify(currentSessionManager.locationHelper).stop();
    verify(currentSessionManager.sessionRepository, never()).save(Mockito.any(Session.class));
    assertThat(currentSessionManager.getMeasurementStreams().isEmpty(), equalTo(true));
    assertThat(currentSessionManager.isSessionRecording(), equalTo(false));
  }

  @Ignore("Fix session persistence")
  @Test
  public void shouldStopASession()
  {
    triggerMeasurement(11);
    currentSessionManager.finishSession(0, true);

    verify(currentSessionManager.audioReader, never()).stop();
    verify(currentSessionManager.locationHelper).stop();
    verify(currentSessionManager.sessionRepository).save(Mockito.any(Session.class));
    assertThat(currentSessionManager.isSessionRecording(), equalTo(false));
  }

  @Test
  public void shouldNotifyListenersOnSessionClobber()
  {
    currentSessionManager.getCurrentSession().setId(1234);
    currentSessionManager.discardSession();

    verify(currentSessionManager.eventBus).post(Mockito.any(VisibleSessionUpdatedEvent.class));
  }

  @Test
  public void shouldNotAddMeasurementsToASavedSession()
  {
    currentSessionManager.currentSession = new Session();

    triggerMeasurement(10);

    assertThat(currentSessionManager.getMeasurementStreams().isEmpty(), equalTo(true));
  }

  @Test
  public void shouldSetSessionStart()
  {
    currentSessionManager.startMobileSession(title, tags, false);

    int oneSecond = 1000;
    assertThat(new Date().getTime() - currentSessionManager.currentSession.getStart().getTime() < oneSecond, equalTo(true));
  }

  @Test
  public void startSession_should_be_indicated_in_recording_state() throws Exception
  {
    // given

    // when
    currentSessionManager.startMobileSession(title, tags, false);

    // then
    org.fest.assertions.Assertions.assertThat(currentSessionManager.state.recording().isRecording()).isTrue();
  }

  @Test
  public void stopSession_should_changeRecordingState() throws Exception
  {
      // given
      currentSessionManager.startMobileSession(title, tags, false);

      // when
      currentSessionManager.stopSession();

      // then
    org.fest.assertions.Assertions.assertThat(currentSessionManager.state.recording().isRecording()).isFalse();
  }

  @Ignore("needs to check if finishing creates a proper task")
  @Test
  public void shouldSetSessionEnd()
  {
    currentSessionManager.startMobileSession(title, tags, false);
    triggerMeasurement();

    currentSessionManager.finishSession(0, true);

    verify(currentSessionManager.sessionRepository).save(Mockito.argThat(new BaseMatcher<Session>()
    {
      @Override
      public boolean matches(Object o)
      {
        Session other = (Session) o;
        long oneSecond = 1000;
        return new Date().getTime() - other.getEnd().getTime() < oneSecond;
      }

      @Override
      public void describeTo(Description description)
      {
        description.appendText("Session with end set");
      }
    }));
  }

  @Ignore("needs to check if finishing creates a proper task")
  @Test
  public void shouldSaveAdditionalData()
  {
    triggerMeasurement(100);
    currentSessionManager.finishSession(0, true);

    verify(currentSessionManager.sessionRepository).save(Mockito.argThat(new BaseMatcher<Session>()
    {
      @Override
      public boolean matches(Object o)
      {
        Session session = (Session) o;
        return session.getCalibration() == 123;
      }

      @Override
      public void describeTo(Description description)
      {
        description.appendText("Session with additional data set");
      }
    }));
  }

  @Test
  public void shouldJustDeleteNotesIfSessionInProgress()
  {
    currentSessionManager.startMobileSession(title, tags, false);
    Note note = currentSessionManager.makeANote(null, null, null);

    currentSessionManager.deleteNote(note);

    assertThat(visibleSession.getSessionNotes(), not(hasItem(note)));
  }

  @Test
  public void afterDeletingNotesShouldHaveNewNumbers()
  {
    currentSessionManager.startMobileSession(title, tags, false);
    Note note1 = currentSessionManager.makeANote(null, "Note1", null);
    Note note2 = currentSessionManager.makeANote(null, "Note2", null);

    currentSessionManager.deleteNote(note1);
    Note note3 = currentSessionManager.makeANote(null, "Note3", null);

    assertThat(note3.getNumber(), not(equalTo(note2.getNumber())));
  }

  @Test
  public void shouldMarkNotesToBeDeletedForSavedSessions()
  {
    currentSessionManager.currentSession = mock(Session.class);
    when(currentSessionManager.currentSession.getId()).thenReturn(1234L);
    Note note = new Note(null, null, location, null, 10);

    currentSessionManager.deleteNote(note);

    verify(currentSessionManager.currentSession).deleteNote(note);
  }

  @Test
  public void shouldRestartExternalSensor()
  {
    currentSessionManager.currentSessionSensorManager.restartSensors();
  }
}
