package pl.llp.aircasting.activity.adapter;

import com.github.mikephil.charting.charts.LineChart;
import com.google.common.collect.ComparisonChain;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.DashboardBaseActivity;
import pl.llp.aircasting.helper.DashboardChartManager;
import pl.llp.aircasting.helper.NoOp;
import pl.llp.aircasting.helper.StreamViewHelper;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.SensorManager;
import pl.llp.aircasting.model.SessionManager;
import pl.llp.aircasting.model.events.SensorEvent;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.sort;

public class StreamAdapter extends SimpleAdapter {
    public static final String QUANTITY = "quantity";
    public static final String SENSOR_NAME = "sensorName";
    public static final String SENSOR = "sensor";

    private static final String[] FROM = new String[]{
            QUANTITY, SENSOR_NAME
    };

    private static final int[] TO = new int[]{
            R.id.quantity, R.id.sensor_name
    };

    private final Comparator<Map<String, Object>> titleComparator = new Comparator<Map<String, Object>>() {
        @Override
        public int compare(@Nullable Map<String, Object> left, @Nullable Map<String, Object> right) {
            Sensor leftSensor = (Sensor) left.get(SENSOR);
            Sensor rightSensor = (Sensor) right.get(SENSOR);
            return ComparisonChain.start()
                    .compare(leftSensor.getSensorName(), rightSensor.getSensorName()).result();
        }
    };

    private final Comparator<Map<String, Object>> positionComparator = new Comparator<Map<String, Object>>() {
        @Override
        public int compare(@Nullable Map<String, Object> left, @Nullable Map<String, Object> right) {
            return ComparisonChain.start()
                    .compare(getPosition(left), getPosition(right)).result();
        }
    };

    SessionManager sessionManager;
    SensorManager sensorManager;
    StreamViewHelper streamViewHelper;
    DashboardChartManager dashboardChartManager;

    DashboardBaseActivity context;
    EventBus eventBus;

    private List<Map<String, Object>> data;
    private Map<String, Map<String, Object>> sensors = newHashMap();
    private LineChart chart;

    // these are static to retain after activity recreation
    private static Map<String, Integer> positions = newHashMap();
    private boolean streamsReordered = false;


    public StreamAdapter(DashboardBaseActivity context, List<Map<String, Object>> data, EventBus eventBus,
                         StreamViewHelper streamViewHelper, SensorManager sensorManager, SessionManager sessionManager, DashboardChartManager dashboardChartManager) {
        super(context, data, R.layout.stream_row, FROM, TO);
        this.data = data;
        this.eventBus = eventBus;
        this.context = context;
        this.sensorManager = sensorManager;
        this.sessionManager = sessionManager;
        this.streamViewHelper = streamViewHelper;
        this.dashboardChartManager = dashboardChartManager;
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

    public void resetAllStaticCharts() {
        dashboardChartManager.resetAllStaticCharts();
    }

    public void resetDynamicCharts() {
        dashboardChartManager.resetDynamicCharts(sensors.keySet());
    }

    private void resetSwappedCharts(String sensor1, String sensor2) {
        String[] sensorNames = new String[2];
        sensorNames[0] = sensor1;
        sensorNames[1] = sensor2;

        dashboardChartManager.resetSpecificStaticCharts(sensorNames);
    }

    @Subscribe
    public void onEvent(final SensorEvent event) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                update();
            }
        });
    }

    public void forceUpdate() {
        update();
    }

    public void swapPositions(int pos1, int pos2) {
        Sensor s1 = (Sensor) data.get(pos1).get(SENSOR);
        Sensor s2 = (Sensor) data.get(pos2).get(SENSOR);
        positions.put(s1.toString(), pos2);
        positions.put(s2.toString(), pos1);

        resetSwappedCharts(s1.getSensorName(), s2.getSensorName());

        streamsReordered = true;
        update();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        Map<String, Object> state = data.get(position);
        final Sensor sensor = (Sensor) state.get(SENSOR);
        chart = (LineChart) view.findViewById(R.id.chart);

        streamViewHelper.updateMeasurements(sensor, view, position);
        dashboardChartManager.drawChart(chart, sensor);
        chart.invalidate();

        return view;
    }

    private void update() {
        data.clear();
        prepareData();
        Comparator comparator;

        if (streamsReordered) {
            comparator = positionComparator;
        } else {
            preparePositions();
            comparator = titleComparator;
        }

        sort(data, comparator);

        notifyDataSetChanged();
    }

    private void prepareData() {
        List<Sensor> sensors = sensorManager.getSensors();

        for (Sensor sensor : sensors) {
            Map<String, Object> map = prepareItem(sensor);

            map.put(QUANTITY, sensor.getMeasurementType() + " - " + sensor.getSymbol());
            map.put(SENSOR_NAME, sensor.getSensorName());
            map.put(SENSOR, sensor);

            data.add(map);
        }
    }

    private void preparePositions() {
        int currentPosition = 0;
        for (Map<String, Object> map : data) {
            Sensor sensor = (Sensor) map.get(SENSOR);
            positions.put(sensor.toString(), Integer.valueOf(currentPosition));
            currentPosition++;
        }
    }

    private Map<String, Object> prepareItem(Sensor sensor) {
        String name = sensor.getSensorName();
        if (!sensors.containsKey(name)) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            sensors.put(name, map);
        }
        return sensors.get(name);
    }

    private int getPosition(Map<String, Object> stream, Map<String, Integer> positions) {
        Sensor sensor = (Sensor) stream.get(SENSOR);
        Integer position = positions.get(sensor.toString());
        if (position == null) {
            return 0;
        }
        return position.intValue();
    }

    private int getPosition(Map<String, Object> stream) {
        return getPosition(stream, positions);
    }

    private void deleteStream(DashboardBaseActivity context, Sensor sensor) {
        if (sessionManager.getSession().getActiveMeasurementStreams().size() > 1) {
            confirmDeletingStream(context, sensor);
        } else {
            confirmDeletingSession(context);
        }
    }

    private void confirmDeletingSession(final DashboardBaseActivity context) {
        AlertDialog.Builder b = new AlertDialog.Builder(context);
        b.setMessage("This is the only stream, delete session?").
                setCancelable(true).
                setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sessionManager.deleteSession();
                        Intents.triggerSync(context);
                        Intents.sessions(context, context);
                    }
                }).setNegativeButton("No", NoOp.dialogOnClick());
        AlertDialog dialog = b.create();
        dialog.show();
    }

    private void confirmDeletingStream(final DashboardBaseActivity context, final Sensor sensor) {
        AlertDialog.Builder b = new AlertDialog.Builder(context);
        b.setMessage("Delete stream?").
                setCancelable(true).
                setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sensorManager.deleteSensorFromCurrentSession(sensor);
                        update();
                        Intents.triggerSync(context);
                    }
                }).setNegativeButton("No", NoOp.dialogOnClick());
        AlertDialog dialog = b.create();
        dialog.show();
    }
}
