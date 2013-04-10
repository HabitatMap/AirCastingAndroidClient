package pl.llp.aircasting.storage;

import android.database.Cursor;

import java.util.Date;

public class DBHelper
{
  public static double getDouble(Cursor cursor, String columnName) {
      return cursor.getDouble(cursor.getColumnIndex(columnName));
  }

  public static Date getDate(Cursor cursor, String columnName) {
      return new Date(cursor.getLong(cursor.getColumnIndex(columnName)));
  }

  public static String getString(Cursor cursor, String columnName) {
      return cursor.getString(cursor.getColumnIndex(columnName));
  }

  public static Long getLong(Cursor cursor, String columnName) {
      return cursor.getLong(cursor.getColumnIndex(columnName));
  }

  public static int getInt(Cursor cursor, String columnName) {
      return cursor.getInt(cursor.getColumnIndex(columnName));
  }

  public static boolean getBool(Cursor cursor, String columnName) {
      return cursor.getInt(cursor.getColumnIndex(columnName)) == 1;
  }
}
