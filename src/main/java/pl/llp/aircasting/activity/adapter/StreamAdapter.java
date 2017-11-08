package pl.llp.aircasting.activity.adapter;

import android.util.Log;
import android.widget.TextView;
import com.github.mikephil.charting.charts.LineChart;
import com.google.common.collect.ComparisonChain;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.DashboardBaseActivity;
import pl.llp.aircasting.activity.events.SessionLoadedEvent;
import pl.llp.aircasting.helper.DashboardChartManager;
import pl.llp.aircasting.helper.NoOp;
import pl.llp.aircasting.helper.StreamViewHelper;
import pl.llp.aircasting.model.CurrentSessionSensorManager;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.CurrentSessionManager;
import pl.llp.aircasting.model.ViewingSessionsSensorManager;
import pl.llp.aircasting.model.events.SensorEvent;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.jetbrains.annotations.Nullable;
import pl.llp.aircasting.model.internal.SensorName;
import pl.llp.aircasting.util.Constants;

import java.util.*;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.sort;

public class StreamAdapter extends SimpleAdapter {
    public static final String QUANTITY = "quantity";
    public static final String SENSOR_NAME = "sensorName";
    public static final String SENSOR = "sensor";
    public static final String SESSION_ID = "session_id";

    private static final String[] FROM = new String[]{
            QUANTITY, SENSOR_NAME
    };

    private static final int[] TO = new int[]{
            R.id.quantity, R.id.sensor_name
    };

    private final Comparator<Map<String, Object>> initialComparator = new Comparator<Map<String, Object>>() {
        @Override
        public int compare(@Nullable Map<String, Object> left, @Nullable Map<String, Object> right) {
            long leftSessionId = (Long) left.get(SESSION_ID);
            long rightSessionId = (Long) right.get(SESSION_ID);

            Sensor leftSensor = (Sensor) left.get(SENSOR);
            Sensor rightSensor = (Sensor) right.get(SENSOR);

            return ComparisonChain.start()
                    .compare(getSessionPosition(leftSessionId), getSessionPosition(rightSessionId))
                    .compare(leftSensor.getSensorName(), rightSensor.getSensorName()).result();
        }
    };

    private final Comparator<Map<String, Object>> positionComparator = new Comparator<Map<String, Object>>() {
        @Override
        public int compare(@Nullable Map<String, Object> left, @Nullable Map<String, Object> right) {
            long leftSessionId = (Long) left.get(SESSION_ID);
            long rightSessionId = (Long) right.get(SESSION_ID);

            return ComparisonChain.start()
                    .compare(getSessionPosition(leftSessionId), getSessionPosition(rightSessionId))
                    .compare(getPosition(left), getPosition(right)).result();
        }
    };

    CurrentSessionManager currentSessionManager;
    CurrentSessionSensorManager currentSessionSensorManager;
    ViewingSessionsSensorManager viewingSessionsSensorManager;
    StreamViewHelper streamViewHelper;
    DashboardChartManager dashboardChartManager;

    DashboardBaseActivity context;
    EventBus eventBus;

    private List<Map<String, Object>> data;
    private Map<String, Map<String, Object>> sensors = newHashMap();
    private LineChart chart;
    public int streamDeleteMessage;

    // these are static to retain after activity recreation
    private static Map<String, Integer> positions = newHashMap();
    private static Map<Long, Integer> sessionPositions = newHashMap();
    private static TreeMap<Integer, Long> sortedSessionPositions = new TreeMap<Integer, Long>();
    private static Map<Long, Integer> sessionStreamCount = newHashMap();
    private static boolean streamsReordered;
    private static boolean reorderInProgress = false;
    private static Map<Long, List<String>> clearedStreams = new HashMap<Long, List<String>>();
    private static Comparator comparator;

