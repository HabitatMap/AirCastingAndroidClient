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

import pl.llp.aircasting.Intents;
import pl.llp.aircasting.event.sensor.MeasurementEvent;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.event.session.NoteCreatedEvent;
import pl.llp.aircasting.event.session.SessionChangeEvent;
import pl.llp.aircasting.helper.LocationHelper;
import pl.llp.aircasting.helper.MetadataHelper;
import pl.llp.aircasting.helper.NotificationHelper;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.repository.ProgressListener;
import pl.llp.aircasting.repository.SessionRepository;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;
import pl.llp.aircasting.sensor.external.ExternalSensor;

import android.app.Application;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

@Singleton
public class SessionManager {
    @Inject SimpleAudioReader audioReader;
    @Inject ExternalSensor externalSensor;
    @Inject EventBus eventBus;

    @Inject SessionRepository sessionRepository;

    @Inject SettingsHelper settingsHelper;
    @Inject LocationHelper locationHelper;
    @Inject MetadataHelper metadataHelper;
    @Inject NotificationHelper notificationHelper;

    @Inject Application applicationContext;
    @Inject TelephonyManager telephonyManager;
    @Inject SensorManager sensorManager;

    Session session = new Session();

    private Map<String, Double> recentMeasurements = newHashMap();

    boolean sessionStarted = false;

    private boolean recording;
    private boolean paused;
    private int noteNumber = 0;

    @Inject
    public void init() {
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

    public Session getSession() {
        return session;
    }

    public void loadSession(long id, ProgressListener listener) {
        Session newSession = sessionRepository.loadFully(id, listener);
        setSession(newSession);
    }

    private void setSession(Session session) {
        this.session = session;
        notifyNewSession();
    }

    public boolean isSessionSaved() {
        return session.isSaved();
    }

    public void updateSession(Session session) {
        this.session.updateHeader(session);
        sessionRepository.update(this.session);
    }

    public Note makeANote(Date date, String text, String picturePath) {
        Note note = new Note(date, text, locationHelper.getLastLocation(), picturePath, noteNumber++);
        session.add(note);

        notifyNote(note);
        return note;
    }

    private void notifyNote(Note note) {
        eventBus.post(new NoteCreatedEvent(note));
    }

    public Iterable<Note> getNotes() {
        return getSession().getNotes();
    }

    public void startSensors() {
        if (!recording) {
            locationHelper.start();

            audioReader.start();

            externalSensor.start();

            recording = true;
        }
    }

    public synchronized void pauseSession() {
        if (recording) {
            paused = true;
            audioReader.stop();
        }
    }

    public synchronized void continueSession() {
        if (paused) {
            paused = false;
            audioReader.start();
        }
    }

    public void stopSensors() {
        if (!sessionStarted) {
            locationHelper.stop();

            audioReader.stop();

            recording = false;
        }
    }

    public boolean isRecording() {
        return recording;
    }

    public void setContribute(boolean value) {
        session.setContribute(value);
    }

    @Subscribe
    public synchronized void onEvent(SensorEvent event) {
        double value = event.getValue();
        String sensorName = event.getSensorName();
        Sensor sensor = sensorManager.getSensor(sensorName);

        recentMeasurements.put(sensorName, value);

        if (locationHelper.getLastLocation() != null && sensor.isEnabled()) {
            double latitude = locationHelper.getLastLocation().getLatitude();
            double longitude = locationHelper.getLastLocation().getLongitude();

            Measurement measurement = new Measurement(latitude, longitude, value);
            if (sessionStarted) {
                MeasurementStream stream = prepareStream(event);
                stream.add(measurement);
            }

            eventBus.post(new MeasurementEvent(measurement, sensor));
        }
    }

    private MeasurementStream prepareStream(SensorEvent event) {
        String sensorName = event.getSensorName();

        if (!session.hasStream(sensorName)) {
            MeasurementStream stream = new MeasurementStream(event);
            session.add(stream);
        }

        return session.getStream(sensorName);
    }

    public Note getNote(int i) {
        return session.getNotes().get(i);
    }

    public void saveChanges() {
        sessionRepository.update(session);
    }

    public void deleteNote(Note note) {
        if (session.isSaved()) {
            sessionRepository.deleteNote(session, note);
        }
        session.deleteNote(note);
    }

    public int getNoteCount() {
        return session.getNotes().size();
    }

    public void restartSensors() {
        externalSensor.start();
    }

    public Collection<MeasurementStream> getMeasurementStreams() {
        return session.getMeasurementStreams();
    }

    public MeasurementStream getMeasurementStream(String sensorName) {
        return session.getStream(sensorName);
    }

    public void discardSession() {
        cleanup();
    }

    public void setTitle(String text) {
        session.setTitle(text);
    }

    public void setTags(String text) {
        session.setTags(text);
    }

    public void setDescription(String text) {
        session.setDescription(text);
    }

    public synchronized double getNow(Sensor sensor) {
        if (!recentMeasurements.containsKey(sensor.getSensorName())) {
            return 0;
        }
        return recentMeasurements.get(sensor.getSensorName());
    }

    private void notifyNewSession() {
        eventBus.post(new SessionChangeEvent());
    }

    public void startSession() {
        setSession(new Session());
        session.setStart(new Date());
        startSensors();
        sessionStarted = true;
        notificationHelper.showRecordingNotification();
    }

    public void finishSession(ProgressListener progressListener) {
        synchronized (this) {
            fillInDetails();

            sessionRepository.save(session, progressListener);
            Intents.triggerSync(applicationContext);
        }
        cleanup();
    }

    private void fillInDetails() {
        session.setCalibration(settingsHelper.getCalibration());
        session.setOffset60DB(settingsHelper.getOffset60DB());

        session.setDataType(metadataHelper.getDataType());
        session.setInstrument(metadataHelper.getInstrument());
        session.setOsVersion(metadataHelper.getOSVersion());
        session.setPhoneModel(metadataHelper.getPhoneModel());

        session.setEnd(new Date());
    }

    private void cleanup() {
        locationHelper.stop();
        sessionStarted = false;
        noteNumber = 0;
        setSession(new Session());
        notificationHelper.hideRecordingNotification();
    }

    public boolean isSessionStarted() {
        return sessionStarted;
    }

    public synchronized List<Measurement> getSoundMeasurements() {
        return newArrayList(session.getMeasurements());
    }

    public double getAvg(Sensor sensor) {
        String sensorName = sensor.getSensorName();

        if (session.hasStream(sensorName)) {
            return session.getStream(sensorName).getAvg();
        } else {
            return 0;
        }
    }

    public double getPeak(Sensor sensor) {
        String sensorName = sensor.getSensorName();

        if (session.hasStream(sensorName)) {
            return session.getStream(sensorName).getPeak();
        } else {
            return 0;
        }
    }

    public List<Measurement> getMeasurements(Sensor sensor) {
        String name = sensor.getSensorName();

        if (session.hasStream(name)) {
            MeasurementStream stream = session.getStream(name);
            return stream.getMeasurements();
        } else {
            return newArrayList();
        }
    }

  public void deleteStream(Sensor sensor)
  {
    String sensorName = sensor.getSensorName();
    MeasurementStream stream = getMeasurementStream(sensorName);
    sessionRepository.deleteStream(session.getId(), stream);
  }
}
