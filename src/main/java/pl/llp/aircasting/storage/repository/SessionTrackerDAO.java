package pl.llp.aircasting.storage.repository;

import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.storage.db.AirCastingDB;
import pl.llp.aircasting.storage.db.ReadOnlyDatabaseTask;
import pl.llp.aircasting.storage.db.WritableDatabaseTask;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.google.inject.Inject;
import org.intellij.lang.annotations.Language;

import java.util.ArrayList;
import java.util.List;

import static pl.llp.aircasting.storage.DBHelper.getLong;
import static pl.llp.aircasting.storage.db.DBConstants.*;

/**
 * Created by ags on 28/03/2013 at 18:48
 */
public class SessionTrackerDAO
{
  @Inject SessionRepository sessions;
  @Inject StreamRepository streamRepo;
  @Inject AirCastingDB dbAccessor;

  public List<Session> unfinishedSessions()
  {
    List<Long> longs = dbAccessor.executeReadOnlyTask(new ReadOnlyDatabaseTask<List<Long>>()
    {
      @Override
      public List<Long> execute(SQLiteDatabase readOnlyDb)
      {
        List<Long> result = new ArrayList<Long>();
        Cursor cursor = readOnlyDb.rawQuery("SELECT " + SESSION_ID + " FROM " + SESSION_TABLE_NAME
                                                + " WHERE " + SESSION_INCOMPLETE + " = 1", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
          Long sessionId = getLong(cursor, SESSION_ID);
          result.add(sessionId);
          cursor.moveToNext();
        }
        cursor.close();

        return result;
      }
    });

    List<Session> shallowSessions = sessions.loadShallow(longs);
    return shallowSessions;
  }

  public void complete(final long sessionId)
  {
    updateAverages(sessionId);
    fixTimes(sessionId);
    markComplete(sessionId);
  }

  private void updateAverages(final long sessionId)
  {
    List<MeasurementStream> streams = streamRepo.findAllForSession(sessionId);
    for (final MeasurementStream stream : streams)
    {
      final long streamId = stream.getId();
      dbAccessor.executeWritableTask(new WritableDatabaseTask<Object>()
      {
        @Override
        public Object execute(SQLiteDatabase writableDb)
        {
          @Language("SQLite")
          String query = "UPDATE " + STREAM_TABLE_NAME + " \n" +
              "SET " + STREAM_AVG + " = \n  (" +
              "SELECT\n     AVG(" + MEASUREMENT_VALUE + ")\n" +
              "   FROM " + MEASUREMENT_TABLE_NAME + " \n" +
              "   WHERE " + MEASUREMENT_STREAM_ID + " = ?\n" +
              "  ),\n " +
              "  " + STREAM_PEAK + " =\n" +
              "  (\n" +
              "    SELECT\n      MAX(" + MEASUREMENT_VALUE + ")\n" +
              "    FROM " + MEASUREMENT_TABLE_NAME + "\n" +
              "    WHERE " + MEASUREMENT_STREAM_ID + "= ?\n" +
              "  )\n" +
              "WHERE " + STREAM_ID + " = ?";
          writableDb.execSQL(query, new Object[]{streamId, streamId, streamId});
          return null;
        }
      });
    }
  }

  private void markComplete(final long sessionId) {
    dbAccessor.executeWritableTask(new WritableDatabaseTask<Void>()
    {
      @Override
      public Void execute(SQLiteDatabase writableDatabase)
      {
        ContentValues values = new ContentValues();
        values.put(SESSION_INCOMPLETE, 0);
        writableDatabase.update(SESSION_TABLE_NAME, values, SESSION_ID + " = " + sessionId, null);
        return null;
      }

      @Override
      public String toString()
      {
        return String.format("Mark session %d as complete", sessionId);
      }
    });
  }

  public void abandon(final Session unfinished)
  {
    final Long sessionId = unfinished.getId();
    dbAccessor.executeWritableTask(new WritableDatabaseTask<Object>()
    {
      @Override
      public Object execute(SQLiteDatabase writableDb)
      {
        writableDb.delete(SESSION_TABLE_NAME, SESSION_ID + " = " + sessionId, null);
        writableDb.delete(MEASUREMENT_TABLE_NAME, MEASUREMENT_SESSION_ID + " = " + sessionId, null);
        writableDb.delete(STREAM_TABLE_NAME, STREAM_SESSION_ID + " = " + sessionId, null);
        writableDb.delete(NOTE_TABLE_NAME, NOTE_SESSION_ID + " = " + sessionId, null);

        return null;
      }
    });
  }

  public void fixTimes(final long sessionId)
  {
    dbAccessor.executeWritableTask(new WritableDatabaseTask<Void>()
    {
      @Override
      public Void execute(SQLiteDatabase writableDatabase)
      {
        @Language("SQLite")
        String sql = " UPDATE " + SESSION_TABLE_NAME +
            " \n SET " + SESSION_START + " = \n   " +
            "(\n     SELECT MIN(" + MEASUREMENT_TIME + ") " +
            "FROM " + MEASUREMENT_TABLE_NAME + " WHERE " + MEASUREMENT_SESSION_ID + " = " + sessionId + " \n   ), \n " + SESSION_END + " = \n   " +
            "(\n     SELECT MAX(" + MEASUREMENT_TIME + ") FROM " + MEASUREMENT_TABLE_NAME + " WHERE " + MEASUREMENT_SESSION_ID + " = " + sessionId + "\n   ) \n" +
            " WHERE " + SESSION_ID + " = " + sessionId;
        writableDatabase.execSQL(sql);

        return null;
      }

      @Override
      public String toString()
      {
        return String.format("Update times for session %d", sessionId);
      }
    });
  }
}
