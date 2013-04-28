package pl.llp.aircasting.model.internal;

/**
 * Created by ags on 31/07/12 at 16:54
 */
public class SensorName
{
  private final String name;

  public SensorName(String name)
  {
    this.name = name;
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SensorName that = (SensorName) o;

    if (name != null ? !name.equals(that.name) : that.name != null) return false;

    return true;
  }

  @Override
  public int hashCode()
  {
    return name != null ? name.hashCode() : 0;
  }

  public static SensorName from(String sensorName)
  {
    return new SensorName(sensorName);
  }
}
