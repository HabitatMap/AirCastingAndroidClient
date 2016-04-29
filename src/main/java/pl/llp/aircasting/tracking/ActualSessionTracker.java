package pl.llp.aircasting.tracking;

import pl.llp.aircasting.helper.MetadataHelper;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.*;
import pl.llp.aircasting.model.events.RealtimeMeasurementEvent;
import pl.llp.aircasting.storage.DatabaseTaskQueue;
import pl.llp.aircasting.storage.SessionPropertySetter;
import pl.llp.aircasting.storage.db.DBConstants;
import pl.llp.aircasting.storage.db.WritableDatabaseTask;
import pl.llp.aircasting.storage.repository.SessionRepository;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import com.google.common.eventbus.EventBus;

import java.util.Date;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static pl.llp.aircasting.storage.db.DBConstants.*;

public class ActualSessionTracker implements SessionTracker
{
  public static final long MEASUREMENTS_INTERVAL = 60000;

  private final Session session;
  private final SettingsHelper settingsHelper;
  private final MetadataHelper metadataHelper;
  private final EventBus eventBus;
  final DatabaseTaskQueue dbQueue;

  final NoteTracker noteTracker;
  final SessionRepository sessions;
  MeasurementTracker measurementTracker;

  final SessionPropertySetter setter;

  private Map<String, Long> pendingMeasurements = newHashMap();

  ActualSessionTracker(EventBus eventBus, final Session session, DatabaseTaskQueue dbQueue, SettingsHelper settingsHelper, MetadataHelper metadataHelper, SessionRepository sessions, boolean locationLess)
  {
    this.session = session;
    this.settingsHelper = settingsHelper;
    this.metadataHelper = metadataHelper;
    this.dbQueue = dbQueue;
    this.sessions = sessions;
    this.setter = new SessionPropertySetter(dbQueue);
    this.eventBus = eventBus;

    this.noteTracker = new ActualNoteTracker(eventBus, dbQueue);
    this.measurementTracker = new ActualMeasurementTracker(dbQueue);

    session.setStart(new Date());

    session.setLocationless(this.settingsHelper.areMapsDisabled() || locationLess);
    session.setCalibration(this.settingsHelper.getCalibration());
    session.setOffset60DB(this.settingsHelper.getOffset60DB());

    session.setOsVersion(this.metadataHelper.getOSVersion());
    session.setPhoneModel(this.metadataHelper.getPhoneModel());
  }

  @Override
  public void addNote(Note note)
  {
    noteTracker.addNote(session, note);
  }

  @Override
  public void finishTracking()
  {
    session.setEnd(new Date());
  }

  @Override
  public void setTitle(long sessionId, String title)
  {
    session.setTitle(title);
    setter.forSession(sessionId).key(DBConstants.SESSION_TITLE).value(title).doSet();
  }

  @Override
  public void setTags(long sessionId, String tags)
  {
    session.setTags(tags);
    setter.forSession(sessionId).key(SESSION_TAGS).value(tags).doSet();
  }

  @Override
  public void setDescription(long sessionId, String description)
  {
    session.setDescription(description);
    setter.forSession(sessionId).key(DBConstants.SESSION_DESCRIPTION).value(description).doSet();
  }

  @Override
  public void setContribute(long sessionId, boolean shouldContribute)
  {
    session.setContribute(shouldContribute);
    setter.forSession(sessionId).key(DBConstants.SESSION_CONTRIBUTE).value(shouldContribute).doSet();
  }

  @Override
  public void addStream(final MeasurementStream stream)
  {
    session.add(stream);
    dbQueue.add(sessions.addStreamTask(stream, session));
  }

  @Override
  public void addMeasurement(MeasurementStream stream, Measurement measurement)
  {
    long currentTime = System.currentTimeMillis();
    Long lastMeauserementTime = pendingMeasurements.get(stream.getSensorName());

    if(lastMeauserementTime == null || lastMeauserementTime < currentTime - MEASUREMENTS_INTERVAL) {
      pendingMeasurements.put(stream.getSensorName(), currentTime);

      measurementTracker.add(stream, measurement);
      RealtimeMeasurement realtimeMeasurement = new RealtimeMeasurement(this.session.getUUID(), stream, measurement);
      eventBus.post(new RealtimeMeasurementEvent(realtimeMeasurement));
    }
  }

  @Override
  public void delete(long session)
  {
    sessions.deleteCompletely(session);
  }

  @Override
  public void save(final Session session)
  {
    dbQueue.add(new WritableDatabaseTask<Void>()
    {
      @Override
      public Void execute(SQLiteDatabase writableDatabase)
      {
        ContentValues values = new ContentValues();

        values.put(SESSION_START, session.getStart().getTime());
        values.put(SESSION_UUID, session.getUUID().toString());
        values.put(SESSION_CALIBRATION, session.getCalibration());
        values.put(SESSION_PHONE_MODEL, session.getPhoneModel());
        values.put(SESSION_OS_VERSION, session.getOSVersion());
        values.put(SESSION_OFFSET_60_DB, session.getOffset60DB());
        values.put(SESSION_MARKED_FOR_REMOVAL, 0);
        values.put(SESSION_SUBMITTED_FOR_REMOVAL, 0);
        values.put(SESSION_CALIBRATED, 1);
        values.put(SESSION_LOCAL_ONLY, session.isLocationless() ? 1 : 0);
        values.put(SESSION_INCOMPLETE, 1);
        values.put(SESSION_REALTIME, session.isRealtime() ? 1 : 0);

        long sessionKey = writableDatabase.insertOrThrow(SESSION_TABLE_NAME, null, values);
        session.setId(sessionKey);
        return null;
      }
    });
  }
}
