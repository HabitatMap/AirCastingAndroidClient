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
    public static final String TITLE = "title";

    private static final int[] TO = new int[]{R.id.title};
    private static final String[] FROM = new String[]{TITLE};

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
        final HashMap<String, String> map = newHashMap();

        map.put(TITLE, event.getSensorName());

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                data.add(map);
                notifyDataSetChanged();
            }
        });
    }
}
