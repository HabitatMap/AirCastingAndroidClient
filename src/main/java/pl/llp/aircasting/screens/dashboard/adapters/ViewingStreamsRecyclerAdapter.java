package pl.llp.aircasting.screens.dashboard.adapters;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.ComparisonChain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.llp.aircasting.R;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.screens.common.helpers.ResourceHelper;
import pl.llp.aircasting.screens.dashboard.helper.StreamItemTouchHelperAdapter;
import pl.llp.aircasting.screens.dashboard.views.StreamItemViewMvc;
import pl.llp.aircasting.screens.dashboard.views.ViewingStreamItemViewMvcImpl;

import static pl.llp.aircasting.screens.dashboard.adapters.CurrentStreamsRecyclerAdapter.PAYLOAD_CHARTS_REFRESHED;
import static pl.llp.aircasting.screens.dashboard.adapters.CurrentStreamsRecyclerAdapter.PAYLOAD_TITLE_POSITION_CHANGED;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.SENSOR;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.SESSION_ID;
import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.TITLE_DISPLAY;

public class ViewingStreamsRecyclerAdapter extends RecyclerView.Adapter<ViewingStreamsRecyclerAdapter.StreamViewHolder>
        implements StreamRecyclerAdapter, StreamItemViewMvc.Listener, StreamItemTouchHelperAdapter {
    public static final int STREAM_CHART_WIDTH = 800;
    public static final int STREAM_CHART_HEIGHT = 125;

    private static boolean mStreamsReordered = false;

    private final LayoutInflater mInflater;
    private final StreamItemViewMvc.Listener mListener;
    private final ResourceHelper mResourceHelper;

    private List<Map<String, Object>> mData = new ArrayList();
    private Map<String, Double> mNowData = new HashMap<>();
    private Map mChartData = new HashMap();
    private ArrayList<Long> mSessionPositions = new ArrayList();
    private ArrayList<String> mStreamPositions = new ArrayList<>();


    private final Comparator<Map<String, Object>> mStreamComparator = new Comparator<Map<String, Object>>() {
        @Override
        public int compare(@Nullable Map<String, Object> leftStreamMap, @Nullable Map<String, Object> rightStreamMap) {
            int result;
            long leftSessionId = (Long) leftStreamMap.get(SESSION_ID);
            long rightSessionId = (Long) rightStreamMap.get(SESSION_ID);

            Sensor leftSensor = (Sensor) leftStreamMap.get(SENSOR);
            Sensor rightSensor = (Sensor) rightStreamMap.get(SENSOR);

            ComparisonChain chain = ComparisonChain.start()
                    .compare(getSessionPosition(leftSessionId), getSessionPosition(rightSessionId));

            if (mStreamsReordered && positionsPrepared()) {
                result = chain.compare(getStreamPosition(leftSensor.toString()), getStreamPosition(rightSensor.toString())).result();
            } else {
                result = chain.compare(leftSensor.getSensorName(), rightSensor.getSensorName()).result();
            }

            return result;
        }
    };

    public class StreamViewHolder extends RecyclerView.ViewHolder {
        private final StreamItemViewMvc mViewMvc;

        public StreamViewHolder(StreamItemViewMvc viewMvc) {
            super(viewMvc.getRootView());
            mViewMvc = viewMvc;
        }
    }

    public ViewingStreamsRecyclerAdapter(LayoutInflater inflater, StreamItemViewMvc.Listener listener, ResourceHelper resourceHelper) {
        mInflater = inflater;
        mListener = listener;
        mResourceHelper = resourceHelper;
    }

    @Override
    public void bindData(List data) {
        mData.clear();
        mData = data;
        prepareSessionPositions();
        Collections.sort(mData, mStreamComparator);
        prepareStreamPositionsAndTitles();

        notifyItemRangeChanged(0, mData.size());
    }

    private void prepareSessionPositions() {
        for (Map<String, Object> dataItem : mData) {
            long sessionId = (long) dataItem.get(SESSION_ID);

            if (!mSessionPositions.contains(sessionId)) {
                mSessionPositions.add(mSessionPositions.size(), sessionId);
            }
        }
    }

    private void prepareStreamPositionsAndTitles() {
        long currentSessionId = -1;

        for (Map<String, Object> dataItem : mData) {
            Sensor sensor = (Sensor) dataItem.get(SENSOR);
            String sensorString = sensor.toString();
            long sessionId = (long) dataItem.get(SESSION_ID);

            if (!mStreamPositions.contains(sensorString)) {
                mStreamPositions.add(mStreamPositions.size(), sensorString);
            }

            dataItem.put(TITLE_DISPLAY, firstSessionStream(sessionId, currentSessionId));
            currentSessionId = sessionId;
        }
    }

    private boolean firstSessionStream(long sessionId, long currentSessionKey) {
        if (sessionId != currentSessionKey) {
            return true;
        } else return false;
    }

    private int getStreamPosition(String sensorString) {
        return mStreamPositions.indexOf(sensorString);
    }

    private int getSessionPosition(long sessionId) {
        return mSessionPositions.indexOf(sessionId);
    }

    @Override
    public void bindNowValues(Map nowValues) {
        mNowData = nowValues;
        notifyDataSetChanged();
    }

    @Override
    public void bindChartData(Map charts) {
        if (charts == null || charts.isEmpty()) {
            return;
        }
        mChartData = charts;
        notifyItemRangeChanged(0, mChartData.size(), PAYLOAD_CHARTS_REFRESHED);
    }

    @Override
    public ViewingStreamsRecyclerAdapter.StreamViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewingStreamItemViewMvcImpl viewMvc = new ViewingStreamItemViewMvcImpl(mInflater, parent);
        viewMvc.registerListener(this);
        return new StreamViewHolder(viewMvc);
    }

    @Override
    public void onBindViewHolder(ViewingStreamsRecyclerAdapter.StreamViewHolder holder, int position) {
        holder.mViewMvc.bindData(mData.get(position), position, mResourceHelper);
    }

    @Override
    public void onBindViewHolder(ViewingStreamsRecyclerAdapter.StreamViewHolder holder, int position, List payloads) {
        if (payloads.isEmpty()) {
            holder.mViewMvc.bindData(mData.get(position), position, mResourceHelper);
        } else if (payloads.get(0) == PAYLOAD_CHARTS_REFRESHED) {
            holder.mViewMvc.bindChart(mChartData);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public boolean onItemMove(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target, int fromPosition, int toPosition) {
        mStreamsReordered = true;

        target.itemView.findViewById(R.id.title_container).setVisibility(View.GONE);

        swapPositions(fromPosition, toPosition);
        Collections.sort(mData, mStreamComparator);
        notifyItemMoved(fromPosition, toPosition);

        // make sure the first element is rebound to show the session title
        notifyItemChanged(0, PAYLOAD_TITLE_POSITION_CHANGED);

        return true;
    }

    private void swapPositions(int fromPosition, int toPosition) {
        String fromSensorString = ((Sensor) mData.get(fromPosition).get(SENSOR)).toString();
        String toSensorString = ((Sensor) mData.get(toPosition).get(SENSOR)).toString();

        mStreamPositions.set(toPosition, fromSensorString);
        mStreamPositions.set(fromPosition, toSensorString);
    }

    @Override
    public void finishDrag(RecyclerView.ViewHolder viewHolder) {
        notifyItemChanged(0, PAYLOAD_TITLE_POSITION_CHANGED);
    }

    @Override
    public void onItemSwipe(int position) {

    }

    @Override
    public void onStreamClicked(View view) {
        mListener.onStreamClicked(view);
    }

    @Override
    public void onSessionUpClicked(View view, long sessionId) {
        int startingPosition = getSessionPosition(sessionId);
        int switchPosition = startingPosition - 1;

        if (startingPosition > 0) {
            long switchSessionId = mSessionPositions.get(switchPosition);
            mSessionPositions.set(startingPosition, switchSessionId);
            mSessionPositions.set(switchPosition, sessionId);

            Collections.sort(mData, mStreamComparator);
            notifyDataSetChanged();
        }
    }

    @Override
    public void onSessionDownClicked(View view, long sessionId) {
        int startingPosition = getSessionPosition(sessionId);
        int switchPosition = startingPosition + 1;

        if (startingPosition < mSessionPositions.size() - 1) {
            long switchSessionId = mSessionPositions.get(switchPosition);
            mSessionPositions.set(startingPosition, switchSessionId);
            mSessionPositions.set(switchPosition, sessionId);

            Collections.sort(mData, mStreamComparator);
            notifyDataSetChanged();
        }
    }

    private boolean positionsPrepared() {
        return mData.size() == mStreamPositions.size();
    }
}
