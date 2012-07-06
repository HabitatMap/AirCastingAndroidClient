package pl.llp.aircasting.repository.db;

import android.database.sqlite.SQLiteDatabase;

public interface WritableDatabaseTask<T> extends DatabaseTask<T>
{
  T execute(SQLiteDatabase writableDatabase);
}
