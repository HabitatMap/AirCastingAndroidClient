package pl.llp.aircasting.tracking;

import pl.llp.aircasting.event.measurements.MobileMeasurementEvent;
import pl.llp.aircasting.screens.common.helpers.SettingsHelper;
import pl.llp.aircasting.model.*;
import pl.llp.aircasting.storage.DatabaseTaskQueue;
import pl.llp.aircasting.storage.SessionPropertySetter;
import pl.llp.aircasting.storage.db.DBConstants;
import pl.llp.aircasting.storage.db.WritableDatabaseTask;
import pl.llp.aircasting.storage.repository.SessionRepository;
import pl.llp.aircasting.util.Constants;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import com.google.common.eventbus.EventBus;

import java.util.Date;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static pl.llp.aircasting.storage.db.DBConstants.*;

public class ActualSessionTracker implements SessionTracker {
    protected final Session session;
    protected final SettingsHelper settingsHelper;
    protected final MetadataHelper metadataHelper;
    protected final EventBus eventBus;
    final DatabaseTaskQueue dbQueue;

    final NoteTracker noteTracker;
    final SessionRepository sessions;
    MeasurementTracker measurementTracker;

    final SessionPropertySetter setter;

    protected Map<String, Double> recentMeasurements = newHashMap();

    ActualSessionTracker(EventBus eventBus, final Session session, DatabaseTaskQueue dbQueue, SettingsHelper settingsHelper, MetadataHelper metadataHelper, SessionRepository sessions, boolean locationLess) {
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

        session.setOsVersion(this.metadataHelper.getOSVersion());
    }

    @Override
    public void addNote(Note note) {
        noteTracker.addNote(session, note);
    }

    @Override
    public void finishTracking() {
        session.setEnd(new Date());
    }

    @Override
    public void setTitle(long sessionId, String title) {
        session.setTitle(title);
        setter.forSession(sessionId).key(DBConstants.SESSION_TITLE).value(title).doSet();
    }

    @Override
    public void setTags(long sessionId, String tags) {
        session.setTags(tags);
        setter.forSession(sessionId).key(SESSION_TAGS).value(tags).doSet();
    }

    @Override
    public void setDescription(long sessionId, String description) {
        session.setDescription(description);
        setter.forSession(sessionId).key(DBConstants.SESSION_DESCRIPTION).value(description).doSet();
    }

    @Override
    public void setContribute(long sessionId, boolean shouldContribute) {
        session.setContribute(shouldContribute);
        setter.forSession(sessionId).key(DBConstants.SESSION_CONTRIBUTE).value(shouldContribute).doSet();
    }

    @Override
    public void addStream(final MeasurementStream stream) {
        session.add(stream);
        dbQueue.add(sessions.addStreamTask(stream, session));
    }

    @Override
    public void addMeasurement(Sensor sensor, MeasurementStream stream, Measurement measurement) {
        measurementTracker.add(stream, measurement);
        recentMeasurements.put(stream.getSensorName(), measurement.getValue());
        eventBus.post(new MobileMeasurementEvent(measurement, sensor, Constants.CURRENT_SESSION_FAKE_ID));
    }

    @Override
    public void delete(long session) {
        sessions.deleteCompletely(session);
    }

    @Override
    public boolean save(final Session session) {
        if (!beforeSave(session))
            return false;

        saveToDb(session);
        return true;
    }

    @Override
    public synchronized double getNow(Sensor sensor) {
        if (!recentMeasurements.containsKey(sensor.getSensorName())) {
            return 0;
        }
        return recentMeasurements.get(sensor.getSensorName());
    }

    protected boolean beforeSave(final Session session) {
        return true;
    }

    protected void saveToDb(final Session session) {
        dbQueue.add(new WritableDatabaseTask<Void>() {
            @Override
            public Void execute(SQLiteDatabase writableDatabase) {
                ContentValues values = new ContentValues();

                values.put(SESSION_START, session.getStart().getTime());
                values.put(SESSION_UUID, session.getUUID().toString());
                values.put(SESSION_CALIBRATION, session.getCalibration());
                values.put(SESSION_OS_VERSION, session.getOSVersion());
                values.put(SESSION_MARKED_FOR_REMOVAL, 0);
                values.put(SESSION_SUBMITTED_FOR_REMOVAL, 0);
                values.put(SESSION_CALIBRATED, 1);
                values.put(SESSION_LOCAL_ONLY, session.isLocationless() ? 1 : 0);
                values.put(SESSION_INCOMPLETE, 1);
                values.put(SESSION_TYPE, session.getType());
                values.put(SESSION_TITLE, session.getTitle());
                values.put(SESSION_DESCRIPTION, session.getDescription());
                values.put(SESSION_TAGS, session.getTags());
                values.put(SESSION_INDOOR, session.isIndoor() ? 1 : 0);
                values.put(SESSION_LATITUDE, session.getLatitude());
                values.put(SESSION_LONGITUDE, session.getLongitude());

                long sessionKey = writableDatabase.insertOrThrow(SESSION_TABLE_NAME, null, values);
                session.setId(sessionKey);
                return null;
            }
        });
    }
}
