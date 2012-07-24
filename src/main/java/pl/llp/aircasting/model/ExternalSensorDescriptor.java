package pl.llp.aircasting.model;

import pl.llp.aircasting.activity.extsens.SensorAdapter;

import com.google.common.base.Strings;
import com.google.gson.annotations.Expose;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class ExternalSensorDescriptor
{
  @Expose String name;
  @Expose String address;

  public ExternalSensorDescriptor(String name, String address)
  {
    if(Strings.isNullOrEmpty(name))
    {
      name = "Unnamed";
    }
    this.name = name;
    this.address = address;
  }

  public String getAddress()
  {
    return address;
  }

  public String getName()
  {
    return name;
  }

  public Map<String, String> asMap()
  {
    HashMap<String, String> result = newHashMap();
    result.put(SensorAdapter.ADDRESS, address);
    result.put(SensorAdapter.NAME, name);
    return result;
  }
}
