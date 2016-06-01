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

import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.activity.ApplicationState;
import pl.llp.aircasting.activity.events.SessionChangeEvent;
import pl.llp.aircasting.activity.events.SessionStartedEvent;
import pl.llp.aircasting.activity.events.SessionStoppedEvent;
import pl.llp.aircasting.android.Logger;
import pl.llp.aircasting.helper.LocationHelper;
import pl.llp.aircasting.helper.NotificationHelper;
import pl.llp.aircasting.model.events.MeasurementEvent;
import pl.llp.aircasting.model.events.SensorEvent;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;
import pl.llp.aircasting.sensor.external.ExternalSensors;
import pl.llp.aircasting.storage.DatabaseTaskQueue;
import pl.llp.aircasting.storage.ProgressListener;
import pl.llp.aircasting.storage.db.DBConstants;
import pl.llp.aircasting.storage.db.WritableDatabaseTask;
import pl.llp.aircasting.storage.repository.SessionRepository;
import pl.llp.aircasting.tracking.ContinuousTracker;

import android.app.Application;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

@Singleton
public class SessionManager
{
  public static final double TOTALLY_FAKE_COORDINATE = 200;

  @Inject SimpleAudioReader audioReader;
  @Inject EventBus eventBus;

  @Inject SessionRepository sessionRepository;
  @Inject DatabaseTaskQueue dbQueue;

  @Inject LocationHelper locationHelper;
  @Inject NotificationHelper notificationHelper;

  @Inject Application applicationContext;
  @Inject TelephonyManager telephonyManager;
  @Inject SensorManager sensorManager;

  @NotNull Session session = new Session();

  @Inject ExternalSensors externalSensors;
  @Inject ContinuousTracker tracker;

  @Inject ApplicationState state;

  private Map<String, Double> recentMeasurements = newHashMap();

  private boolean paused;

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

  @NotNull
  public Session getSession() {
    return session;
  }

  public void loadSession(long sessionId, @NotNull ProgressListener listener)
  {
    Preconditions.checkNotNull(listener);
    Session newSession = sessionRepository.loadFully(sessionId, listener);
    state.recording().startShowingOldSession();
    setSession(newSession);
  }

  public void refreshUnits() {
      if (state.recording().isJustShowingCurrentValues())
          setSession(new Session());
  }

  void setSession(@NotNull Session session)
  {
    Preconditions.checkNotNull(session, "Cannot set null session");
    this.session = session;
    notifyNewSession(session);
  }

  public boolean isSessionSaved()
  {
    return state.recording().isShowingOldSession();
  }

  public void updateSession(Session from) {
    Preconditions.checkNotNull(from.getId(), "Unsaved session?");
    setTitleTagsDescription(from.getId(), from.getTitle(),
                            from.getTags(),
                            from.getDescription());
  }

  public Note makeANote(Date date, String text, String picturePath) {
    Note note = new Note(date, text, locationHelper.getLastLocation(), picturePath);

    tracker.addNote(note);
    return note;
  }

  public Iterable<Note> getNotes() {
    return getSession().getNotes();
  }

  public void startSensors() {
    if (!state.sensors().started())
    {
      locationHelper.start();

      audioReader.start();
      externalSensors.start();
      state.sensors().start();
    }
  }

