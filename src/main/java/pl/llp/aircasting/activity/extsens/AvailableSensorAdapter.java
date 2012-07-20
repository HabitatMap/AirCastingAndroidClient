package pl.llp.aircasting.activity.extsens;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class AvailableSensorAdapter extends SensorAdapter
{
  Map<String, BluetoothDevice> devices = newHashMap();

  AvailableSensorAdapter(Context context)
  {
    super(context);
  }

  public void deviceFound(BluetoothDevice device)
  {
    String address = device.getAddress();
    String name = device.getName();

    if (!devices.containsKey(address))
    {
      devices.put(address, device);

      Map<String, String> item = newHashMap();
      item.put(ADDRESS, address);
      item.put(NAME, name);

      data.add(item);
      notifyDataSetChanged();
    }
  }

  @Override
  public Map<String, String> remove(int position)
  {
    Map<String, String> removed = super.remove(position);
    devices.remove(removed.get(ADDRESS));
    return removed;
  }
}