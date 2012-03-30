package pl.llp.aircasting.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ags on 28/03/12 at 16:23
 */
public class AirCastingDB extends SQLiteOpenHelper implements DBConstants
{
  public AirCastingDB(Context context) {
    super(context, DB_NAME, null, DB_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db)
  {
    new SchemaMigrator().create(db);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
  {
    new SchemaMigrator().migrate(db, oldVersion, newVersion);
  }
}
