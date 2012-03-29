package pl.llp.aircasting.activity.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import pl.llp.aircasting.R;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.event.ui.ViewStreamEvent;
import pl.llp.aircasting.helper.GaugeHelper;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.SensorManager;
import pl.llp.aircasting.model.SessionManager;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.sort;

public class StreamAdapter extends SimpleAdapter implements View.OnClickListener {
    public static final String TITLE = "title";
    public static final String NOW = "now";
    public static final String AVERAGE = "average";
    public static final String PEAK = "peak";
    public static final String PEAK_LABEL = "peak_label";
    public static final String AVG_LABEL = "avg_label";
    public static final String NOW_LABEL = "now_label";
    public static final String VERY_LOW = "veryLow";
    public static final String LOW = "low";
    public static final String MID = "mid";
    public static final String HIGH = "high";
    public static final String VERY_HIGH = "veryHigh";

    private static final String[] FROM = new String[]{
            TITLE, NOW, AVERAGE, PEAK,
            NOW_LABEL, AVG_LABEL, PEAK_LABEL,
            VERY_LOW, LOW, MID, HIGH, VERY_HIGH
    };
    private static final int[] TO = new int[]{
            R.id.title, R.id.now_gauge, R.id.avg_gauge, R.id.peak_gauge,
            R.id.now_label, R.id.avg_label, R.id.peak_label,
            R.id.top_bar_very_low, R.id.top_bar_low, R.id.top_bar_mid, R.id.top_bar_high, R.id.top_bar_very_high
    };

    private static final Comparator<Map<String, Object>> titleComparator = new Comparator<Map<String, Object>>() {
        @Override
        public int compare(@Nullable Map<String, Object> left, @Nullable Map<String, Object> right) {
            String rightTitle = right.get(TITLE).toString();
            String leftTitle = left.get(TITLE).toString();
            return leftTitle.compareTo(rightTitle);
        }
    };
    public static final String SENSOR = "sensor";

    GaugeHelper gaugeHelper;
    SessionManager sessionManager;
    EventBus eventBus;
    SensorManager sensorManager;

    private List<Map<String, Object>> data;
    private Map<String, Map<String, Object>> sensors = newHashMap();

    private Activity context;

    public StreamAdapter(Activity context, List<Map<String, Object>> data, EventBus eventBus,
                         SessionManager sessionManager, GaugeHelper gaugeHelper, SensorManager sensorManager) {
        super(context, data, R.layout.stream, FROM, TO);
        this.data = data;
        this.eventBus = eventBus;
        this.context = context;
        this.sessionManager = sessionManager;
        this.gaugeHelper = gaugeHelper;
        this.sensorManager = sensorManager;
    }

    /**
     * Start updating the adapter
     */
    public void start() {
        eventBus.register(this);
    }

    /**
     * Stop updating the adapter
     */
    public void stop() {
        eventBus.unregister(this);
    }

    /**
     * Adjust the state of the adapter to incorporate
     * new sensor data.
     */
    @Subscribe
    public void onEvent(SensorEvent event) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                update();
            }
        });
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        Map<String, Object> state = data.get(position);
        Sensor sensor = (Sensor) state.get(SENSOR);
        Integer peak = (Integer) state.get(PEAK);
        Integer avg = (Integer) state.get(AVERAGE);
        Integer now = (Integer) state.get(NOW);
        gaugeHelper.updateGauges(view, sensor, now, avg, peak);

        initializeButtons(view, sensor);
        view.setClickable(true);
        view.setFocusable(true);

        return view;
    }

    private void initializeButtons(View view, Sensor sensor) {
        View recordButton = view.findViewById(R.id.record_stream);
        recordButton.setTag(sensor);
        recordButton.setOnClickListener(this);
        if (sensor.isEnabled()) {
            recordButton.setBackgroundResource(R.drawable.minus_active);
        } else {
            recordButton.setBackgroundResource(R.drawable.minus_regular);
        }

        View viewButton = view.findViewById(R.id.view_stream);
        viewButton.setTag(sensor);
        viewButton.setOnClickListener(this);
        if (sensorManager.getVisibleSensor().equals(sensor)) {
            viewButton.setBackgroundResource(R.drawable.minus_active);
        } else {
            viewButton.setBackgroundResource(R.drawable.minus_regular);
        }
    }

    private void update() {
        data.clear();

        for (Sensor sensor : sensorManager.getSensors()) {
            Map<String, Object> map = prepareItem(sensor);
            String name = sensor.getSensorName();

            map.put(TITLE, title(sensor));
            map.put(SENSOR, sensor);

            map.put(NOW, (int) sessionManager.getNow(name));
            map.put(AVERAGE, (int) sessionManager.getAvg(name));
            map.put(PEAK, (int) sessionManager.getPeak(name));
            map.put(PEAK_LABEL, label(R.string.peak_label_template, sensor));
            map.put(NOW_LABEL, label(R.string.now_label_template, sensor));
            map.put(AVG_LABEL, label(R.string.avg_label_template, sensor));

            data.add(map);
        }

        sort(data, titleComparator);

        notifyDataSetChanged();
    }

    private Map<String, Object> prepareItem(Sensor sensor) {
        String name = sensor.getSensorName();
        if (!sensors.containsKey(name)) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            sensors.put(name, map);
            data.add(map);
        }
        return sensors.get(name);
    }

    private String title(Sensor sensor) {
        StringBuilder builder = new StringBuilder();

        return builder.append(sensor.getMeasurementType())
                .append(" - ")
                .append(sensor.getSensorName())
                .toString();
    }

    private String label(int templateId, Sensor sensor) {
        String template = context.getString(templateId);
        return String.format(template, sensor.getSymbol());
    }

    @Override
    public void onClick(View view) {
        Sensor sensor = (Sensor) view.getTag();

        switch (view.getId()) {
            case R.id.view_stream:
                eventBus.post(new ViewStreamEvent(sensor));
                break;
            case R.id.record_stream:
                sensorManager.toggleSensor(sensor);
                break;
        }

        notifyDataSetChanged();
    }
}
