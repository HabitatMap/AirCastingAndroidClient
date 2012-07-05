package pl.llp.aircasting.repository.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Singleton;

@Singleton
public class AirCastingDB extends SQLiteOpenHelper implements DBConstants
{
  private static volatile SQLiteDatabase db;

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

  private SQLiteDatabase getDatabase()
  {
    if(db == null || !db.isOpen())
    {
      db = super.getWritableDatabase();
    }

    if(db.isDbLockedByOtherThreads() || db.isDbLockedByCurrentThread())
    {
      Log.e("DATABASE!", "Database is locked");
    }

    return db;
  }

  public synchronized <T> T executeDbTask(DatabaseTask<T> task)
  {
    SQLiteDatabase database = getDatabase();

    T result;

    result = task.execute(database);

    Log.e("DATABASE!", "Database is locked: " + database.isOpen());

    return result;
  }
}

