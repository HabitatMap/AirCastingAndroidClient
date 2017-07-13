package pl.llp.aircasting.activity.adapter;

import com.github.mikephil.charting.charts.LineChart;
import com.google.common.collect.ComparisonChain;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.DashboardBaseActivity;
import pl.llp.aircasting.model.DashboardChartManager;
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

public class StreamAdapter extends SimpleAdapter implements View.OnClickListener {
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
            String rightTitle = right.get(QUANTITY).toString();
            String leftTitle = left.get(QUANTITY).toString();
            Sensor leftSensor = (Sensor) left.get(SENSOR);
            Sensor rightSensor = (Sensor) right.get(SENSOR);
            return ComparisonChain.start()
                    .compare(leftSensor.isEnabled() ? 0 : 1, rightSensor.isEnabled() ? 0 : 1)
                    .compare(getPosition(left), getPosition(right))
                    .compare(leftTitle, rightTitle).result();
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
    private static boolean firstStatsVisible = false;
    private static Map<String, Boolean> statsVisibility = newHashMap();
    private static int lastStreamsNumber = -1;

    private Map<String, Integer> oldPositions = newHashMap();
    private int invisiblePosition = -1;

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

    @Subscribe
    public void onEvent(SensorEvent event) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                update();
            }
        });
    }

//    public void startReorder() {
//        for (Map<String, Object> stream : data) {
//            Sensor s = (Sensor) stream.get(SENSOR);
//            oldPositions.put(s.toString(), getPosition(stream));
//        }
//    }
//
//    public void cancelReorder() {
//        for (Map<String, Object> stream : data) {
//            Sensor s = (Sensor) stream.get(SENSOR);
//            positions.put(s.toString(), getPosition(stream, oldPositions));
//        }
//        update();
//    }
//
//    public void commitReorder() {
//        oldPositions = newHashMap();
//    }
//
//    public void swapPositions(int pos1, int pos2) {
//        Sensor s1 = (Sensor) data.get(pos1).get(SENSOR);
//        Sensor s2 = (Sensor) data.get(pos2).get(SENSOR);
//        positions.put(s1.toString(), pos2);
//        positions.put(s2.toString(), pos1);
//        update();
//    }

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

//    public void toggleStatsVisibility(Sensor sensor) {
//        Boolean currentVisibility = statsVisibility.get(sensor.toString());
//        if (currentVisibility == null) {
//            currentVisibility = false;
//        statsVisibility.put(sensor.toString(), !currentVisibility && !firstStatsVisible);
//        firstStatsVisible = false;
//        update();
//    }
//
//    public void setInvisiblePosition(int position) {
//        invisiblePosition = position;
//        update();
//    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        Map<String, Object> state = data.get(position);
        Sensor sensor = (Sensor) state.get(SENSOR);
        chart = (LineChart) view.findViewById(R.id.chart);

        streamViewHelper.updateMeasurements(sensor, view, position);
        dashboardChartManager.drawChart(chart, sensor);
        chart.invalidate();

        return view;
    }

    private void update() {
        data.clear();

        List<Sensor> sensors = sensorManager.getSensors();

        for (Sensor sensor : sensors) {
            Map<String, Object> map = prepareItem(sensor);

            map.put(QUANTITY, sensor.getMeasurementType() + " - " + sensor.getSymbol());
            map.put(SENSOR_NAME, sensor.getSensorName());
            map.put(SENSOR, sensor);

            data.add(map);
        }

        sort(data, titleComparator);

        int currentPosition = 0;
        for (Map<String, Object> map : data) {
            Sensor sensor = (Sensor) map.get(SENSOR);
            positions.put(sensor.toString(), Integer.valueOf(currentPosition));
            currentPosition++;
        }

        if (data.size() == 1 && lastStreamsNumber != 1) {
            firstStatsVisible = true;
        } else if (data.size() > 1) {
            firstStatsVisible = false;
        }
        lastStreamsNumber = data.size();

        notifyDataSetChanged();
    }

    private Map<String, Object> prepareItem(Sensor sensor) {
        String name = sensor.getSensorName();
        if (!sensors.containsKey(name)) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            sensors.put(name, map);
        }
        return sensors.get(name);
    }

    @Override
    public void onClick(View view) {
        Sensor sensor = (Sensor) view.getTag();
        switch (view.getId()) {
            case R.id.delete_stream:
                deleteStream(context, sensor);
                break;
        }

        notifyDataSetChanged();
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
