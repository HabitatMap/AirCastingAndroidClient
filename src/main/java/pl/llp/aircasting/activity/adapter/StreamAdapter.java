package pl.llp.aircasting.activity.adapter;

import android.app.Activity;
import android.widget.SimpleAdapter;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import pl.llp.aircasting.R;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.model.SessionManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class StreamAdapter extends SimpleAdapter {
    public static final String TITLE = "title";
    public static final String NOW = "now";

    private static final int[] TO = new int[]{R.id.title, R.id.db_now};
    private static final String[] FROM = new String[]{TITLE, NOW};

    private List<Map<String, String>> data;
    private Map<String, Map<String, String>> sensors = newHashMap();

    private Activity context;
    private SessionManager sessionManager;
    EventBus eventBus;

    public StreamAdapter(Activity context, List<Map<String, String>> data, EventBus eventBus, SessionManager sessionManager) {
        super(context, data, R.layout.stream, FROM, TO);
        this.data = data;
        this.eventBus = eventBus;
        this.context = context;
        this.sessionManager = sessionManager;
    }

    public void start() {
        eventBus.register(this);
    }

    public void stop() {
        eventBus.unregister(this);
    }

    @Subscribe
    public void onEvent(final SensorEvent event) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                update(event);
            }
        });
    }

    private void update(SensorEvent event) {
        String name = event.getSensorName();
        if (!sensors.containsKey(name)) {
            HashMap<String, String> map = new HashMap<String, String>();
            sensors.put(name, map);
            data.add(map);
        }
        Map<String, String> map = sensors.get(name);

        map.put(TITLE, name);
        map.put(NOW, String.valueOf((int)sessionManager.getNow(name)));

        notifyDataSetChanged();
    }
}
