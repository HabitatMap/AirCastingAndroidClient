package pl.llp.aircasting.activity.adapter;

import pl.llp.aircasting.R;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.widget.SimpleAdapter;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class SensorAdapter extends SimpleAdapter
{
  public static final String ADDRESS = "address";
  public static final String NAME = "name";

  public static final String[] KEYS = new String[]{ADDRESS, NAME};
  public static final int[] FIELDS = new int[]{R.id.address, R.id.name};

  private List<Map<String, String>> data;
  private Map<String, BluetoothDevice> devices = newHashMap();

  SensorAdapter(Context context, List<Map<String, String>> data)
  {
    super(context, data, R.layout.external_sensor_item, KEYS, FIELDS);
    this.data = data;
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

  public void previouslyConnected(String name, String address)
  {
    Map<String, String> item = newHashMap();
    item.put(ADDRESS, address);
    item.put(NAME, name);

    data.add(item);
    notifyDataSetChanged();
  }

  public String getAddress(int position)
  {
    return data.get(position).get(ADDRESS);
  }

  public String getName(int position)
  {
    return data.get(position).get(NAME);
  }
}
