package pl.llp.aircasting.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CursorWrapper
{
  Cursor cursor;
  SQLiteDatabase dbConnection;

  CursorWrapper(Cursor cursor, SQLiteDatabase dbConnection)
  {
    this.cursor = cursor;
    this.dbConnection = dbConnection;
  }

  public void close()
  {
    cursor.close();
    dbConnection.close();
  }

  public Cursor getCursor()
  {
    return cursor;
  }

  public boolean isClosed()
  {
    return cursor.isClosed() && !dbConnection.isOpen();
  }
}
