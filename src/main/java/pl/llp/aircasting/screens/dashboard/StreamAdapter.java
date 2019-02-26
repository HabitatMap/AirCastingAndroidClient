package pl.llp.aircasting.screens.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import com.google.common.collect.ComparisonChain;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.event.measurements.FixedSessionsMeasurementEvent;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.screens.common.ApplicationState;
import pl.llp.aircasting.screens.common.helpers.ResourceHelper;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionSensorManager;
import pl.llp.aircasting.screens.common.helpers.NoOp;
import pl.llp.aircasting.screens.common.sessionState.SessionDataFactory;
import pl.llp.aircasting.screens.common.sessionState.SessionState;
import pl.llp.aircasting.screens.common.sessionState.ViewingSessionsSensorManager;
import pl.llp.aircasting.event.sensor.SessionSensorsLoadedEvent;
import pl.llp.aircasting.event.session.SessionStoppedEvent;
import pl.llp.aircasting.event.session.ToggleSessionReorderEvent;
import pl.llp.aircasting.model.*;

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
import java.util.List;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.sort;

public class StreamAdapter extends SimpleAdapter {
    public static final String QUANTITY = "quantity";
    public static final String SENSOR_NAME = "sensorName";
    public static final String SENSOR = "sensor";
    public static final String SESSION_ID = "session_id";
    public static final String POSITIONS = "positions";
    public static final String SESSION_STREAM_COUNT = "session_stream_count";
    public static final String CLEARED_STREAMS = "cleared_streams";
    public static final String STREAMS_REORDERED = "streams_reordered";
    private static final int INTERVAL = 60000;

    private static final String[] FROM = new String[]{
            QUANTITY, SENSOR_NAME
    };

    private static final int[] TO = new int[]{
            R.id.quantity, R.id.sensor_name
    };

    private final Comparator<Map<String, Object>> comparator = new Comparator<Map<String, Object>>() {
        @Override
        public int compare(@Nullable Map<String, Object> left, @Nullable Map<String, Object> right) {
            int result;
            long leftSessionId = (Long) left.get(SESSION_ID);
            long rightSessionId = (Long) right.get(SESSION_ID);

            Sensor leftSensor = (Sensor) left.get(SENSOR);
            Sensor rightSensor = (Sensor) right.get(SENSOR);

            ComparisonChain chain = ComparisonChain.start()
                    .compare(getSessionPosition(leftSessionId), getSessionPosition(rightSessionId));

            if (stateRestored || streamsReordered.get(leftSessionId) == true) {
                result = chain.compare(getPosition(left), getPosition(right)).result();
            } else {
                result = chain.compare(leftSensor.getSensorName(), rightSensor.getSensorName()).result();
            }

            return result;
        }
    };

    CurrentSessionSensorManager currentSessionSensorManager;
    ViewingSessionsSensorManager viewingSessionsSensorManager;
    StreamViewHelper streamViewHelper;
    DashboardChartManager dashboardChartManager;
    ResourceHelper resourceHelper;

    DashboardBaseActivity context;
    EventBus eventBus;
    SessionState sessionState;
    SessionDataFactory sessionData;
    ApplicationState state;

    private List<Map<String, Object>> data;
    public int streamDeleteMessage;

    private static HashMap<String, Integer> positions = newHashMap();
    private static HashMap<Long, Integer> sessionPositions = newHashMap();
    private static TreeMap<Integer, Long> sortedSessionPositions = new TreeMap<Integer, Long>();
    private static HashMap<Long, Integer> sessionStreamCount = newHashMap();
    private static HashMap<Long, List<String>> clearedStreams = new HashMap<Long, List<String>>();
    private static HashMap<Long, Boolean> streamsReordered = new HashMap<Long, Boolean>();
    private static HashMap<String, Boolean> currentSessionSensors = newHashMap();
    private static boolean reorderInProgress = false;
    private static boolean updateAllowed = false;
    private static Handler handler = new Handler();
    private static boolean shouldRunFakeActivity = false;
    private static boolean stateRestored = false;

