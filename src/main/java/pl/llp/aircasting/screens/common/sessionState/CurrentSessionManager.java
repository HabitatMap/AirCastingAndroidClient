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
package pl.llp.aircasting.screens.common.sessionState;

import pl.llp.aircasting.Intents;
import pl.llp.aircasting.event.measurements.MobileMeasurementEvent;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.screens.common.ApplicationState;
import pl.llp.aircasting.screens.common.helpers.LocationHelper;
import pl.llp.aircasting.screens.common.helpers.NotificationHelper;
import pl.llp.aircasting.event.session.CurrentSessionSetEvent;
import pl.llp.aircasting.event.session.SessionStartedEvent;
import pl.llp.aircasting.event.session.SessionStoppedEvent;
import pl.llp.aircasting.storage.repository.SessionRepository;
import pl.llp.aircasting.tracking.ContinuousTracker;
import pl.llp.aircasting.util.Constants;

import android.app.Application;
import android.location.Location;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Date;

import static com.google.common.collect.Lists.newArrayList;

@Singleton
public class CurrentSessionManager {
    public static final double TOTALLY_FAKE_COORDINATE = 200;

    @Inject EventBus eventBus;
    @Inject SessionRepository sessionRepository;
    @Inject LocationHelper locationHelper;
    @Inject NotificationHelper notificationHelper;
    @Inject Application applicationContext;
    @Inject TelephonyManager telephonyManager;
    @Inject CurrentSessionSensorManager currentSessionSensorManager;
    @Inject ContinuousTracker tracker;
    @Inject ApplicationState state;

    @NotNull
    Session currentSession = new Session();

    private boolean paused;

    @Inject
    public void init() {
        setSession(new Session());

        telephonyManager.listen(new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == TelephonyManager.CALL_STATE_IDLE) {
                    continueSession();
                } else {
                    pauseSession();
                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE);

        eventBus.register(this);
    }

    @NotNull
    public Session getCurrentSession() {
        return currentSession;
    }

    public void setSession(@NotNull Session session) {
        Preconditions.checkNotNull(session, "Cannot set null session");
        this.currentSession = session;
        eventBus.post(new CurrentSessionSetEvent(session));
    }

    public boolean isSessionRecording() {
        return currentSessionSensorManager.anySensorConnected() && state.recording().isRecording();
    }

    public boolean isSessionIdle() {
        return currentSessionSensorManager.anySensorConnected() && state.recording().isJustShowingCurrentValues();
    }

    public void updateSession(Session from) {
        Preconditions.checkNotNull(from.getId(), "Unsaved session?");
        setTitleTags(from.getId(), from.getTitle(),
                from.getTags()
        );
    }

    public Note makeANote(Date date, String text, String picturePath) {
        Note note = new Note(date, text, locationHelper.getLastLocation(), picturePath);

        tracker.addNote(note);
        return note;
    }

    public synchronized void pauseSession() {
        if (state.recording().isRecording()) {
            paused = true;
            currentSessionSensorManager.stopAudioSensor();
        }
    }

    public synchronized void continueSession() {
        if (paused) {
            paused = false;
            currentSessionSensorManager.startAudioSensor();
        }
    }

    @Subscribe
    public synchronized void onEvent(SensorEvent event) {
        double value = event.getValue();
        String sensorName = event.getSensorName();
        Sensor sensor = currentSessionSensorManager.getSensorByName(sensorName);

        Location location = getLocation();
        if (location != null && sensor != null && sensor.isEnabled()) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            Measurement measurement = new Measurement(latitude, longitude, value, event.getMeasuredValue(), event.getDate());

            if (state.recording().isRecording()) {
                MeasurementStream stream = prepareStream(event);
                tracker.addMeasurement(sensor, stream, measurement);
            } else {
                eventBus.post(new MobileMeasurementEvent(measurement, sensor, Constants.CURRENT_SESSION_FAKE_ID));
            }
        }
    }

    public Location getLocation() {
        Location location = locationHelper.getLastLocation();

        if (currentSession.isFixed()) {
            location.setLatitude(currentSession.getLatitude());
            location.setLongitude(currentSession.getLongitude());
        } else if (currentSession.isLocationless()) {
            location = new Location("fake");
            location.setLatitude(TOTALLY_FAKE_COORDINATE);
            location.setLongitude(TOTALLY_FAKE_COORDINATE);
        }

        return location;
    }

    private MeasurementStream prepareStream(SensorEvent event) {
        String sensorName = event.getSensorName();

        if (!currentSession.hasStream(sensorName)) {
            MeasurementStream stream = event.stream();
            tracker.addStream(stream);
        }

        MeasurementStream stream = currentSession.getStream(sensorName);
        if (stream.isVisible()) {
            stream.markAs(MeasurementStream.Visibility.VISIBLE_RECONNECTED);
        }

        return stream;
    }

    public Collection<MeasurementStream> getMeasurementStreams() {
        return newArrayList(currentSession.getActiveMeasurementStreams());
    }

    public MeasurementStream getMeasurementStream(String sensorName) {
        return currentSession.getStream(sensorName);
    }

    @VisibleForTesting
    void discardSession() {
        Long sessionId = getCurrentSession().getId();
        discardSession(sessionId);
    }

    public void startMobileSession(String title, String tags, boolean locationLess) {
        Session newSession = new Session(false);
        newSession.setTitle(title);
        newSession.setTags(tags);

        setSession(newSession);

        eventBus.post(new SessionStartedEvent(getCurrentSession()));
        currentSessionSensorManager.startSensors();
        state.recording().startRecording();
        notificationHelper.showRecordingNotification();

        if (!tracker.startTracking(getCurrentSession(), locationLess)) {
            cleanup();
        }
    }

    public void stopSession() {
        tracker.stopTracking(getCurrentSession());
        state.recording().stopRecording();
        notificationHelper.hideRecordingNotification();
        eventBus.post(new SessionStoppedEvent(getCurrentSession()));
    }

    public void finishSession(long sessionId, boolean shouldContribute) {
        synchronized (this) {
            tracker.setContribute(sessionId, shouldContribute);
            tracker.complete(sessionId);
            Intents.triggerSync(applicationContext);
        }

        cleanup();
    }

    public void discardSession(long sessionId) {
        tracker.discard(sessionId);
        cleanup();
    }

    public void deleteSession() {
        Long sessionId = currentSession.getId();
        sessionRepository.markSessionForRemoval(sessionId);
        discardSession(sessionId);
    }

    private void cleanup() {
        state.recording().stopRecording();
        setSession(new Session());
        notificationHelper.hideRecordingNotification();
    }

    public void setTitleTags(long sessionId, String title, String tags) {
        tracker.setTitle(sessionId, title);
        tracker.setTags(sessionId, tags);
    }

    public boolean anySensorConnected() {
        return currentSessionSensorManager.anySensorConnected();
    }
}
