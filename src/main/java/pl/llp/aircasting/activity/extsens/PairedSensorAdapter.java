package pl.llp.aircasting.activity.extsens;

import pl.llp.aircasting.model.ExternalSensorDescriptor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public class PairedSensorAdapter extends SensorAdapter
{
  private Map<String, Integer> deviceIndexes = new HashMap<String, Integer>();
  private Set<String> addressesToHide = newHashSet();

  PairedSensorAdapter(Context context)
  {
    super(context, Lists.<Map<String, String>>newArrayList());
    updatePairedDevices();
  }

  Set<BluetoothDevice> getBondedDevices()
  {
    return BluetoothAdapter.getDefaultAdapter().getBondedDevices();
  }

  public Map<String, String> markAsConnected(int position)
  {
    Map<String, String> remove = data.remove(position);
    addressesToHide.add(remove.get(ADDRESS));
    notifyDataSetChanged();
    return remove;
  }

  // call from somewhere
  public void connectionFailedWith(String address)
  {
    addressesToHide.remove(address);
    Set<BluetoothDevice> devices = getBondedDevices();
    for (BluetoothDevice device : devices)
    {
      if(device.getAddress().equalsIgnoreCase(address))
      {
        add(new ExternalSensorDescriptor(device));
      }
    }
  }

  void add(ExternalSensorDescriptor sensor)
  {
    deviceIndexes.put(sensor.getAddress(), data.size());
    data.add(sensor.asMap());
    notifyDataSetChanged();
  }

  public void updatePairedDevices()
  {
    data.clear();
    Set<BluetoothDevice> bondedDevices = getBondedDevices();
    for (BluetoothDevice device : bondedDevices)
    {
      if (!addressesToHide.contains(device.getAddress()))
      {
        add(new ExternalSensorDescriptor(device));
      }
    }
  }

  public void markAsConnected(ExternalSensorDescriptor descriptor)
  {
    int pos = findPosition(descriptor);
    if(pos < 0)
      return;

    markAsConnected(pos);
  }

}