    public StreamAdapter(DashboardBaseActivity context,
                         List<Map<String, Object>> data,
                         EventBus eventBus,
                         StreamViewHelper streamViewHelper,
                         ResourceHelper resourceHelper,
                         CurrentSessionSensorManager currentSessionSensorManager,
                         ViewingSessionsSensorManager viewingSessionsSensorManager,
                         DashboardChartManager dashboardChartManager,
                         SessionState sessionState,
                         SessionDataFactory sessionData,
                         ApplicationState state) {
        super(context, data, R.layout.stream_row, FROM, TO);
        this.data = data;
        this.eventBus = eventBus;
        this.context = context;
        this.currentSessionSensorManager = currentSessionSensorManager;
        this.viewingSessionsSensorManager = viewingSessionsSensorManager;
        this.streamViewHelper = streamViewHelper;
        this.resourceHelper = resourceHelper;
        this.dashboardChartManager = dashboardChartManager;
        this.sessionState = sessionState;
        this.sessionData = sessionData;
        this.state = state;
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

    public void saveAdapterState(Bundle outState) {
        outState.putSerializable(POSITIONS, positions);
        outState.putSerializable(SESSION_STREAM_COUNT, sessionStreamCount);
        outState.putSerializable(CLEARED_STREAMS, clearedStreams);
        outState.putSerializable(STREAMS_REORDERED, streamsReordered);
    }

    public void restoreAdapterState(Bundle state) {
        stateRestored = true;
        positions = (HashMap<String, Integer>) state.getSerializable(POSITIONS);
        sessionStreamCount = (HashMap<Long, Integer>) state.getSerializable(SESSION_STREAM_COUNT);
        clearedStreams = (HashMap<Long, List<String>>) state.getSerializable(CLEARED_STREAMS);
        streamsReordered = (HashMap<Long, Boolean>) state.getSerializable(STREAMS_REORDERED);

        prepareSessionPositions();
        update(false);
    }

    public void resetAllStaticCharts() {
        dashboardChartManager.resetAllStaticCharts();
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
    public void onEvent(final FixedSessionsMeasurementEvent event) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!reorderInProgress) {
                    update(false);
                }
            }
        });
    }

    @Subscribe
    public void onEvent(final SensorEvent event) {
        updateSessionPosition(Constants.CURRENT_SESSION_FAKE_ID);
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!reorderInProgress && updateAllowed) {
                    update(true);
                }
            }
        });
    }

    @Subscribe
    public void onEvent(SessionStoppedEvent event) {
        currentSessionSensors.clear();
    }

    @Subscribe
    public void onEvent(SessionSensorsLoadedEvent event) {
        long sessionId = event.getSessionId();
        int sensorsCount = sessionData.getSessionSensorsCount(sessionId);

        clearedStreams.remove(sessionId);
        updateSessionPosition(sessionId);

        if (sensorsCount > 0) {
            sessionStreamCount.put(sessionId, sessionData.getSessionSensorsCount(sessionId));
        } else {
            sessionStreamCount.remove(sessionId);
        }

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                context.startActivity(new Intent(context, FakeActivity.class));
                update(false);
            }
        });

        setStartFakeActivity();
    }

    public void setStartFakeActivity() {
        shouldRunFakeActivity = true;
    }

    public void stopFakeActivityCallback() {
        shouldRunFakeActivity = false;
    }

    private Runnable startFakeActivity = new Runnable() {
        @Override
        public void run() {
            // pretty ugly way to make the GC run in update() successful
            if (!reorderInProgress && !data.isEmpty() && shouldRunFakeActivity) {
                context.startActivity(new Intent(context, FakeActivity.class));
                update(true);
            }
            handler.removeCallbacksAndMessages(null);
            handler.postDelayed(startFakeActivity, INTERVAL);
        }
    };

    @Subscribe
    public void onEvent(ToggleSessionReorderEvent event) {
        // this is a hacky way to make the ListFragment call onResume,
        // so that the OnItemClick and OnItemTouch listeners get reset properly.

        context.startActivity(new Intent(context, FakeActivity.class));
        if (event.areSessionsCleared()) {
            sortedSessionPositions.clear();
            sessionPositions.clear();
        }
        update(false);
    }

    public void forceUpdate() {
        update(false);
    }

    public void swapPositions(int pos1, int pos2) {
        Map item1 = data.get(pos1);
        Map item2 = data.get(pos2);
        Sensor s1 = (Sensor) item1.get(SENSOR);
        Sensor s2 = (Sensor) item2.get(SENSOR);
        Long sessionId = (Long) item1.get(SESSION_ID);
        String positionKey1 = s1.toString();
        String positionKey2 = s2.toString();

        positions.put(positionKey1, pos2);
        positions.put(positionKey2, pos1);

        resetSwappedCharts(sessionId, s1.getSensorName(), s2.getSensorName());

        streamsReordered.put(sessionId, true);
        update(false);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        Map<String, Object> item = (Map<String, Object>) getItem(position);
        final Sensor sensor = (Sensor) item.get(SENSOR);
        final long sessionId = (Long) item.get(SESSION_ID);
        view.setTag(R.id.session_id_tag, sessionId);

        final Button moveSessionDown = (Button) view.findViewById(R.id.session_down);
        final Button moveSessionUp = (Button) view.findViewById(R.id.session_up);

        if (state.dashboardState().isSessionReorderInProgress()) {
            moveSessionDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    moveSessionDown(sessionId);
                }
            });

            moveSessionUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    moveSessionUp(sessionId);
                }
            });

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DashboardActivity activity = (DashboardActivity) context;
                    activity.viewChartOptions(v);
                }
            });
        }

        dashboardChartManager.drawChart(view, sensor, sessionId);
        streamViewHelper.updateMeasurements(sessionId, sensor, view, position);

        context.invalidateOptionsMenu();

        return view;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    private void moveSessionDown(long sessionId) {
        int sessionPosition = sessionPositions.get(sessionId);
        int switchSessionPosition = sessionPosition + 1;

        if (sessionPosition < sessionPositions.size() - 1) {
            switchSessionPositions(sessionPosition, switchSessionPosition);
        }
    }

    private void moveSessionUp(long sessionId) {
        int sessionPosition = sessionPositions.get(sessionId);
        int switchSessionPosition = sessionPosition - 1;

        if (sessionPosition != 0) {
            switchSessionPositions(sessionPosition, switchSessionPosition);
        }
    }

    private void switchSessionPositions(int pos1, int pos2) {
        long session1Id = sortedSessionPositions.get(pos1);
        long session2Id = sortedSessionPositions.get(pos2);

        sessionPositions.put(session1Id, pos2);
        sessionPositions.put(session2Id, pos1);
        sortedSessionPositions.put(pos2, session1Id);
        sortedSessionPositions.put(pos1, session2Id);

//        resetAllStaticCharts();
        update(false);
    }

    private void update(boolean onlyCurrentStreams) {
        prepareData();

        sort(data, comparator);
        preparePositions();

        setSessionTitles();

        System.gc();
        System.gc();
        System.gc();

        if (data.size() == positions.size()) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        }
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

    private void prepareData() {
        updateAllowed = false;

        data.clear();

        Map<Long, Map<SensorName, Sensor>> allSensors;
        Map<SensorName, Sensor> currentSensors = currentSessionSensorManager.getSensorsMap();

        allSensors = viewingSessionsSensorManager.getAllViewingSensors();
        allSensors.put(Constants.CURRENT_SESSION_FAKE_ID, currentSensors);

        if (allSensors.isEmpty()) {
            return;
        }

        for (Map.Entry<Long, Map<SensorName, Sensor>> entry : allSensors.entrySet()) {
            int hiddenStreamsSize = 0;
            final Long sessionId = entry.getKey();
            Map<SensorName, Sensor> sensors = entry.getValue();
            List clearedStreamsForSession = clearedStreams.get(sessionId);
            if (clearedStreamsForSession != null) {
                hiddenStreamsSize = clearedStreamsForSession.size();
            }

            sessionStreamCount.put(sessionId, sensors.size() - hiddenStreamsSize);

            for (final Sensor sensor : sensors.values()) {
                if (sensorIsHidden(sensor, clearedStreamsForSession)) {
                    continue;
                }

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        HashMap<String, Object> map = new HashMap<String, Object>();

                        map.put(SESSION_ID, sessionId);
                        map.put(SENSOR_NAME, sensor.getSensorName());
                        map.put(SENSOR, sensor);

                        data.add(map);
                    }
                });
            }

            if (streamsReordered.get(sessionId) == null) {
                streamsReordered.put(sessionId, false);
            }
        }
        updateAllowed = true;
    }

    private boolean sensorIsHidden(Sensor sensor, @Nullable List<String> clearedStreamsForSession) {
        return clearedStreamsForSession != null && clearedStreamsForSession.contains(sensor.toString());
    }

    private void preparePositions() {
        positions.clear();
        int currentPosition = 0;
        for (Map<String, Object> map : data) {
            Sensor sensor = (Sensor) map.get(SENSOR);
            String positionKey = sensor.toString();
            positions.put(positionKey, Integer.valueOf(currentPosition));
            currentPosition++;
        }
    }

    private int getPosition(Map<String, Object> stream) {
        Sensor sensor = (Sensor) stream.get(SENSOR);
        String positionKey = sensor.toString();
        Integer position = positions.get(positionKey);
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

    private void prepareSessionPositions() {
        for (Map.Entry<Long, Boolean> entry : streamsReordered.entrySet()) {
            updateSessionPosition(entry.getKey());
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

    public void clearStreamFromPosition(int position, long sessionId) {
        Sensor sensor = getSensorFromData(position);
        clearStream(sensor, sessionId);
    }

    private void clearStream(Sensor sensor, long sessionId) {
        if (!clearedStreams.containsKey(sessionId)) {
            clearedStreams.put(sessionId, new ArrayList<String>());
        }
        clearedStreams.get(sessionId).add(sensor.toString());
        List clearedStreamsForSession = clearedStreams.get(sessionId);

        clearViewingSessionIfNeeded(sessionId, clearedStreamsForSession.size());
        streamsReordered.put(sessionId, true);
        update(false);
    }

    private void clearViewingSessionIfNeeded(long sessionId, int clearedStreamsSize) {
        if (!sessionState.isSessionCurrent(sessionId) &&
                sessionData.getSession(sessionId).getStreamsSize() <= clearedStreamsSize) {
            sessionData.clearViewingSession(sessionId);
            sortedSessionPositions.remove(getSessionPosition(sessionId));
            context.invalidateOptionsMenu();
        }
    }

    public void deleteStream(int position, long sessionId) {
        Sensor sensor = getSensorFromData(position);

        if (sessionData.getSession(sessionId).getStreamsSize() > 1) {
            confirmDeletingStream(sensor, sessionId);
        } else {
            confirmDeletingSession(sessionId);
        }
    }

    public boolean canStreamBeClearedOrDeleted(long sessionId) {
        if (!sessionState.isSessionCurrent(sessionId)) {
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
                        cleanupSession(sessionId);
                        Intents.triggerSync(context);
                        update(false);
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
                        sessionData.deleteSensorStream(sensor, sessionId);
                        Intents.triggerSync(context);
                        update(false);
                    }
                }).setNegativeButton("No", NoOp.dialogOnClick());
        AlertDialog dialog = b.create();
        dialog.show();
    }

    private void cleanupSession(long sessionId) {
        int sessionPosition = sessionPositions.get(sessionId);

        sortedSessionPositions.remove(sessionPosition);
        sessionPositions.remove(sessionId);
        sessionStreamCount.remove(sessionId);
        clearedStreams.remove(sessionId);
    }
}
