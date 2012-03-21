package pl.llp.aircasting.activity.adapter;

import android.app.Activity;
import android.widget.SimpleAdapter;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import pl.llp.aircasting.R;
import pl.llp.aircasting.event.sensor.SensorEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class StreamAdapter extends SimpleAdapter {
    public static final String NAME = "name";

    private static final int[] TO = new int[]{R.id.name};
    private static final String[] FROM = new String[]{NAME};

    private List<Map<String, String>> data;
    EventBus eventBus;
    private Activity context;

    public StreamAdapter(Activity context, List<Map<String, String>> data, EventBus eventBus) {
        super(context, data, R.layout.stream, FROM, TO);
        this.data = data;
        this.eventBus = eventBus;
        this.context = context;
    }

    public void start() {
        eventBus.register(this);
    }

    public void stop() {
        eventBus.unregister(this);
    }

    @Subscribe
    public void onEvent(SensorEvent event) {
        HashMap<String, String> map = newHashMap();

        map.put(NAME, event.getSensorName());

        data.add(map);
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }
}
