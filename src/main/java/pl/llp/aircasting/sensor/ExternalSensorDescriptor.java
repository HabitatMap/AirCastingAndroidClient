package pl.llp.aircasting.sensor;

import pl.llp.aircasting.activity.extsens.SensorAdapter;

import android.bluetooth.BluetoothDevice;
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
    if (Strings.isNullOrEmpty(name))
    {
      name = "Unnamed";
    }
    this.name = name;
    this.address = address;
  }

  public ExternalSensorDescriptor(BluetoothDevice device)
  {
    this(device.getName(), device.getAddress());
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

  public static ExternalSensorDescriptor from(Map<String, String> map)
  {
    return new ExternalSensorDescriptor(map.get(SensorAdapter.NAME), map.get(SensorAdapter.ADDRESS));
  }

  public boolean matches(Map<String, String> stringStringMap)
  {
    return address.equalsIgnoreCase(stringStringMap.get(SensorAdapter.ADDRESS))
        && name.equalsIgnoreCase(stringStringMap.get(SensorAdapter.NAME));
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ExternalSensorDescriptor that = (ExternalSensorDescriptor) o;

    if (!address.equals(that.address)) return false;

    return true;
  }

  @Override
  public int hashCode()
  {
    return address.hashCode();
  }
}
