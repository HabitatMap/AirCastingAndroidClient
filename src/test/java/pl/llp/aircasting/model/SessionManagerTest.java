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
import pl.llp.aircasting.activity.events.SessionChangeEvent;
import pl.llp.aircasting.helper.LocationHelper;
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
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.util.Collection;
import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.IsCollectionContaining.hasItem;
import static org.mockito.Mockito.*;

@RunWith(InjectedTestRunner.class)
public class SessionManagerTest
{
  @Inject
  SessionManager sessionManager;

  Location location;
  Sensor sensor;

  SensorEvent lastEvent;
  ProgressListener progressListener;

  private String title;
  private String tags;
  private String description;

  private void mockSensors()
  {
    sessionManager.locationHelper = mock(LocationHelper.class);
    sessionManager.audioReader = mock(SimpleAudioReader.class);
    sessionManager.externalSensors = mock(ExternalSensors.class);
    sessionManager.eventBus = mock(EventBus.class);
    sessionManager.sensorManager = mock(SensorManager.class);

    sensor = mock(Sensor.class);
    when(sensor.isEnabled()).thenReturn(true);
    when(sensor.getSensorName()).thenReturn("LHC");

    when(sessionManager.locationHelper.getLastLocation()).thenReturn(location);
    when(sessionManager.sensorManager.getSensorByName(Mockito.any(String.class))).thenReturn(sensor);
  }

  @Before
  public void setUp() throws Exception
  {
    location = new Location(LocationManager.GPS_PROVIDER);
    location.setLatitude(50);
    location.setLongitude(20);
    sessionManager.sessionRepository = mock(SessionRepository.class);

    mockSensors();
    progressListener = mock(ProgressListener.class);
  }

  private void triggerMeasurement(String name, double value)
  {

    lastEvent = New.sensorEvent(name, value);
    sessionManager.onEvent(lastEvent);
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
    sessionManager.startMobileSession(title, tags, description, false);

    triggerMeasurement();

    MeasurementStream expected = lastEvent.stream();
    Collection<MeasurementStream> streams = sessionManager.getMeasurementStreams();

    assertThat(streams, hasItem(expected));
  }

  @Test
  public void shouldCreateOnlyOneStreamPerSensor()
  {
    sessionManager.startMobileSession(title, tags, description, false);

    triggerMeasurement();
    triggerMeasurement();

    assertThat(sessionManager.getMeasurementStreams().size(), equalTo(1));
  }

  @Test
  public void shouldCreateAStreamForEachSensor()
  {
    sessionManager.startMobileSession(title, tags, description, false);

    triggerMeasurement();
    SensorEvent event = new SensorEvent("CERN", "LHC2", "Siggh boson", "SB", "number", "#", 1, 2, 3, 4, 5, 10);
    sessionManager.onEvent(event);

    MeasurementStream expected = event.stream();
    assertThat(sessionManager.getMeasurementStreams(), hasItem(expected));
  }

  @Test
  public void shouldAllowAccessToAParticularStream()
  {
    sessionManager.startMobileSession(title, tags, description, false);

    triggerMeasurement();

    MeasurementStream expected = Iterables.getOnlyElement(sessionManager.getMeasurementStreams());
    assertThat(sessionManager.getMeasurementStream("LHC") == expected, equalTo(true));
  }

  @Test
  public void shouldStoreLastMeasurementForEachSensor()
  {
    triggerMeasurement("LHC", 150);
    triggerMeasurement("LHC2", 123);

    Sensor sensor2 = mock(Sensor.class);
    when(sensor2.getSensorName()).thenReturn("LHC2");

    assertThat(sessionManager.getNow(sensor), equalTo(150.0));
    assertThat(sessionManager.getNow(sensor2), equalTo(123.0));
  }

  @Test
  public void shouldAssumeLastMeasurementIsZeroByDefault()
  {
    assertThat(sessionManager.getNow(sensor), equalTo(0.0));
  }

  @Test
  public void shouldProvideAvgForEachStream()
  {
    MeasurementStream stream = mock(MeasurementStream.class);
    when(stream.getAvg()).thenReturn(10.0);
    String name = sensor.getSensorName();
    when(stream.getSensorName()).thenReturn(name);
    sessionManager.currentSession.add(stream);

    assertThat(sessionManager.getAvg(sensor), equalTo(10.0));
  }

