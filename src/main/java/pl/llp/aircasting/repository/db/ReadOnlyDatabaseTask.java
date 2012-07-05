package pl.llp.aircasting.repository.db;

import android.database.sqlite.SQLiteDatabase;

/**
 * Created by ags on 05/07/12 at 15:07
 */
public interface ReadOnlyDatabaseTask<T>
{
  T execute(SQLiteDatabase readOnlyDatabase);
}
