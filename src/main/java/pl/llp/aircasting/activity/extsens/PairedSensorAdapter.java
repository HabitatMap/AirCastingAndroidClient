package pl.llp.aircasting.activity.extsens;

import pl.llp.aircasting.sensor.ExternalSensorDescriptor;
import pl.llp.aircasting.util.Constants;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public class PairedSensorAdapter extends SensorAdapter
{
  private Set<String> addressesToHide = newHashSet();

  private long lastUpdate = 0;

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
    data.add(sensor.asMap());
    notifyDataSetChanged();
  }

  void updatePairedDevices()
  {
    Set<BluetoothDevice> bondedDevices = getBondedDevices();
    for (BluetoothDevice device : bondedDevices)
    {
      String address = device.getAddress();
      if (!addressesToHide.contains(address) && !contains(address))
      {
        add(new ExternalSensorDescriptor(device));
      }
    }

    for (Iterator<Map<String, String>> iterator = data.iterator(); iterator.hasNext(); )
    {
      Map<String, String> deviceMap = iterator.next();
      String address = deviceMap.get(ADDRESS);
      boolean found = false;
      for (BluetoothDevice bondedDevice : bondedDevices)
      {
        if (bondedDevice.getAddress().equalsIgnoreCase(address))
        {
          found = true;
          break;
        }
      }
      if(!found)
      {
        iterator.remove();
        notifyDataSetChanged();
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

  @Override
  public ExternalSensorDescriptor get(int position)
  {
    return super.get(position);
  }

  public void updateIfNecessary()
  {
    long current = System.currentTimeMillis();
    if(current - lastUpdate > Constants.ONE_SECOND)
    {
      lastUpdate = current;
      updatePairedDevices();
    }
  }
}