  public synchronized void pauseSession()
  {
    if (state.recording().isRecording())
    {
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

  public void stopSensors()
  {
    if (state.recording().isRecording())
    {
      return;
    }
    locationHelper.stop();
    audioReader.stop();
    state.sensors().stop();
  }

  public boolean isRecording()
  {
    return state.recording().isRecording();
  }

  public boolean canSessionHaveNotes()
  {
    return !session.isRealtime();
  }

  public void setContribute(long sessionId, boolean shouldContribute) {
    tracker.setContribute(sessionId, shouldContribute);
  }

  @Subscribe
  public synchronized void onEvent(SensorEvent event)
  {
    double value = event.getValue();
    String sensorName = event.getSensorName();
    Sensor sensor = sensorManager.getSensorByName(sensorName);
    recentMeasurements.put(sensorName, value);

    Location location = getLocation();
    if (location != null && sensor != null && sensor.isEnabled())
    {
      double latitude = location.getLatitude();
      double longitude = location.getLongitude();
      Measurement measurement = new Measurement(latitude, longitude, value, event.getMeasuredValue(), event.getDate());
      if (state.recording().isRecording())
      {
        MeasurementStream stream = prepareStream(event);
        tracker.addMeasurement(sensor, stream, measurement);
      }
      else
      {
        eventBus.post(new MeasurementEvent(measurement, sensor));
      }
    }
  }

  private Location getLocation()
  {
    Location location = locationHelper.getLastLocation();

    if(session.isRealtime())
    {
      location.setLatitude(session.getLatitude());
      location.setLongitude(session.getLongitude());
    }
    else if(session.isLocationless())
    {
      location = new Location("fake");
      location.setLatitude(TOTALLY_FAKE_COORDINATE);
      location.setLongitude(TOTALLY_FAKE_COORDINATE);
    }

    return location;
  }

  private MeasurementStream prepareStream(SensorEvent event)
  {
    String sensorName = event.getSensorName();

    if (!session.hasStream(sensorName)) {
      MeasurementStream stream = event.stream();
      tracker.addStream(stream);
    }

    MeasurementStream stream = session.getStream(sensorName);
    if(stream.isVisible())
    {
      stream.markAs(MeasurementStream.Visibility.VISIBLE_RECONNECTED);
    }

    return stream;
  }

  public Note getNote(int i) {
    return session.getNotes().get(i);
  }

  public void deleteNote(Note note)
  {
    tracker.deleteNote(session, note);
  }

  public int getNoteCount() {
    return session.getNotes().size();
  }

  public void restartSensors() {
    externalSensors.start();
  }

  public Collection<MeasurementStream> getMeasurementStreams() {
    return newArrayList(session.getActiveMeasurementStreams());
  }

  public MeasurementStream getMeasurementStream(String sensorName) {
    return session.getStream(sensorName);
  }

  @VisibleForTesting
  void discardSession()
  {
    Long sessionId = getSession().getId();
    discardSession(sessionId);
  }

  public synchronized double getNow(Sensor sensor) {
    if (state.recording().isRecording()) {
      return tracker.getNow(sensor);
    }
    else {
      if (!recentMeasurements.containsKey(sensor.getSensorName())) {
        return 0;
      }
      return recentMeasurements.get(sensor.getSensorName());
    }
  }

  private void notifyNewSession(Session session) {
    eventBus.post(new SessionChangeEvent(session));
  }

  public void startTimeboxedSession(boolean locationLess)
  {
    startSession(new Session(false), locationLess);
  }

  public void startRealtimeSession(String title, String tags, String description, boolean isIndoor, LatLng latlng)
  {
    Session newSession = new Session(true);
    newSession.setTitle(title);
    newSession.setTags(tags);
    newSession.setDescription(description);
    newSession.setIndoor(isIndoor);

    if(latlng == null) {
      newSession.setLatitude(TOTALLY_FAKE_COORDINATE);
      newSession.setLongitude(TOTALLY_FAKE_COORDINATE);
    }
    else {
      newSession.setLatitude(latlng.latitude);
      newSession.setLongitude(latlng.longitude);
    }

    startSession(newSession, true);
  }

  private void startSession(Session newSession, boolean locationLess)
  {
    eventBus.post(new SessionStartedEvent(getSession()));
    setSession(newSession);
    locationHelper.start();
    startSensors();
    state.recording().startRecording();
    notificationHelper.showRecordingNotification();

    if(!tracker.startTracking(getSession(), locationLess))
      cleanup();
  }

  public void stopSession()
  {
    tracker.stopTracking(getSession());
    locationHelper.stop();
    state.recording().stopRecording();
    notificationHelper.hideRecordingNotification();
    eventBus.post(new SessionStoppedEvent(getSession()));
  }

  public void finishSession(long sessionId) {
    synchronized (this) {
      tracker.complete(sessionId);
      Intents.triggerSync(applicationContext);
    }
    cleanup();
  }

  public void discardSession(long sessionId) {
      tracker.discard(sessionId);
      cleanup();
  }

  public void resetSession(long sessionId) {
      tracker.stopTracking();
      cleanup();
  }

  public void deleteSession()
  {
    Long sessionId = session.getId();
    sessionRepository.markSessionForRemoval(sessionId);
    discardSession(sessionId);
  }

  private void cleanup() {
    locationHelper.stop();
    state.recording().stopRecording();
    setSession(new Session());
    notificationHelper.hideRecordingNotification();
  }

  public boolean isSessionStarted() {
    return state.recording().isRecording();
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

  public void deleteSensorStream(Sensor sensor)
  {
    String sensorName = sensor.getSensorName();
    deleteSensorStream(sensorName);
  }

  void deleteSensorStream(String sensorName)
  {
    MeasurementStream stream = getMeasurementStream(sensorName);
    if(stream == null)
    {
      Logger.w("No stream for sensor [" + sensorName + "]");
      return;
    }
    sessionRepository.deleteStream(session, stream);
    session.removeStream(stream);
  }

  public boolean isLocationless()
  {
    return session.isLocationless();
  }

  public void setTitleTagsDescription(long sessionId, String title, String tags, String description)
  {
    tracker.setTitle(sessionId, title);
    tracker.setTags(sessionId, tags);
    tracker.setDescription(sessionId, description);
  }

  public void updateNote(final Note currentNote)
  {
    dbQueue.add(new WritableDatabaseTask<Void>()
    {
      @Override
      public Void execute(SQLiteDatabase writableDatabase)
      {
        ContentValues values = new ContentValues();
        values.put(DBConstants.NOTE_TEXT, currentNote.getText());
        @Language("SQLite")
        String whereClause = " WHERE " + DBConstants.NOTE_NUMBER + " = " + currentNote.getNumber() + " AND " + DBConstants.NOTE_SESSION_ID + " = " + session.getId();
        writableDatabase.update(DBConstants.NOTE_TABLE_NAME, values, whereClause, null);
        return null;
      }
    });
  }
}
