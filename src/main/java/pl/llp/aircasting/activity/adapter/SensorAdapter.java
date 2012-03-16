package pl.llp.aircasting.activity.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.widget.SimpleAdapter;
import pl.llp.aircasting.R;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;

public class SensorAdapter extends SimpleAdapter {
    public static final String ADDRESS = "address";
    public static final String NAME = "name";

    public static final String[] KEYS = new String[]{ADDRESS, NAME};
    public static final int[] FIELDS = new int[]{R.id.address, R.id.name};

    private List<Map<String, String>> data;
    private Set<String> addresses = newHashSet();

    protected SensorAdapter(Context context, List<Map<String, String>> data) {
        super(context, data, R.layout.external_sensor_item, KEYS, FIELDS);
        this.data = data;
    }

    public void deviceFound(BluetoothDevice device) {
        if (!addresses.contains(device.getAddress())) {
            addresses.add(device.getAddress());

            Map<String, String> item = newHashMap();

            item.put(ADDRESS, device.getAddress());
            item.put(NAME, device.getName());

            data.add(item);
            notifyDataSetChanged();
        }
    }
}
