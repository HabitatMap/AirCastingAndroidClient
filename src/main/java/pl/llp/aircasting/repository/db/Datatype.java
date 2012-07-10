package pl.llp.aircasting.repository.db;

/**
 * Created by ags on 10/07/12 at 14:12
 */
public enum Datatype
{
  INTEGER("integer"),
  BOOLEAN("boolean"),
  REAL("real"),
  TEXT("text");

  private String typeName;

  Datatype(String typeName)
  {
    this.typeName = typeName;
  }

  public String getTypeName()
  {
    return typeName;
  }
}
