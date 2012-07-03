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
import pl.llp.aircasting.event.sensor.MeasurementEvent;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.event.session.SessionChangeEvent;
import pl.llp.aircasting.helper.LocationHelper;
import pl.llp.aircasting.helper.MetadataHelper;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.repository.ProgressListener;
import pl.llp.aircasting.repository.SessionRepository;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;
import pl.llp.aircasting.sensor.external.ExternalSensor;

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

import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.IsCollectionContaining.hasItem;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(InjectedTestRunner.class)
public class SessionManagerTest {
    @Inject SessionManager sessionManager;

    Location location;
    Sensor sensor;

    SensorEvent lastEvent;
    ProgressListener progressListener;

    private void mockSensors() {
        sessionManager.locationHelper = mock(LocationHelper.class);
        sessionManager.audioReader = mock(SimpleAudioReader.class);
        sessionManager.metadataHelper = mock(MetadataHelper.class);
        sessionManager.externalSensor = mock(ExternalSensor.class);
        sessionManager.eventBus = mock(EventBus.class);
        sessionManager.sensorManager = mock(SensorManager.class);

        sensor = mock(Sensor.class);
        when(sensor.isEnabled()).thenReturn(true);
        when(sensor.getSensorName()).thenReturn("LHC");

        when(sessionManager.locationHelper.getLastLocation()).thenReturn(location);
        when(sessionManager.sensorManager.getSensor(Mockito.any(String.class))).thenReturn(sensor);
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

    private void triggerMeasurement(String name, double value) {

        lastEvent = New.sensorEvent(name, value);
        sessionManager.onEvent(lastEvent);
    }

    private void triggerMeasurement(double value) {
        triggerMeasurement("LHC", value);
    }

    private void triggerMeasurement() {
        triggerMeasurement(10);
    }

    @Test
    public void shouldCreateMeasurementStreams() {
        sessionManager.startSession();

        triggerMeasurement();

        MeasurementStream expected = new MeasurementStream(lastEvent);
        assertThat(sessionManager.getMeasurementStreams(), hasItem(expected));
    }

    @Test
    public void shouldCreateOnlyOneStreamPerSensor() {
        sessionManager.startSession();

        triggerMeasurement();
        triggerMeasurement();

        assertThat(sessionManager.getMeasurementStreams().size(), equalTo(1));
    }

    @Test
    public void shouldCreateAStreamForEachSensor() {
        sessionManager.startSession();

        triggerMeasurement();
        SensorEvent event = new SensorEvent("CERN", "LHC2", "Siggh boson", "SB", "number", "#", 1, 2, 3, 4, 5, 10);
        sessionManager.onEvent(event);

        MeasurementStream expected = new MeasurementStream(event);
        assertThat(sessionManager.getMeasurementStreams(), hasItem(expected));
    }

    @Test
    public void shouldAllowAccessToAParticularStream() {
        sessionManager.startSession();

        triggerMeasurement();

        MeasurementStream expected = Iterables.getOnlyElement(sessionManager.getMeasurementStreams());
        assertThat(sessionManager.getMeasurementStream("LHC") == expected, equalTo(true));
    }

    @Test
    public void shouldStoreLastMeasurementForEachSensor() {
        triggerMeasurement("LHC", 150);
        triggerMeasurement("LHC2", 123);

        Sensor sensor2 = mock(Sensor.class);
        when(sensor2.getSensorName()).thenReturn("LHC2");

        assertThat(sessionManager.getNow(sensor), equalTo(150.0));
        assertThat(sessionManager.getNow(sensor2), equalTo(123.0));
    }

    @Test
    public void shouldAssumeLastMeasurementIsZeroByDefault() {
        assertThat(sessionManager.getNow(sensor), equalTo(0.0));
    }

    @Test
    public void shouldProvideAvgForEachStream() {
        MeasurementStream stream = mock(MeasurementStream.class);
        when(stream.getAvg()).thenReturn(10.0);
        String name = sensor.getSensorName();
        when(stream.getSensorName()).thenReturn(name);
        sessionManager.session.add(stream);

        assertThat(sessionManager.getAvg(sensor), equalTo(10.0));
    }

    @Test
    public void shouldProvidePeakForEachStream() {
        MeasurementStream stream = mock(MeasurementStream.class);
        when(stream.getPeak()).thenReturn(11.0);
        String name = sensor.getSensorName();
        when(stream.getSensorName()).thenReturn(name);
        sessionManager.session.add(stream);

        assertThat(sessionManager.getPeak(sensor), equalTo(11.0));
    }

    @Test
    public void shouldStoreMeasurements() {
        sessionManager.sessionStarted = true;

        triggerMeasurement(22);

        Measurement expected = new Measurement(location.getLatitude(), location.getLongitude(), 22);
        assertThat(sessionManager.getMeasurementStream("LHC").getMeasurements(), hasItem(equalTo(expected)));
    }

    @Test
    public void shouldSkipMeasurementsWithoutLocation() {
        sessionManager.sessionStarted = true;
        when(sessionManager.locationHelper.getLastLocation()).thenReturn(null);

        triggerMeasurement();

        assertThat(sessionManager.getMeasurementStreams().isEmpty(), equalTo(true));
    }

    @Test
    public void shouldSkipMeasurementsFromDisabledStreams() {
        sessionManager.sessionStarted = true;
        when(sensor.isEnabled()).thenReturn(false);

        triggerMeasurement();

        assertThat(sessionManager.getMeasurementStreams().isEmpty(), equalTo(true));
    }

    @Test
    public void shouldNotifyListenersAboutMeasurements() {
        triggerMeasurement(11);

        verify(sessionManager.eventBus).post(Mockito.any(MeasurementEvent.class));
    }

    @Test
    public void shouldStartSensors() {
        sessionManager.startSensors();

        verify(sessionManager.locationHelper).start();
        verify(sessionManager.audioReader).start();
        verify(sessionManager.externalSensor).start();
        assertThat(sessionManager.isRecording(), equalTo(true));
    }

    @Test
    public void shouldOnlyStartSensorsOnce() {
        sessionManager.startSensors();
        sessionManager.startSensors();

        verify(sessionManager.locationHelper, atMost(1)).start();
        verify(sessionManager.audioReader, atMost(1)).start();
        assertThat(sessionManager.isRecording(), equalTo(true));
    }

    @Test
    public void shouldStopSensors() {
        sessionManager.startSensors();
        sessionManager.stopSensors();

        verify(sessionManager.audioReader).stop();
        verify(sessionManager.locationHelper).stop();
        assertThat(sessionManager.isSessionStarted(), equalTo(false));
    }

    @Test
    public void shouldNotStopSensorsDuringASession() {
        sessionManager.startSession();
        sessionManager.stopSensors();

        verify(sessionManager.locationHelper, never()).stop();
        verify(sessionManager.audioReader, never()).stop();
        assertThat(sessionManager.isSessionStarted(), equalTo(true));
    }

    @Test
    public void shouldStartASession() {
        sessionManager.startSession();

        verify(sessionManager.locationHelper).start();
        verify(sessionManager.audioReader).start();
        assertThat(sessionManager.isSessionStarted(), equalTo(true));
    }

    @Test
    public void shouldDiscardASession() {
        sessionManager.startSession();

        triggerMeasurement(13.5);
        sessionManager.discardSession();

        verify(sessionManager.audioReader, never()).stop();
        verify(sessionManager.locationHelper).stop();
        verify(sessionManager.sessionRepository, never()).save(Mockito.any(Session.class));
        assertThat(sessionManager.getMeasurementStreams().isEmpty(), equalTo(true));
        assertThat(sessionManager.isSessionStarted(), equalTo(false));
    }

    @Ignore("Fix session persistence")
    @Test
    public void shouldStopASession() {


        ProgressListener listener = mock(ProgressListener.class);

        triggerMeasurement(11);
        sessionManager.finishSession(listener);

        verify(sessionManager.audioReader, never()).stop();
        verify(sessionManager.locationHelper).stop();
        verify(sessionManager.sessionRepository).save(Mockito.any(Session.class), eq(listener));
        assertThat(sessionManager.isSessionStarted(), equalTo(false));
    }

    @Test
    public void shouldNotifyListenersOnSessionClobber() {
        sessionManager.discardSession();

        verify(sessionManager.eventBus).post(Mockito.any(SessionChangeEvent.class));
    }

    @Test
    public void shouldNotifyListenersOnSessionLoad()
    {
      sessionManager = spy(sessionManager);
      doReturn(new Session()).when(sessionManager.sessionRepository).loadFully(anyLong(), Matchers.<ProgressListener>anyObject());

      sessionManager.loadSession(0, progressListener);

      verify(sessionManager, atLeastOnce()).setSession(any(Session.class));
    }

    @Test
    public void shouldNotAddMeasurementsToASavedSession() {
        sessionManager.session = new Session();

        triggerMeasurement(10);

        assertThat(sessionManager.getMeasurementStreams().isEmpty(), equalTo(true));
    }

    @Test
    public void shouldSetSessionStart() {
        sessionManager.startSession();

        int oneSecond = 1000;
        assertThat(new Date().getTime() - sessionManager.session.getStart().getTime() < oneSecond, equalTo(true));
    }

    @Test
    public void shouldSetSessionEnd() {
        sessionManager.startSession();
        triggerMeasurement();

        sessionManager.finishSession(null);

        verify(sessionManager.sessionRepository).save(Mockito.argThat(new BaseMatcher<Session>() {
            @Override
            public boolean matches(Object o) {
                Session other = (Session) o;
                long oneSecond = 1000;
                return new Date().getTime() - other.getEnd().getTime() < oneSecond;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Session with end set");
            }
        }), Mockito.<ProgressListener>any());
    }

    @Ignore("Needs a redo")
    @Test
    public void shouldSaveAdditionalData()
    {
        sessionManager.settingsHelper = mock(SettingsHelper.class);
        sessionManager.metadataHelper = mock(MetadataHelper.class);
        when(sessionManager.settingsHelper.getCalibration()).thenReturn(123);
        when(sessionManager.settingsHelper.getOffset60DB()).thenReturn(432);
        when(sessionManager.metadataHelper.getOSVersion()).thenReturn("1.1.1");
        when(sessionManager.metadataHelper.getDataType()).thenReturn("volts");
        when(sessionManager.metadataHelper.getInstrument()).thenReturn("hammer");
        when(sessionManager.metadataHelper.getPhoneModel()).thenReturn("very old");

        triggerMeasurement(100);
        sessionManager.finishSession(null);

        verify(sessionManager.sessionRepository).save(Mockito.argThat(new BaseMatcher<Session>() {
            @Override
            public boolean matches(Object o) {
                Session session = (Session) o;
                return session.getCalibration() == 123
                        && session.getOffset60DB() == 432
                        && "1.1.1".equals(session.getOSVersion())
                        && "volts".equals(session.getDataType())
                        && "hammer".equals(session.getInstrument())
                        && "very old".equals(session.getPhoneModel());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Session with additional data set");
            }
        }), Mockito.any(ProgressListener.class));
    }

    @Test
    public void shouldJustDeleteNotesIfSessionInProgress() {
        sessionManager.startSession();
        Note note = sessionManager.makeANote(null, null, null);

        sessionManager.deleteNote(note);

        assertThat(sessionManager.getNotes(), not(hasItem(note)));
    }

    @Test
    public void afterDeletingNotesShouldHaveNewNumbers() {
        sessionManager.startSession();
        Note note1 = sessionManager.makeANote(null, null, null);
        Note note2 = sessionManager.makeANote(null, null, null);

        sessionManager.deleteNote(note1);
        Note note3 = sessionManager.makeANote(null, null, null);

        assertThat(note3.getNumber(), not(equalTo(note2.getNumber())));
    }

    @Test
    public void shouldMarkNotesToBeDeletedForSavedSessions() {
        sessionManager.session = mock(Session.class);
        when(sessionManager.session.isSaved()).thenReturn(true);
        Note note = new Note(null, null, location, null, 10);

        sessionManager.deleteNote(note);

        verify(sessionManager.session).deleteNote(note);
        verify(sessionManager.sessionRepository).deleteNote(sessionManager.session, note);
    }

    @Test
    public void shouldRestartExternalSensor() {
        sessionManager.restartSensors();

        verify(sessionManager.externalSensor).start();
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
