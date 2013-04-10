package pl.llp.aircasting.storage.db;

import android.database.sqlite.SQLiteDatabase;

interface DatabaseTask<T>
{
  T execute(SQLiteDatabase writableDatabase);
}
