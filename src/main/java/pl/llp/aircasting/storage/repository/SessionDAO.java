package pl.llp.aircasting.storage.repository;

import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.storage.db.AirCastingDB;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.inject.Inject;
import org.intellij.lang.annotations.Language;

import java.util.UUID;

import static pl.llp.aircasting.storage.DBHelper.*;
import static pl.llp.aircasting.storage.db.DBConstants.*;

/**
 * Created by ags on 03/14/13 at 23:44
 */
public class SessionDAO {
    @Inject AirCastingDB dbAccessor;

    @Language("SQLite")
    public static final String SESSIONS_WITH_SENSOR_QUERY =
            "SELECT " + SESSION_TABLE_NAME + ".*" +
                    " FROM " + SESSION_TABLE_NAME +
                    " JOIN " + STREAM_TABLE_NAME +
                    " ON " + SESSION_TABLE_NAME + "." + SESSION_ID + " = " + STREAM_TABLE_NAME + "." + STREAM_SESSION_ID +
                    " WHERE " + STREAM_SENSOR_NAME + " = ?" +
                    " AND " + SESSION_MARKED_FOR_REMOVAL + " = 0 " +
                    " AND " + SESSION_TABLE_NAME + "." + SESSION_INCOMPLETE + " = 0 " +
                    " ORDER BY " + SESSION_START + " DESC";

    public Cursor notDeletedCursor(SQLiteDatabase readOnlyDatabase) {
        Cursor result;
        result = readOnlyDatabase
                .query(SESSION_TABLE_NAME, null, SESSION_MARKED_FOR_REMOVAL + " = 0 AND " + SESSION_INCOMPLETE + " = 0 ", null, null, null, SESSION_START + " DESC");
        return result;
    }

    public void loadDetails(Cursor cursor, Session session) {
        session.setId(getLong(cursor, SESSION_ID));
        session.setTitle(getString(cursor, SESSION_TITLE));
        session.setTags(getString(cursor, SESSION_TAGS));
        session.setStart(getDate(cursor, SESSION_START));
        session.setEnd(getDate(cursor, SESSION_END));
        session.setUuid(UUID.fromString(getString(cursor, SESSION_UUID)));
        session.setLocation(getString(cursor, SESSION_LOCATION));
        session.setCalibration(getInt(cursor, SESSION_CALIBRATION));
        session.setContribute(getBool(cursor, SESSION_CONTRIBUTE));
        session.setMarkedForRemoval(getBool(cursor, SESSION_MARKED_FOR_REMOVAL));
        session.setSubmittedForRemoval(getBool(cursor, SESSION_SUBMITTED_FOR_REMOVAL));
        session.setLocationless(getBool(cursor, SESSION_LOCAL_ONLY));
        session.setType(getString(cursor, SESSION_TYPE));
        session.setIndoor(getBool(cursor, SESSION_INDOOR));
        session.setLatitude(getDouble(cursor, SESSION_LATITUDE));
        session.setLongitude(getDouble(cursor, SESSION_LONGITUDE));
    }

    public ContentValues asValues(Session session) {
        ContentValues values = new ContentValues();

        values.put(SESSION_TITLE, session.getTitle());
        values.put(SESSION_TAGS, session.getTags());
        values.put(SESSION_LOCATION, session.getLocation());
        values.put(SESSION_START, session.getStart().getTime());
        values.put(SESSION_END, session.getEnd().getTime());
        values.put(SESSION_UUID, session.getUUID().toString());
        values.put(SESSION_CALIBRATION, session.getCalibration());
        values.put(SESSION_CONTRIBUTE, session.getContribute());
        values.put(SESSION_MARKED_FOR_REMOVAL, session.isMarkedForRemoval() ? 1 : 0);
        values.put(SESSION_SUBMITTED_FOR_REMOVAL, session.isSubmittedForRemoval() ? 1 : 0);
        values.put(SESSION_CALIBRATED, 1);
        values.put(SESSION_INCOMPLETE, 0);
        values.put(SESSION_LOCAL_ONLY, session.isLocationless() ? 1 : 0);
        values.put(SESSION_TYPE, session.getType());
        values.put(SESSION_INDOOR, session.isIndoor() ? 1 : 0);
        values.put(SESSION_LATITUDE, session.getLatitude());
        values.put(SESSION_LONGITUDE, session.getLongitude());

        return values;
    }
}
