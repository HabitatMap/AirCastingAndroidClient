package pl.llp.aircasting.storage.db;

import pl.llp.aircasting.util.Constants;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import com.google.inject.Singleton;

@Singleton
public class AirCastingDB extends SQLiteOpenHelper implements DBConstants
{
  private static volatile SQLiteDatabase db;
  private static volatile Throwable lockedAt;

  public AirCastingDB(Context context)
  {
    super(context, DB_NAME, null, DB_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db)
  {
    new SchemaCreator().create(db);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
  {
    new SchemaMigrator().migrate(db, oldVersion, newVersion);
  }

  @Override
  public synchronized SQLiteDatabase getWritableDatabase()
  {
    throw new RuntimeException("Don't use me!");
  }

  @Override
  public synchronized SQLiteDatabase getReadableDatabase()
  {
    throw new RuntimeException("Don't use me!");
  }

  @VisibleForTesting
  public SQLiteDatabase getDatabaseDuringTests()
  {
    return getDatabase();
  }

  private synchronized SQLiteDatabase getDatabase()
  {
    if(db == null || !db.isOpen())
    {
      db = super.getWritableDatabase();
      lockedAt = new Throwable();
    }

    if(db.isDbLockedByOtherThreads())
    {
      Log.v("DATABASE!", "Database is locked: ", new Throwable());
      Log.v("DATABASE!", "Locked at: ", lockedAt);
    }

    return db;
  }

  public synchronized <T> T executeWritableTask(WritableDatabaseTask<T> task)
  {
    SQLiteDatabase database = getDatabase();

    T result;
    database.beginTransaction();
    try
    {
      result = measureExecution(task, database);
      database.setTransactionSuccessful();
    }
    finally
    {
      database.endTransaction();
    }

    return result;
  }

  public synchronized <T> T executeReadOnlyTask(ReadOnlyDatabaseTask<T> task)
  {
    SQLiteDatabase database = getDatabase();

    T result;
    result = measureExecution(task, database);

    return result;
  }

  private <T> T measureExecution(DatabaseTask<T> task, SQLiteDatabase database)
  {
    Stopwatch stopwatch = new Stopwatch().start();
    T result = task.execute(database);

    Constants.logDbPerformance("Database task took: " + stopwatch.elapsedMillis());
    return result;
  }
}

