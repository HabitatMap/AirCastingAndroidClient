package pl.llp.aircasting.repository.db;

import android.database.sqlite.SQLiteDatabase;
import com.google.inject.Inject;

public class DBUser
{
  @Inject
  AirCastingDB dbAccessor;
  protected SQLiteDatabase db;

  @Inject
  public  void init(){
    db = dbAccessor.getWritableDatabase();
  }

  public void close(){
    db.close();
  }
}
