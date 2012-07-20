package pl.llp.aircasting.activity.extsens;

import pl.llp.aircasting.R;

import android.content.Context;
import android.widget.SimpleAdapter;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

public class SensorAdapter extends SimpleAdapter
{
  public static final String ADDRESS = "address";
  public static final String NAME = "name";

  public static final String[] KEYS = new String[]{ADDRESS, NAME};
  public static final int[] FIELDS = new int[]{R.id.address, R.id.name};

  protected List<Map<String, String>> data;

  SensorAdapter(Context context)
  {
    this(context, Lists.<Map<String, String>>newArrayList());
  }

  SensorAdapter(Context context, List<Map<String, String>> data)
  {
    super(context, data, R.layout.external_sensor_item, KEYS, FIELDS);
    this.data = data;
  }

  public String getAddress(int position)
  {
    return data.get(position).get(ADDRESS);
  }

  public String getName(int position)
  {
    return data.get(position).get(NAME);
  }

  public Map<String, String> remove(int position)
  {
    Map<String, String> remove = data.remove(position);
    notifyDataSetChanged();
    return remove;
  }
}
