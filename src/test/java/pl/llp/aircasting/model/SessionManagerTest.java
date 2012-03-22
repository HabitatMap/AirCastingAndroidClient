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
import com.google.common.collect.Iterables;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.helper.LocationHelper;
import pl.llp.aircasting.helper.MetadataHelper;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.repository.SessionRepository;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;
import pl.llp.aircasting.sensor.external.ExternalSensor;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.internal.matchers.IsCollectionContaining.hasItem;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 9/29/11
 * Time: 1:08 PM
 */
@RunWith(InjectedTestRunner.class)
public class SessionManagerTest {
    @Inject SessionManager sessionManager;

    Location location;

    private void mockSensors() {
        sessionManager.locationHelper = mock(LocationHelper.class);
        sessionManager.audioReader = mock(SimpleAudioReader.class);
        sessionManager.metadataHelper = mock(MetadataHelper.class);
        sessionManager.externalSensor = mock(ExternalSensor.class);
        sessionManager.eventBus = mock(EventBus.class);

        when(sessionManager.locationHelper.getLastLocation()).thenReturn(location);
    }

    @Before
    public void setup() {
        location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(50);
        location.setLongitude(20);
        sessionManager.sessionRepository = mock(SessionRepository.class);

        mockSensors();
    }

    private void triggerMeasurement(double value) {
        sessionManager.onEvent(new SensorEvent("LHC", "Higgs boson", "number", "#", value));
    }

    private void triggerMeasurement() {
        triggerMeasurement(10);
    }

    @Test
    public void shouldCreateMeasurementStreams() {
        triggerMeasurement();

        MeasurementStream expected = new MeasurementStream("LHC", "Higgs boson", "number", "#");
        assertThat(sessionManager.getMeasurementStreams(), hasItem(expected));
    }

    @Test
    public void shouldCreateOnlyOneStreamPerSensor() {
        triggerMeasurement();
        triggerMeasurement();

        assertThat(sessionManager.getMeasurementStreams().size(), equalTo(1));
    }

    @Test
    public void shouldCreateAStreamForEachSensor() {
        triggerMeasurement();
        sessionManager.onEvent(new SensorEvent("LHC2", "Siggh boson", "number", "#", 10));

        MeasurementStream expected = new MeasurementStream("LHC2", "Siggh boson", "number", "#");
        assertThat(sessionManager.getMeasurementStreams(), hasItem(expected));
    }

    @Test
    public void shouldAllowAccessToAParticularStream() {
        triggerMeasurement();

        MeasurementStream expected = Iterables.getOnlyElement(sessionManager.getMeasurementStreams());
        assertThat(sessionManager.getMeasurementStream("LHC") == expected, equalTo(true));
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
        triggerMeasurement(12.3);

        assertThat(sessionManager.getSoundMeasurements().isEmpty(), equalTo(true));
    }

    @Test
    public void shouldStoreLastMeasurementForEachSensor() {
        fail("Not implemented");
    }

    @Test
    public void shouldNotifyListenersAboutMeasurements() {
        SessionManager.Listener listener = mock(SessionManager.Listener.class);
        sessionManager.registerListener(listener);

        triggerMeasurement(11);

        verify(listener).onNewMeasurement(Mockito.any(Measurement.class));
    }

    @Test
    public void shouldStartSensors() {
        sessionManager.startSensors();

        verify(sessionManager.locationHelper).start();
        verify(sessionManager.audioReader).start();
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
    public void shouldUnregisterListeners() {
        SessionManager.Listener listener = mock(SessionManager.Listener.class);
        sessionManager.registerListener(listener);
        sessionManager.unregisterListener(listener);

        triggerMeasurement(22);

        verifyZeroInteractions(listener);
    }

    @Test
    public void shouldNotStopSessionWhenLastListenerUnregisters() {
        SessionManager.Listener listener = mock(SessionManager.Listener.class);
        sessionManager.registerListener(listener);

        sessionManager.startSession();
        sessionManager.unregisterListener(listener);

        assertThat(sessionManager.isSessionStarted(), equalTo(true));
    }

    @Test
    public void shouldDiscardSession() {
        triggerMeasurement(13.5);
        sessionManager.discardSession();

        verify(sessionManager.audioReader, never()).stop();
        verify(sessionManager.locationHelper).stop();
        verify(sessionManager.sessionRepository, never()).save(Mockito.any(Session.class));
        assertThat(sessionManager.getSoundMeasurements().isEmpty(), equalTo(true));
        assertThat(sessionManager.isSessionStarted(), equalTo(false));
    }

    @Test
    public void shouldStopASession() {
        SessionRepository.ProgressListener listener = mock(SessionRepository.ProgressListener.class);

        triggerMeasurement(11);
        sessionManager.finishSession(listener);

        verify(sessionManager.audioReader, never()).stop();
        verify(sessionManager.locationHelper).stop();
        verify(sessionManager.sessionRepository).save(Mockito.any(Session.class), eq(listener));
        assertThat(sessionManager.getSoundMeasurements().isEmpty(), equalTo(true));
        assertThat(sessionManager.isSessionStarted(), equalTo(false));
    }

    @Test
    public void shouldNotifyListenersOnSessionClobber() {
        SessionManager.Listener listener = mock(SessionManager.Listener.class);
        sessionManager.registerListener(listener);

        sessionManager.discardSession();

        verify(listener).onNewSession();
    }

    @Test
    public void shouldNotifyListenersOnSessionLoad() {
        SessionManager.Listener listener = mock(SessionManager.Listener.class);
        sessionManager.registerListener(listener);

        sessionManager.loadSession(0, null);

        verify(listener).onNewSession();
    }

    @Test
    public void shouldNotAddMeasurementsToASavedSession() {
        sessionManager.session = new Session();

        triggerMeasurement(10);

        assertThat(sessionManager.getSoundMeasurements().isEmpty(), equalTo(true));
    }

    @Test
    public void shouldProvideRunningAverage() {
        sessionManager.session = new Session();
        sessionManager.session.add(new Measurement(2));
        sessionManager.session.add(new Measurement(4));
        sessionManager.session.add(new Measurement(6));

        assertThat(sessionManager.getAvg(4), equalTo(4.0));
        assertThat(sessionManager.getAvg(3), equalTo(4.0));
        assertThat(sessionManager.getAvg(2), equalTo(5.0));
    }

    @Test
    public void shouldProvideRunningPeak() {
        sessionManager.session = new Session();
        sessionManager.session.add(new Measurement(6));
        sessionManager.session.add(new Measurement(4));
        sessionManager.session.add(new Measurement(2));

        assertThat(sessionManager.getPeak(4), equalTo(6.0));
        assertThat(sessionManager.getPeak(3), equalTo(6.0));
        assertThat(sessionManager.getPeak(2), equalTo(4.0));
    }

    @Test
    public void shouldSaveAdditionalData() {
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
        }), Mockito.any(SessionRepository.ProgressListener.class));
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
}