  @Test
  public void shouldProvidePeakForEachStream()
  {
    MeasurementStream stream = mock(MeasurementStream.class);
    when(stream.getPeak()).thenReturn(11.0);
    String name = sensor.getSensorName();
    when(stream.getSensorName()).thenReturn(name);
    sessionManager.currentSession.add(stream);

    assertThat(sessionManager.getPeak(sensor), equalTo(11.0));
  }

  @Test
  public void shouldStoreMeasurements()
  {
    sessionManager.startMobileSession(title, tags, description, false);

    triggerMeasurement(22);

    Measurement expected = new Measurement(location.getLatitude(), location.getLongitude(), 22);
    org.fest.assertions.Assertions.assertThat(sessionManager.getMeasurementStream("LHC").getMeasurements())
                       .contains(expected);
  }

  @Test
  public void measurements_withoutLocation_should_get_a_fake()
  {
    sessionManager.startMobileSession(title, tags, description, false);
    sessionManager.currentSession.setLocationless(true);
    when(sessionManager.locationHelper.getLastLocation()).thenReturn(null);

    triggerMeasurement();

    assertThat(sessionManager.getMeasurementStreams().isEmpty(), equalTo(false));
    assertThat(sessionManager.getCurrentSession().isLocationless(), equalTo(true));
  }

  @Test
  public void shouldSkipMeasurementsFromDisabledStreams()
  {
    sessionManager.state.recording().startRecording();
    when(sensor.isEnabled()).thenReturn(false);

    triggerMeasurement();

    assertThat(sessionManager.getMeasurementStreams().isEmpty(), equalTo(true));
  }

  @Test
  public void shouldNotifyListenersAboutMeasurements()
  {
    triggerMeasurement(11);

    verify(sessionManager.eventBus).post(Mockito.any(MeasurementEvent.class));
  }

  @Test
  public void shouldStartSensors()
  {
    sessionManager.startSensors();

    verify(sessionManager.locationHelper).start();
    verify(sessionManager.audioReader).start();
    verify(sessionManager.externalSensors).start();
  }

  @Test
  public void shouldOnlyStartSensorsOnce()
  {
    sessionManager.startSensors();
    sessionManager.startSensors();

    verify(sessionManager.locationHelper, atMost(1)).start();
    verify(sessionManager.audioReader, atMost(1)).start();
  }

  @Test
  public void shouldStopSensors()
  {
    sessionManager.startSensors();
    sessionManager.stopSensors();

    verify(sessionManager.audioReader).stop();
    verify(sessionManager.locationHelper).stop();
    assertThat(sessionManager.isSessionRecording(), equalTo(false));
  }

  @Test
  public void shouldNotStopSensorsDuringASession()
  {
    sessionManager.startMobileSession(title, tags, description, false);
    sessionManager.stopSensors();

    verify(sessionManager.locationHelper, never()).stop();
    verify(sessionManager.audioReader, never()).stop();
    assertThat(sessionManager.isSessionRecording(), equalTo(true));
  }

  @Test
  public void shouldStartASession()
  {
    sessionManager.startMobileSession(title, tags, description, false);

    verify(sessionManager.locationHelper, times(2)).start();
    verify(sessionManager.audioReader).start();
    assertThat(sessionManager.isSessionRecording(), equalTo(true));
  }

  @Test
  public void shouldDiscardASession()
  {
    sessionManager.startMobileSession(title, tags, description, false);
    sessionManager.getCurrentSession().setId(1234);

    triggerMeasurement(13.5);
    sessionManager.discardSession();

    verify(sessionManager.audioReader, never()).stop();
    verify(sessionManager.locationHelper).stop();
    verify(sessionManager.sessionRepository, never()).save(Mockito.any(Session.class));
    assertThat(sessionManager.getMeasurementStreams().isEmpty(), equalTo(true));
    assertThat(sessionManager.isSessionRecording(), equalTo(false));
  }

  @Ignore("Fix session persistence")
  @Test
  public void shouldStopASession()
  {
    triggerMeasurement(11);
    sessionManager.finishSession(0);

    verify(sessionManager.audioReader, never()).stop();
    verify(sessionManager.locationHelper).stop();
    verify(sessionManager.sessionRepository).save(Mockito.any(Session.class));
    assertThat(sessionManager.isSessionRecording(), equalTo(false));
  }

