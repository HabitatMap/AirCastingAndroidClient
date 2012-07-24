package pl.llp.aircasting.activity.extsens;

import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.ExternalSensorDescriptor;

import android.content.Context;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

public class KnownSensorAdapter extends SensorAdapter
{
  SettingsHelper settingsHelper;

  KnownSensorAdapter(Context context, SettingsHelper settingsHelper)
  {
    super(context);
    this.settingsHelper = settingsHelper;
  }

  public boolean knows(String address)
  {
    return containsAddress(address);
  }

  private boolean containsAddress(String address)
  {
    for (Map<String, String> keyValues : data)
    {
      if (address.equalsIgnoreCase(keyValues.get(ADDRESS)))
      {
        return true;
      }
    }
    return false;
  }

  @Override
  public Map<String, String> remove(int position)
  {
    Map<String, String> removed = super.remove(position);
    updateSettings();
    return removed;
  }

  private void updateSettings()
  {
    Iterator<Map<String,String>> iterator = data.iterator();
    List<ExternalSensorDescriptor> sensors = newArrayList();
    while (iterator.hasNext())
    {
      Map<String, String> next = iterator.next();
      String address = next.get(ADDRESS);
      String name = (next.get(NAME));
      sensors.add(sensor(name, address));
    }
    settingsHelper.setExternalSensors(sensors);
    notifyDataSetChanged();
  }

  public void addSensor(String name, String address)
  {
    ExternalSensorDescriptor sensor = sensor(name, address);
    if(!containsAddress(address))
    {
      data.add(sensor.asMap());
    }
    updateSettings();
  }

  ExternalSensorDescriptor sensor(String name, String address)
  {
    return new ExternalSensorDescriptor(name, address);
  }

  public void updatePreviouslyConnected(List<ExternalSensorDescriptor> descriptors)
  {
    for (ExternalSensorDescriptor sensor : descriptors)
    {
      if(!containsAddress(sensor.getAddress()))
      {
        Map<String, String> item = newHashMap();
        item.put(ADDRESS, sensor.getAddress());
        item.put(NAME, sensor.getName());

        data.add(item);
      }
    }

    notifyDataSetChanged();
  }

}


