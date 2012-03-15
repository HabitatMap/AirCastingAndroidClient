package pl.llp.aircasting.activity.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.widget.SimpleAdapter;
import pl.llp.aircasting.R;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class SensorAdapter extends SimpleAdapter {
    public static final String ADDRESS = "address";

    public static final String[] KEYS = new String[]{ADDRESS};
    public static final int[] FIELDS = new int[]{R.id.address};

    private List<Map<String, String>> data;

    protected SensorAdapter(Context context, List<Map<String, String>> data) {
        super(context, data, R.layout.external_sensor_item, KEYS, FIELDS);
        this.data = data;
    }

    public void deviceFound(BluetoothDevice device) {
        Map<String, String> item = newHashMap();
        item.put(ADDRESS, device.getAddress());
        data.add(item);

        notifyDataSetChanged();
    }
}
