package pl.llp.aircasting.storage;

import pl.llp.aircasting.storage.db.DBConstants;
import pl.llp.aircasting.storage.db.WritableDatabaseTask;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by ags on 02/04/2013 at 21:36
 */
public class SessionPropertySetter
{
  DatabaseTaskQueue dbQueue;

  public SessionPropertySetter(DatabaseTaskQueue dbQueue)
  {
    this.dbQueue = dbQueue;
  }

  public NeedsFirstKey forSession(long sessionId)
  {
    return new Builder(sessionId);
  }

  public void set(final long sessionId, final ContentValues toSet)
  {
    dbQueue.add(new WritableDatabaseTask<Void >()
    {
      @Override
      public Void execute(SQLiteDatabase writableDatabase)
      {
        writableDatabase.update(DBConstants.SESSION_TABLE_NAME, toSet, DBConstants.SESSION_ID + " = " + sessionId, null);
        return null;
      }

      @Override
      public String toString()
      {
        return String.format("Session [%d] set [%s]", sessionId, toSet.toString());
      }
    });
  }

  class Builder implements NeedsFirstKey, NeedsKeyOrFinish, NeedsValue
  {
    ContentValues values = new ContentValues();
    String key;
    private long sessionId;

    public Builder(long sessionId)
    {
      this.sessionId = sessionId;
    }

    @Override
    public NeedsValue key(String key)
    {
      this.key = key;
      return this;
    }

    @Override
    public void doSet()
    {
      set(sessionId, values);
    }

    @Override
    public NeedsKeyOrFinish value(Long value)
    {
      values.put(key, value);
      return this;
    }

    @Override
    public NeedsKeyOrFinish value(String value)
    {
      values.put(key, value);
      return this;
    }

    @Override
    public NeedsKeyOrFinish value(boolean value)
    {
      values.put(key, value);
      return this;
    }
  }

  public interface NeedsFirstKey
  {
    NeedsValue key(String key);
  }

  public interface NeedsKeyOrFinish
  {
    NeedsValue key(String key);
    void doSet();
  }

  public interface NeedsValue
  {
    NeedsKeyOrFinish value(Long value);
    NeedsKeyOrFinish value(String value);
    NeedsKeyOrFinish value(boolean shouldContribute);
  }
}
