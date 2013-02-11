package pl.llp.aircasting.repository.db;

import android.database.sqlite.SQLiteDatabase;
import org.intellij.lang.annotations.Language;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static pl.llp.aircasting.repository.db.DBConstants.*;

public class SchemaCreator
{
  @Language("SQL")
  private static final String CREATE_MEASUREMENT_TABLE = "create table " + MEASUREMENT_TABLE_NAME +
      " (" + MEASUREMENT_ID + " integer primary key" +
      ", " + MEASUREMENT_LATITUDE + " real" +
      ", " + MEASUREMENT_LONGITUDE + " real" +
      ", " + MEASUREMENT_VALUE + " real" +
      ", " + MEASUREMENT_TIME + " integer" +
      ", " + MEASUREMENT_STREAM_ID + " integer" +
      ", " + MEASUREMENT_SESSION_ID + " integer" +
      ")";

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
    db.execSQL(CREATE_MEASUREMENT_TABLE);
    db.execSQL(streamTable().asSQL(DB_VERSION));
    db.execSQL(CREATE_NOTE_TABLE);
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
