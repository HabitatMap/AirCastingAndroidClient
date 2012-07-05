package pl.llp.aircasting.repository.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.google.inject.Singleton;

@Singleton
public class AirCastingDB extends SQLiteOpenHelper implements DBConstants
{
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
    return super.getWritableDatabase();
  }

  @Override
  public synchronized SQLiteDatabase getReadableDatabase()
  {
    return super.getReadableDatabase();
  }

  public <T> T executeReadOnlyDbTask(ReadOnlyDatabaseTask<T> task)
  {
    SQLiteDatabase database = getReadableDatabase();
    T result = task.execute(database);
    database.close();
    return result;
  }
}

