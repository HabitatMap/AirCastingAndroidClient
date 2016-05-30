package pl.llp.aircasting.storage.db;

import android.database.sqlite.SQLiteDatabase;
import org.intellij.lang.annotations.Language;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static pl.llp.aircasting.storage.db.DBConstants.*;

public class SchemaCreator
{
  @Language("SQL")
  private static final String CREATE_NOTE_TABLE = "create table " + NOTE_TABLE_NAME + "(" +
      NOTE_SESSION_ID + " integer " +
      ", " + NOTE_LATITUDE + " real " +
      ", " + NOTE_LONGITUDE + " real " +
      ", " + NOTE_TEXT + " text " +
      ", " + NOTE_DATE + " integer " +
      ", " + NOTE_PHOTO + " text" +
      ", " + NOTE_NUMBER + " integer" +
      ")";

  public void create(SQLiteDatabase db)
  {
    db.execSQL(sessionTable().asSQL(DBConstants.DB_VERSION));
    db.execSQL(measurementTable().asSQL(DB_VERSION));
    db.execSQL(streamTable().asSQL(DB_VERSION));
    db.execSQL(CREATE_NOTE_TABLE);
    db.execSQL(regressionTable().asSQL(DB_VERSION));
  }

  Table regressionTable() {
      Table table = new Table(REGRESSION_TABLE_NAME);
      table.setPrimaryKey(new Column(REGRESSION_ID, Datatype.INTEGER));

      table.addColumn(new Column(REGRESSION_COEFFICIENTS, Datatype.TEXT));
      table.addColumn(new Column(REGRESSION_THRESHOLD_LOW, Datatype.INTEGER));
      table.addColumn(new Column(REGRESSION_THRESHOLD_VERY_LOW, Datatype.INTEGER));
      table.addColumn(new Column(REGRESSION_THRESHOLD_HIGH, Datatype.INTEGER));
      table.addColumn(new Column(REGRESSION_THRESHOLD_VERY_HIGH, Datatype.INTEGER));
      table.addColumn(new Column(REGRESSION_THRESHOLD_MEDIUM, Datatype.INTEGER));
      table.addColumn(new Column(REGRESSION_MEASUREMENT_UNIT, Datatype.TEXT));
      table.addColumn(new Column(REGRESSION_MEASUREMENT_SYMBOL, Datatype.TEXT));
      table.addColumn(new Column(REGRESSION_MEASUREMENT_TYPE, Datatype.TEXT));
      table.addColumn(new Column(REGRESSION_SENSOR_NAME, Datatype.TEXT));
      table.addColumn(new Column(REGRESSION_SENSOR_PACKAGE_NAME, Datatype.TEXT));
      table.addColumn(new Column(REGRESSION_SHORT_TYPE, Datatype.TEXT, 32));
      table.addColumn(new Column(REGRESSION_REFERENCE_SENSOR_NAME, Datatype.TEXT, 33));
      table.addColumn(new Column(REGRESSION_REFERENCE_SENSOR_PACKAGE_NAME, Datatype.TEXT, 33));
      table.addColumn(new Column(REGRESSION_IS_OWNER, Datatype.BOOLEAN, 33));
      table.addColumn(new Column(REGRESSION_IS_ENABLED, Datatype.BOOLEAN, 33));
      table.addColumn(new Column(REGRESSION_BACKEND_ID, Datatype.INTEGER, 33));
      table.addColumn(new Column(REGRESSION_CREATED_AT, Datatype.TEXT, 34));
      return table;
  }

  Table measurementTable()
  {
    Table table = new Table(MEASUREMENT_TABLE_NAME);
    table.setPrimaryKey(new Column(MEASUREMENT_ID, Datatype.INTEGER));

    table.addColumn(new Column(MEASUREMENT_LATITUDE, Datatype.REAL));
    table.addColumn(new Column(MEASUREMENT_LONGITUDE, Datatype.REAL));
    table.addColumn(new Column(MEASUREMENT_VALUE, Datatype.REAL));
    table.addColumn(new Column(MEASUREMENT_TIME, Datatype.INTEGER));
    table.addColumn(new Column(MEASUREMENT_STREAM_ID, Datatype.INTEGER));
    table.addColumn(new Column(MEASUREMENT_SESSION_ID, Datatype.INTEGER));
    table.addColumn(new Column(MEASUREMENT_MEASURED_VALUE, Datatype.REAL, 32));
    return table;
  }

