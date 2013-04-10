package pl.llp.aircasting.storage.db;

import android.database.sqlite.SQLiteDatabase;

public interface WritableDatabaseTask<T> extends DatabaseTask<T>
{
  T execute(SQLiteDatabase writableDatabase);
}
