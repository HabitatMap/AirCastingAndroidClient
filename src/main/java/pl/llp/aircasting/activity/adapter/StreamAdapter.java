package pl.llp.aircasting.activity.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.ButtonsActivity;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.event.ui.ViewStreamEvent;
import pl.llp.aircasting.helper.GaugeHelper;
import pl.llp.aircasting.helper.TopBarHelper;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.SensorManager;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.sort;

public class StreamAdapter extends SimpleAdapter implements View.OnClickListener {
    public static final String TITLE = "title";
    public static final String VERY_LOW = "veryLow";
    public static final String LOW = "low";
    public static final String MID = "mid";
    public static final String HIGH = "high";
    public static final String VERY_HIGH = "veryHigh";

    private static final String[] FROM = new String[]{
            TITLE, VERY_LOW, LOW, MID, HIGH, VERY_HIGH
    };
    private static final int[] TO = new int[]{
            R.id.title, R.id.top_bar_very_low, R.id.top_bar_low, R.id.top_bar_mid, R.id.top_bar_high, R.id.top_bar_very_high
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
    EventBus eventBus;
    TopBarHelper topBarHelper;
    SensorManager sensorManager;
    ButtonsActivity context;

    private List<Map<String, Object>> data;
    private Map<String, Map<String, Object>> sensors = newHashMap();


    public StreamAdapter(ButtonsActivity context, List<Map<String, Object>> data, EventBus eventBus,
                         GaugeHelper gaugeHelper, TopBarHelper topBarHelper, SensorManager sensorManager) {
        super(context, data, R.layout.stream, FROM, TO);
        this.data = data;
        this.eventBus = eventBus;
        this.context = context;
        this.gaugeHelper = gaugeHelper;
        this.topBarHelper = topBarHelper;
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

        gaugeHelper.updateGauges(sensor, view);
        topBarHelper.updateTopBar(sensor, view);

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

            map.put(TITLE, title(sensor));
            map.put(SENSOR, sensor);

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

        context.suppressNextTap();
        notifyDataSetChanged();
    }
}