  @Test
  public void shouldNotifyListenersOnSessionClobber()
  {
    sessionManager.getCurrentSession().setId(1234);
    sessionManager.discardSession();

    verify(sessionManager.eventBus).post(Mockito.any(SessionChangeEvent.class));
  }

  @Test
  public void isCalibrating()
  {
    sessionManager = spy(sessionManager);
    doReturn(new Session()).when(sessionManager.sessionRepository)
        .loadFully(anyLong(), Matchers.<ProgressListener>anyObject()); 

    sessionManager.loadSessionForViewing(0, progressListener);

    verify(sessionManager, atLeastOnce()).setSession(any(Session.class));
  }

  @Test
  public void shouldNotAddMeasurementsToASavedSession()
  {
    sessionManager.currentSession = new Session();

    triggerMeasurement(10);

    assertThat(sessionManager.getMeasurementStreams().isEmpty(), equalTo(true));
  }

  @Test
  public void shouldSetSessionStart()
  {
    sessionManager.startMobileSession(title, tags, description, false);

    int oneSecond = 1000;
    assertThat(new Date().getTime() - sessionManager.currentSession.getStart().getTime() < oneSecond, equalTo(true));
  }

  @Test
  public void startSession_should_be_indicated_in_recording_state() throws Exception
  {
    // given

    // when
    sessionManager.startMobileSession(title, tags, description, false);

    // then
    org.fest.assertions.Assertions.assertThat(sessionManager.state.recording().isRecording()).isTrue();
  }

  @Test
  public void stopSession_should_changeRecordingState() throws Exception
  {
      // given
      sessionManager.startMobileSession(title, tags, description, false);

      // when
      sessionManager.stopSession();

      // then
    org.fest.assertions.Assertions.assertThat(sessionManager.state.recording().isRecording()).isFalse();
  }

  @Ignore("needs to check if finishing creates a proper task")
  @Test
  public void shouldSetSessionEnd()
  {
    sessionManager.startMobileSession(title, tags, description, false);
    triggerMeasurement();

    sessionManager.finishSession(0);

    verify(sessionManager.sessionRepository).save(Mockito.argThat(new BaseMatcher<Session>()
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
    sessionManager.finishSession(0);

    verify(sessionManager.sessionRepository).save(Mockito.argThat(new BaseMatcher<Session>()
    {
      @Override
      public boolean matches(Object o)
      {
        Session session = (Session) o;
        return session.getCalibration() == 123
            && session.getOffset60DB() == 432
            && "1.1.1".equals(session.getOSVersion())
            && "very old".equals(session.getPhoneModel());
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
    sessionManager.startMobileSession(title, tags, description, false);
    Note note = sessionManager.makeANote(null, null, null);

    sessionManager.deleteNote(note);

    assertThat(sessionManager.getNotes(), not(hasItem(note)));
  }

  @Test
  public void afterDeletingNotesShouldHaveNewNumbers()
  {
    sessionManager.startMobileSession(title, tags, description, false);
    Note note1 = sessionManager.makeANote(null, "Note1", null);
    Note note2 = sessionManager.makeANote(null, "Note2", null);

    sessionManager.deleteNote(note1);
    Note note3 = sessionManager.makeANote(null, "Note3", null);

    assertThat(note3.getNumber(), not(equalTo(note2.getNumber())));
  }

  @Test
  public void shouldMarkNotesToBeDeletedForSavedSessions()
  {
    sessionManager.currentSession = mock(Session.class);
    when(sessionManager.currentSession.getId()).thenReturn(1234L);
    Note note = new Note(null, null, location, null, 10);

    sessionManager.deleteNote(note);

    verify(sessionManager.currentSession).deleteNote(note);
  }

  @Test
  public void shouldRestartExternalSensor()
  {
    sessionManager.restartSensors();

    verify(sessionManager.externalSensors).start();
  }

  @Test
  public void should_not_crashOnDeletedSensor() throws Exception
  {
    // given
    SensorEvent event = new SensorEvent("CERN", "LHC", "Siggh boson", "SB", "number", "#", 1, 2, 3, 4, 5, 10);
    sessionManager.onEvent(event);

    // when
    sessionManager.deleteSensorStream(event.getSensorName());

    // then (shouldn't crash)
    sessionManager.onEvent(event);
  }
}
