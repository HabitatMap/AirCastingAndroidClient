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
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.audio.SimpleAudioReader;
import pl.llp.aircasting.audio.SoundVolumeListener;
import pl.llp.aircasting.helper.LocationHelper;
import pl.llp.aircasting.helper.MetadataHelper;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.helper.SoundHelper;
import pl.llp.aircasting.repository.SessionRepository;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Iterables.skip;
import static com.google.common.primitives.Ints.min;
import static com.google.inject.internal.Lists.newArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 9/29/11
 * Time: 1:09 PM
 */
@Singleton
public class SessionManager implements SoundVolumeListener {
    @Inject SimpleAudioReader audioReader;
    @Inject SessionRepository sessionRepository;
    @Inject SettingsHelper settingsHelper;
    @Inject Application applicationContext;
    @Inject LocationHelper locationHelper;
    @Inject MetadataHelper metadataHelper;
    @Inject TelephonyManager telephonyManager;

    Session session = new Session();

    private double dbLast = SoundHelper.TOTALLY_QUIET;

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
    }

    public Session getSession() {
        return session;
    }

    public void loadSession(long id, SessionRepository.ProgressListener listener) {
        Session session = sessionRepository.loadEager(id, listener);
        setSession(session);
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
        for (Listener listener : listeners) {
            listener.onNewNote(note);
        }
    }

    public Iterable<Note> getNotes() {
        return getSession().getNotes();
    }

    public void startSensors() {
        if (!recording) {
            locationHelper.start();

            audioReader.start(this);

            recording = true;
        }
    }

    private synchronized void pauseSession() {
        if (recording) {
            paused = true;
            audioReader.stop();
        }
    }

    private synchronized void continueSession() {
        if (paused) {
            paused = false;
            audioReader.start(this);
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
        if (session.getSoundMeasurements().isEmpty()) return 0;

        Iterable<SoundMeasurement> measurements = getLast(n);
        double max = Double.NEGATIVE_INFINITY;
        for (SoundMeasurement measurement : measurements) {
            if (measurement.getValue() > max) max = measurement.getValue();
        }

        return max;
    }

    private Iterable<SoundMeasurement> getLast(int n) {
        int toSkip = session.getSoundMeasurements().size() - n;
        if (toSkip < 0) toSkip = 0;
        return skip(session.getSoundMeasurements(), toSkip);
    }

    public synchronized double getAvg(int n) {
        if (session.getSoundMeasurements().isEmpty()) return 0;

        double result = 0;

        Iterable<SoundMeasurement> measurements = getLast(n);
        for (SoundMeasurement measurement : measurements) {
            result += measurement.getValue();
        }

        return result / min(n, session.getSoundMeasurements().size());
    }

    public void setContribute(boolean value) {
        session.setContribute(value);
    }

    @Override
    public void onError() {
        notificationHandler.post(new Runnable() {
            @Override
            public void run() {
                for (Listener listener : listeners) {
                    listener.onError();
                }
            }
        });
    }

    @Override
    public synchronized void onMeasurement(double value) {
        dbLast = value;

        if (locationHelper.getLastLocation() != null) {
            double latitude = locationHelper.getLastLocation().getLatitude();
            double longitude = locationHelper.getLastLocation().getLongitude();

            SoundMeasurement measurement = new SoundMeasurement(latitude, longitude, value);
            if (sessionStarted) {
                session.add(measurement);
            }
            notifyMeasurement(measurement);
        }

        notifyNewReading();
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

    public interface Listener {
        public void onNewMeasurement(SoundMeasurement measurement);

        public void onNewSession();

        public void onNewNote(Note note);

        public void onNewReading();

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

    public double getDbNow() {
        return dbLast;
    }

    Handler notificationHandler = new Handler();

    private void notifyMeasurement(final SoundMeasurement measurement) {
        notificationHandler.post(new Runnable() {
            @Override
            public void run() {
                for (Listener listener : listeners) {
                    listener.onNewMeasurement(measurement);
                }
            }
        });
    }

    private void notifyNewSession() {
        for (Listener listener : listeners) {
            listener.onNewSession();
        }
    }

    private void notifyNewReading() {
        notificationHandler.post(new Runnable() {
            @Override
            public void run() {
                for (Listener listener : listeners) {
                    listener.onNewReading();
                }
            }
        });
    }

    public void startSession() {
        setSession(new Session());
        startSensors();
        sessionStarted = true;
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
    }

    public boolean isSessionStarted() {
        return sessionStarted;
    }

    public synchronized List<SoundMeasurement> getSoundMeasurements() {
        return newArrayList(session.getSoundMeasurements());
    }
}