  Table streamTable()
  {
    Table table = new Table(STREAM_TABLE_NAME);
    table.setPrimaryKey(new Column(STREAM_ID, Datatype.INTEGER));

    table.addColumn(new Column(STREAM_SESSION_ID, Datatype.INTEGER));
    table.addColumn(new Column(STREAM_AVG, Datatype.REAL));
    table.addColumn(new Column(STREAM_PEAK, Datatype.REAL));
    table.addColumn(new Column(STREAM_SENSOR_NAME, Datatype.TEXT));
    table.addColumn(new Column(STREAM_SENSOR_PACKAGE_NAME, Datatype.TEXT, 27));
    table.addColumn(new Column(STREAM_MEASUREMENT_UNIT, Datatype.TEXT));
    table.addColumn(new Column(STREAM_MEASUREMENT_TYPE, Datatype.TEXT));
    table.addColumn(new Column(STREAM_SHORT_TYPE, Datatype.TEXT, 25));
    table.addColumn(new Column(STREAM_MEASUREMENT_SYMBOL, Datatype.TEXT));
    table.addColumn(new Column(STREAM_THRESHOLD_VERY_LOW, Datatype.INTEGER));
    table.addColumn(new Column(STREAM_THRESHOLD_LOW, Datatype.INTEGER));
    table.addColumn(new Column(STREAM_THRESHOLD_MEDIUM, Datatype.INTEGER));
    table.addColumn(new Column(STREAM_THRESHOLD_HIGH, Datatype.INTEGER));
    table.addColumn(new Column(STREAM_THRESHOLD_VERY_HIGH, Datatype.INTEGER));
    table.addColumn(new Column(STREAM_MARKED_FOR_REMOVAL, Datatype.BOOLEAN, 28));
    table.addColumn(new Column(STREAM_SUBMITTED_FOR_REMOVAL, Datatype.BOOLEAN, 28));
    table.addColumn(new Column(SESSION_LOCAL_ONLY, Datatype.BOOLEAN, 29));
    table.addColumn(new Column(SESSION_INCOMPLETE, Datatype.BOOLEAN, 30));
    return table;
  }

  Table sessionTable()
  {
    Table table = new Table(SESSION_TABLE_NAME);
    table.setPrimaryKey(new Column(SESSION_ID, Datatype.INTEGER));

    table.addColumn(new Column(SESSION_TITLE, Datatype.TEXT));
    table.addColumn(new Column(SESSION_DESCRIPTION, Datatype.TEXT));
    table.addColumn(new Column(SESSION_TAGS, Datatype.TEXT));
    table.addColumn(new Column(SESSION_START, Datatype.INTEGER));
    table.addColumn(new Column(SESSION_END, Datatype.INTEGER));
    table.addColumn(new Column(SESSION_UUID, Datatype.TEXT));
    table.addColumn(new Column(SESSION_LOCATION, Datatype.TEXT));
    table.addColumn(new Column(SESSION_CALIBRATION, Datatype.INTEGER));
    table.addColumn(new Column(SESSION_CONTRIBUTE, Datatype.BOOLEAN));
    table.addColumn(new Column(SESSION_PHONE_MODEL, Datatype.TEXT));
    table.addColumn(new Column(SESSION_OS_VERSION, Datatype.TEXT));
    table.addColumn(new Column(SESSION_OFFSET_60_DB, Datatype.INTEGER));
    table.addColumn(new Column(SESSION_MARKED_FOR_REMOVAL,Datatype.BOOLEAN));
    table.addColumn(new Column(SESSION_SUBMITTED_FOR_REMOVAL, Datatype.BOOLEAN, 21));
    table.addColumn(new Column(SESSION_CALIBRATED, Datatype.BOOLEAN, 26));
    table.addColumn(new Column(SESSION_LOCAL_ONLY, Datatype.BOOLEAN, 29));
    table.addColumn(new Column(SESSION_INCOMPLETE, Datatype.BOOLEAN, 30));
    table.addColumn(new Column(SESSION_REALTIME, Datatype.BOOLEAN, 35));
    table.addColumn(new Column(SESSION_INDOOR, Datatype.BOOLEAN, 36));
    table.addColumn(new Column(SESSION_LATITUDE, Datatype.REAL, 36));
    table.addColumn(new Column(SESSION_LONGITUDE, Datatype.REAL, 36));

    return table;
  }

  public static class Table
  {
    String name;
    private List<Column> columns = newArrayList();
    private Column primaryKey;

    public Table(String name)
    {
      this.name = name;
    }

    public void addColumn(Column column)
    {
      this.columns.add(column);
    }

    public String getName()
    {
      return name;
    }

    public List<Column> getColumns()
    {
      return columns;
    }

    public void setPrimaryKey(Column primaryKey)
    {
      this.primaryKey = primaryKey;
    }

    public Column getPrimaryKey()
    {
      return primaryKey;
    }

    public String asSQL(int revision)
    {
      return new CreateSQLWriter().createSql(this, revision);
    }
  }

  class Column
  {
    String name;
    Datatype datatype;
    int appearedInRevision;

    public Column(String columnName, Datatype datatype)
    {
      this.name = columnName;
      this.datatype = datatype;
    }

    public Column(String name, Datatype datatype, int appearedInRevision)
    {
      this(name, datatype);
      this.appearedInRevision = appearedInRevision;
    }

    public String getName()
    {
      return name;
    }

    public Datatype getDatatype()
    {
      return datatype;
    }

    public int getAppearedInRevision()
    {
      return appearedInRevision;
    }
  }
}
