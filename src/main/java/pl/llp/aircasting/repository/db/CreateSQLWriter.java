package pl.llp.aircasting.repository.db;

import java.util.List;

/**
 * Created by ags on 10/07/12 at 14:26
 */
public class CreateSQLWriter
{
  String createSql(SchemaCreator.Table table, int revision)
  {
    StringBuilder builder = new StringBuilder();

    builder.append("CREATE TABLE ");
    builder.append(table.getName());
    builder.append(" (");

    builder.append(createPrimaryKey(table.getPrimaryKey()));

    List<SchemaCreator.Column> columns = table.getColumns();
    for (SchemaCreator.Column column : columns)
    {
      if(column.getAppearedInRevision() > revision)
        continue;

      builder.append(", \n ");
      builder.append(column.getName());
      builder.append(" ");
      builder.append(column.getDatatype().getTypeName());
    }

    builder.append(" )");

    return builder.toString();
  }

  StringBuilder createPrimaryKey(SchemaCreator.Column column)
  {
    StringBuilder b = new StringBuilder(20);
    b.append(column.getName());
    b.append(" ");
    b.append(column.getDatatype().getTypeName());
    b.append(" PRIMARY KEY ");
    return b;
  }

}
