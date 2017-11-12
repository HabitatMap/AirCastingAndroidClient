package pl.llp.aircasting.activity.adapter;

import android.widget.TextView;
import com.github.mikephil.charting.charts.LineChart;
import com.google.common.collect.ComparisonChain;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.DashboardBaseActivity;
import pl.llp.aircasting.activity.events.SessionLoadedEvent;
import pl.llp.aircasting.helper.*;
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

    CurrentSessionSensorManager currentSessionSensorManager;
    ViewingSessionsSensorManager viewingSessionsSensorManager;
    StreamViewHelper streamViewHelper;
    DashboardChartManager dashboardChartManager;

    DashboardBaseActivity context;
    EventBus eventBus;
    SessionState sessionState;
    SessionDataFactory sessionData;

    private List<Map<String, Object>> data;
    private Map<String, Map<String, Object>> sensors = newHashMap();
    private LineChart chart;
    public int streamDeleteMessage;

    // these are static to retain after activity recreation
    private static Map<String, Integer> positions = newHashMap();
    private static Map<Long, Integer> sessionPositions = newHashMap();
    private static TreeMap<Integer, Long> sortedSessionPositions = new TreeMap<Integer, Long>();
    private static Map<Long, Integer> sessionStreamCount = newHashMap();
    private static Map<Long, List<String>> clearedStreams = new HashMap<Long, List<String>>();
    private static boolean streamsReordered;
    private static boolean reorderInProgress = false;
    private static Comparator comparator;

    public StreamAdapter(DashboardBaseActivity context,
                         List<Map<String, Object>> data,
                         EventBus eventBus,
                         StreamViewHelper streamViewHelper,
                         CurrentSessionSensorManager currentSessionSensorManager,
                         ViewingSessionsSensorManager viewingSessionsSensorManager,
                         DashboardChartManager dashboardChartManager,
                         SessionState sessionState,
                         SessionDataFactory sessionData) {
        super(context, data, R.layout.stream_row, FROM, TO);
        this.data = data;
        this.eventBus = eventBus;
        this.context = context;
        this.currentSessionSensorManager = currentSessionSensorManager;
        this.viewingSessionsSensorManager = viewingSessionsSensorManager;
        this.streamViewHelper = streamViewHelper;
        this.dashboardChartManager = dashboardChartManager;
        this.sessionState = sessionState;
        this.sessionData = sessionData;
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
                    update(true);
                }
            }
        });
    }

   @Subscribe
    public void onEvent(SessionLoadedEvent event) {
        long sessionId = event.getSession().getId();

        clearedStreams.remove(sessionId);
        updateSessionPosition(sessionId);

        update(false);
    }

    public void forceUpdate() {
        update(false);
    }

    public void swapPositions(int pos1, int pos2) {
        Sensor s1 = (Sensor) data.get(pos1).get(SENSOR);
        Sensor s2 = (Sensor) data.get(pos2).get(SENSOR);

        positions.put(s1.toString(), pos2);
        positions.put(s2.toString(), pos1);

        resetSwappedCharts((Long) data.get(pos1).get(SESSION_ID), s1.getSensorName(), s2.getSensorName());

        streamsReordered = true;
        update(false);
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

    private void update(boolean onlyCurrentStreams) {
        if (!onlyCurrentStreams) {
            data.clear();
        }

        prepareData(onlyCurrentStreams);

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
        List positionsWithTitle = new ArrayList();
        int streamWithTitlePosition = 0;

        if (data.isEmpty()) {
            return;
        }

        for (Map.Entry<Integer, Long> entry : sortedSessionPositions.entrySet()) {
            long sessionId = entry.getValue();

            positionsWithTitle.add(streamWithTitlePosition);

            streamWithTitlePosition += sessionStreamCount.get(sessionId);
        }

        streamViewHelper.setPositionsWithTitle(positionsWithTitle);
    }

    private void prepareData(boolean onlyCurrentStreams) {
        Map<Long, Map<SensorName, Sensor>> allSensors = newHashMap();
        Map<SensorName, Sensor> currentSensors = currentSessionSensorManager.getSensorsMap();

        if (onlyCurrentStreams == false) {
            allSensors = viewingSessionsSensorManager.getAllViewingSensors();
        }

        allSensors.put(Constants.CURRENT_SESSION_FAKE_ID, currentSensors);

        if (allSensors.isEmpty()) {
            return;
        }

        for (Map.Entry<Long, Map<SensorName, Sensor>> entry : allSensors.entrySet()) {
            int hiddenStreamsSize = 0;
            Long sessionId = entry.getKey();
            Map<SensorName, Sensor> sensors = entry.getValue();
            List clearedStreamsForSession = clearedStreams.get(sessionId);
            if (clearedStreamsForSession != null) {
                hiddenStreamsSize = clearedStreamsForSession.size();
            }

            sessionStreamCount.put(sessionId, sensors.size() - hiddenStreamsSize);

            for (Sensor sensor : sensors.values()) {
                if (sensorIsHidden(sensor, clearedStreamsForSession)) {
                    continue;
                }

                HashMap<String, Object> map = new HashMap<String, Object>();

                map.put(SESSION_ID, sessionId);
                map.put(QUANTITY, sensor.getMeasurementType() + " - " + sensor.getSymbol());
                map.put(SENSOR_NAME, sensor.getSensorName());
                map.put(SENSOR, sensor);

                data.remove(map);
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

    private int getPosition(Map<String, Object> stream) {
        Sensor sensor = (Sensor) stream.get(SENSOR);
        Integer position = positions.get(sensor.toString());
        if (position == null) {
            return 0;
        }
        return position.intValue();
    }

    private void updateSessionPosition(long sessionId) {
        if (!sessionPositions.containsKey(sessionId)) {
            if (sessionState.isSessionCurrent(sessionId)) {
                insertCurrentSessionPosition();
            } else {
                sessionPositions.put(sessionId, sessionPositions.size());
                sortedSessionPositions.put(sessionPositions.size() - 1, sessionId);
            }
        }
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

    public void clearStream(int position, long sessionId) {
        Sensor sensor = getSensorFromData(position);
        String sensorName = sensor.toString();

        if (!clearedStreams.containsKey(sessionId)) {
            clearedStreams.put(sessionId, new ArrayList<String>());
        }
        clearedStreams.get(sessionId).add(sensorName);
        streamsReordered = true;
        update(false);
    }

    public void deleteStream(int position, long sessionId) {
        Sensor sensor = getSensorFromData(position);

        if (sessionData.getSession(sessionId).getActiveMeasurementStreams().size() > 1) {
            confirmDeletingStream(sensor, sessionId);
        } else {
            confirmDeletingSession(sessionId);
        }
    }

    public boolean canStreamBeClearedOrDeleted(long sessionId) {
        if (sessionState.isSessionBeingViewed(sessionId)) {
            return true;
        } else if (sessionState.isCurrentSessionIdle()) {
            streamDeleteMessage = R.string.cannot_delete_stream;
            return false;
        } else {
            if (sessionData.getSession(sessionId).isFixed()) {
                return true;
            } else {
                streamDeleteMessage = R.string.wrong_session_type;
                return false;
            }
        }
    }

    public int getStreamDeleteMessage() {
        return streamDeleteMessage;
    }

    private Sensor getSensorFromData(int position) {
        return (Sensor) data.get(position).get(SENSOR);
    }

    private void confirmDeletingSession(final long sessionId) {
        AlertDialog.Builder b = new AlertDialog.Builder(context);
        b.setMessage("This is the only stream, delete session?").
                setCancelable(true).
                setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sessionData.deleteSession(sessionId);
                        Intents.triggerSync(context);
                        Intents.sessions(context, context);
                    }
                }).setNegativeButton("No", NoOp.dialogOnClick());
        AlertDialog dialog = b.create();
        dialog.show();
    }

    private void confirmDeletingStream(final Sensor sensor, final long sessionId) {
        AlertDialog.Builder b = new AlertDialog.Builder(context);
        b.setMessage("Delete stream?").
                setCancelable(true).
                setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        currentSessionSensorManager.deleteSensorFromCurrentSession(sensor);
                        update(false);
                        Intents.triggerSync(context);
                    }
                }).setNegativeButton("No", NoOp.dialogOnClick());
        AlertDialog dialog = b.create();
        dialog.show();
    }
}
