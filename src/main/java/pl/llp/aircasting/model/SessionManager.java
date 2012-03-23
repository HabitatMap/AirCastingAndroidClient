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

import android.app.Application;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.event.sensor.AudioReaderErrorEvent;
import pl.llp.aircasting.event.sensor.MeasurementEvent;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.event.session.NoteCreatedEvent;
import pl.llp.aircasting.event.session.SessionChangeEvent;
import pl.llp.aircasting.helper.*;
import pl.llp.aircasting.repository.SessionRepository;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;
import pl.llp.aircasting.sensor.external.ExternalSensor;

import java.util.*;

import static com.google.common.collect.Iterables.skip;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.primitives.Ints.min;
import static com.google.inject.internal.Lists.newArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 9/29/11
 * Time: 1:09 PM
 */
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

    Session session = new Session();

    private Map<String, Double> recentMeasurements = newHashMap();

    boolean sessionStarted = false;

    private boolean recording;
    private boolean paused;
    private int noteNumber = 0;

    private Map<String, MeasurementStream> measurementStreams = newHashMap();

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

    public void loadSession(long id, SessionRepository.ProgressListener listener) {
        Session newSession = sessionRepository.loadEager(id, listener);
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

    public synchronized double getPeak(int n) {
        if (session.getMeasurements().isEmpty()) return 0;

        Iterable<Measurement> measurements = getLast(n);
        double max = Double.NEGATIVE_INFINITY;
        for (Measurement measurement : measurements) {
            if (measurement.getValue() > max) max = measurement.getValue();
        }

        return max;
    }

    private Iterable<Measurement> getLast(int n) {
        int toSkip = session.getMeasurements().size() - n;
        if (toSkip < 0) toSkip = 0;
        return skip(session.getMeasurements(), toSkip);
    }

    public synchronized double getAvg(int n) {
        if (session.getMeasurements().isEmpty()) return 0;

        double result = 0;

        Iterable<Measurement> measurements = getLast(n);
        for (Measurement measurement : measurements) {
            result += measurement.getValue();
        }

        return result / min(n, session.getMeasurements().size());
    }

    public void setContribute(boolean value) {
        session.setContribute(value);
    }

    @Subscribe
    public void onEvent(AudioReaderErrorEvent event) {
        notificationHandler.post(new Runnable() {
            @Override
            public void run() {
                for (Listener listener : listeners) {
                    listener.onError();
                }
            }
        });
    }

    @Subscribe
    public synchronized void onEvent(SensorEvent event) {
        double value = event.getValue();
        recentMeasurements.put(event.getSensorName(), value);

        if (locationHelper.getLastLocation() != null) {
            double latitude = locationHelper.getLastLocation().getLatitude();
            double longitude = locationHelper.getLastLocation().getLongitude();

            Measurement measurement = new Measurement(latitude, longitude, value);
            if (sessionStarted) {
                MeasurementStream stream = prepareStream(event);
                stream.add(measurement);
            }
            
            eventBus.post(new MeasurementEvent(measurement));
        }
    }

    private MeasurementStream prepareStream(SensorEvent event) {
        String sensorName = event.getSensorName();
        if (!measurementStreams.containsKey(sensorName)) {
            String measurementType = event.getMeasurementType();
            String unit = event.getUnit();
            String symbol = event.getSymbol();
            MeasurementStream stream = new MeasurementStream(sensorName, measurementType, unit, symbol);
            measurementStreams.put(sensorName, stream);
        }
        
        return measurementStreams.get(sensorName);
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
        return measurementStreams.values();
    }

    public MeasurementStream getMeasurementStream(String sensorName) {
        return measurementStreams.get(sensorName);
    }

    public interface Listener {
        public void onError();
    }

    private Set<Listener> listeners = new HashSet<Listener>();

    public void registerListener(Listener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(Listener listener) {
        listeners.remove(listener);
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

    public double getDbPeak() {
        return session.getPeak();
    }

    public double getDbAvg() {
        return session.getAvg();
    }

    public synchronized double getNow(String sensorName) {
        if(!recentMeasurements.containsKey(sensorName)){
            return 0;
        }
        return recentMeasurements.get(sensorName);
    }

    Handler notificationHandler = new Handler();

    private void notifyNewSession() {
        eventBus.post(new SessionChangeEvent());
    }

    public void startSession() {
        setSession(new Session());
        startSensors();
        sessionStarted = true;
        notificationHelper.showRecordingNotification();
    }

    public void finishSession(SessionRepository.ProgressListener progressListener) {
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
    }

    private void cleanup() {
        locationHelper.stop();
        sessionStarted = false;
        noteNumber = 0;
        setSession(new Session());
        measurementStreams = newHashMap();
        notificationHelper.hideRecordingNotification();
    }

    public boolean isSessionStarted() {
        return sessionStarted;
    }

    public synchronized List<Measurement> getSoundMeasurements() {
        return newArrayList(session.getMeasurements());
    }
}