    public StreamAdapter(DashboardBaseActivity context,
                         List<Map<String, Object>> data,
                         EventBus eventBus,
                         StreamViewHelper streamViewHelper,
                         CurrentSessionSensorManager currentSessionSensorManager,
                         ViewingSessionsSensorManager viewingSessionsSensorManager,
                         CurrentSessionManager currentSessionManager,
                         DashboardChartManager dashboardChartManager) {
        super(context, data, R.layout.stream_row, FROM, TO);
        this.data = data;
        this.eventBus = eventBus;
        this.context = context;
        this.currentSessionSensorManager = currentSessionSensorManager;
        this.viewingSessionsSensorManager = viewingSessionsSensorManager;
        this.currentSessionManager = currentSessionManager;
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

    private void resetSwappedCharts(Long sessionId, String sensor1, String sensor2) {
        String[] sensorNames = new String[2];
        sensorNames[0] = sensor1;
        sensorNames[1] = sensor2;

        dashboardChartManager.resetSpecificStaticCharts(sessionId, sensorNames);
    }

    public void startReorder() {
        reorderInProgress = true;
    }

    public void stopReorder() {
        reorderInProgress = false;
    }

    @Subscribe
    public void onEvent(SensorEvent event) {
        updateSessionPosition(Constants.CURRENT_SESSION_FAKE_ID);

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!reorderInProgress) {
                    update();
                }
            }
        });
    }

   @Subscribe
    public void onEvent(SessionLoadedEvent event) {
        long sessionId = event.getSession().getId();

        clearedStreams.remove(sessionId);
        updateSessionPosition(sessionId);
    }

    public void forceUpdate() {
        update();
    }

    public void swapPositions(int pos1, int pos2) {
        Sensor s1 = (Sensor) data.get(pos1).get(SENSOR);
        Sensor s2 = (Sensor) data.get(pos2).get(SENSOR);
        positions.put(s1.toString(), pos2);
        positions.put(s2.toString(), pos1);

        resetSwappedCharts((Long) data.get(pos1).get(SESSION_ID), s1.getSensorName(), s2.getSensorName());

        streamsReordered = true;
        update();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        Map<String, Object> state = data.get(position);
        final Sensor sensor = (Sensor) state.get(SENSOR);
        final long sessionId = (Long) state.get(SESSION_ID);
        chart = (LineChart) view.findViewById(R.id.chart);

        view.setTag(R.id.session_id_tag, sessionId);
        streamViewHelper.updateMeasurements(sessionId, sensor, view, position);
        dashboardChartManager.drawChart(chart, sensor, sessionId);
        chart.invalidate();

        return view;
    }

    private void update() {
        data.clear();
        prepareData();

        if (streamsReordered) {
            comparator = positionComparator;
        } else {
            preparePositions();
            comparator = initialComparator;
        }

        sort(data, comparator);
        setSessionTitles();

        notifyDataSetChanged();
    }

    private void setSessionTitles() {
        if (data.isEmpty()) {
            return;
        }

        int streamWithTitlePosition = 0;

        for (Map.Entry<Integer, Long> entry : sortedSessionPositions.entrySet()) {
            long sessionId = entry.getValue();

            streamViewHelper.addPositionWithTitle(streamWithTitlePosition);

            streamWithTitlePosition += sessionStreamCount.get(sessionId);
        }
    }

    private void prepareData() {
        Map<Long, Map<SensorName, Sensor>> allSensors = viewingSessionsSensorManager.getAllViewingSensors();
//        List<String> clearedStreamsForSession = new ArrayList<String>();
        Map<SensorName, Sensor> currentSensors = currentSessionSensorManager.getSensorsMap();

        allSensors.put(Constants.CURRENT_SESSION_FAKE_ID, currentSensors);

        if (allSensors.isEmpty()) {
            return;
        }

//        if (currentSessionManager.sessionHasId()) {
//            sessionId = currentSessionManager.getCurrentSession().getId();
//            clearedStreamsForSession = clearedStreams.get(sessionId);
//        }

        for (Map.Entry<Long, Map<SensorName, Sensor>> entry : allSensors.entrySet()) {
            Long sessionId = entry.getKey();
            Map<SensorName, Sensor> sensors = entry.getValue();

            sessionStreamCount.put(sessionId, sensors.size());

            for (Sensor sensor : sensors.values()) {
//                if (sensorIsHidden(sensor, clearedStreamsForSession)) {
//                    continue;
//                }

//                Map<String, Object> map = prepareItem(sensor);
                HashMap<String, Object> map = new HashMap<String, Object>();

                map.put(SESSION_ID, sessionId);
                map.put(QUANTITY, sensor.getMeasurementType() + " - " + sensor.getSymbol());
                map.put(SENSOR_NAME, sensor.getSensorName());
                map.put(SENSOR, sensor);

                data.add(map);
            }
        }
    }

    private boolean sensorIsHidden(Sensor sensor, @Nullable List<String> clearedStreamsForSession) {
        return clearedStreamsForSession != null && clearedStreamsForSession.contains(sensor.toString());
    }

    private void preparePositions() {
        int currentPosition = 0;
        for (Map<String, Object> map : data) {
            Sensor sensor = (Sensor) map.get(SENSOR);
            positions.put(sensor.toString(), Integer.valueOf(currentPosition));
            currentPosition++;
        }
    }

    private void updateSessionPosition(long sessionId) {
        if (!sessionPositions.containsKey(sessionId)) {
            if (sessionId == Constants.CURRENT_SESSION_FAKE_ID) {
                insertCurrentSessionPosition();
            } else {
                sessionPositions.put(sessionId, sessionPositions.size());
                sortedSessionPositions.put(sessionPositions.size(), sessionId);
            }
        }
    }

    private int getPosition(Map<String, Object> stream) {
        Sensor sensor = (Sensor) stream.get(SENSOR);
        Integer position = positions.get(sensor.toString());
        if (position == null) {
            return 0;
        }
        return position.intValue();
    }

    private int getSessionPosition(long sessionId) {
        Integer position = sessionPositions.get(sessionId);
        if (position == null) {
            return 0;
        }
        return position.intValue();
    }

    private void insertCurrentSessionPosition() {
        for (Map.Entry<Long, Integer> entry : sessionPositions.entrySet()) {
            sessionPositions.put(entry.getKey(), entry.getValue() + 1);
            sortedSessionPositions.put(entry.getValue() + 1, entry.getKey());
        }

        sessionPositions.put(Constants.CURRENT_SESSION_FAKE_ID, 0);
        sortedSessionPositions.put(0, Constants.CURRENT_SESSION_FAKE_ID);
    }

    public void clearStream(int position) {
        long sessionId;

        Sensor sensor = (Sensor) data.get(position).get(SENSOR);
        String sensorName = sensor.toString();
        sessionId = currentSessionManager.getCurrentSession().getId();

        if (!clearedStreams.containsKey(sessionId)) {
            clearedStreams.put(sessionId, new ArrayList<String>());
        }
        clearedStreams.get(sessionId).add(sensorName);
        streamsReordered = true;
        update();
    }

    public void deleteStream(View streamView) {
        TextView sensorTitle = (TextView) streamView.findViewById(R.id.sensor_name);
        String sensorName = (String) sensorTitle.getText();
        Sensor sensor = currentSessionSensorManager.getSensorByName(sensorName);

        if (currentSessionManager.getCurrentSession().getActiveMeasurementStreams().size() > 1) {
            confirmDeletingStream(sensor);
        } else {
            confirmDeletingSession();
        }
    }

    public boolean canStreamBeClearedOrDeleted() {
        if (currentSessionManager.isSessionRecording()) {
            if (currentSessionManager.getCurrentSession().isFixed()) {
                return true;
            } else {
                streamDeleteMessage = R.string.wrong_session_type;
                return false;
            }
        } else if (currentSessionManager.isSessionBeingViewed()) {
            return true;
        } else if (currentSessionManager.isSessionIdle()) {
            streamDeleteMessage = R.string.cannot_delete_stream;
            return false;
        }
        return false;
    }

    public int getStreamDeleteMessage() {
        return streamDeleteMessage;
    }

    private void confirmDeletingSession() {
        AlertDialog.Builder b = new AlertDialog.Builder(context);
        b.setMessage("This is the only stream, delete session?").
                setCancelable(true).
                setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentSessionManager.deleteSession();
                        Intents.triggerSync(context);
                        Intents.sessions(context, context);
                    }
                }).setNegativeButton("No", NoOp.dialogOnClick());
        AlertDialog dialog = b.create();
        dialog.show();
    }

    private void confirmDeletingStream(final Sensor sensor) {
        AlertDialog.Builder b = new AlertDialog.Builder(context);
        b.setMessage("Delete stream?").
                setCancelable(true).
                setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentSessionSensorManager.deleteSensorFromCurrentSession(sensor);
                        update();
                        Intents.triggerSync(context);
                    }
                }).setNegativeButton("No", NoOp.dialogOnClick());
        AlertDialog dialog = b.create();
        dialog.show();
    }
}
