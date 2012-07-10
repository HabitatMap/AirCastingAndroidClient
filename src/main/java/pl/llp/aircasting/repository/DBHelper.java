package pl.llp.aircasting.repository;

import android.database.Cursor;

import java.util.Date;

public class DBHelper
{
  static double getDouble(Cursor cursor, String columnName) {
      return cursor.getDouble(cursor.getColumnIndex(columnName));
  }

  public static Date getDate(Cursor cursor, String columnName) {
      return new Date(cursor.getLong(cursor.getColumnIndex(columnName)));
  }

  static String getString(Cursor cursor, String columnName) {
      return cursor.getString(cursor.getColumnIndex(columnName));
  }

  static Long getLong(Cursor cursor, String columnName) {
      return cursor.getLong(cursor.getColumnIndex(columnName));
  }

  static int getInt(Cursor cursor, String columnName) {
      return cursor.getInt(cursor.getColumnIndex(columnName));
  }

  static boolean getBool(Cursor cursor, String columnName) {
      return cursor.getInt(cursor.getColumnIndex(columnName)) == 1;
  }
}
