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
import java.util.TreeMap;

import pl.llp.aircasting.R;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.screens.common.helpers.ResourceHelper;
import pl.llp.aircasting.screens.dashboard.helper.StreamItemTouchHelperAdapter;
import pl.llp.aircasting.screens.dashboard.views.StreamItemViewMvc;
import pl.llp.aircasting.screens.dashboard.views.CurrentStreamItemViewMvcImpl;

import static pl.llp.aircasting.screens.dashboard.viewModel.DashboardViewModel.SENSOR;

public class CurrentStreamsRecyclerAdapter extends RecyclerView.Adapter<CurrentStreamsRecyclerAdapter.StreamViewHolder>
        implements StreamRecyclerAdapter, StreamItemViewMvc.Listener, StreamItemTouchHelperAdapter {
    private static final String PAYLOAD_NOW_VALUES_UPDATE = "payload_now_values";
    public static final String PAYLOAD_CHARTS_REFRESHED = "payload_charts";
    public static final String PAYLOAD_TITLE_POSITION_CHANGED = "payload_title";

    private static boolean mStreamsReordered = false;

    private final LayoutInflater mInflater;
    private final StreamItemViewMvc.Listener mListener;
    private final ResourceHelper mResourceHelper;
    private List<Map<String, Object>> mData = new ArrayList();
    private Map<String, Double> mNowData = new HashMap<>();
    private Map mChartData;
    private Map<String, Integer> mStreamPositions = new TreeMap();

    private final Comparator<Map<String, Object>> mStreamComparator = new Comparator<Map<String, Object>>() {
        @Override
        public int compare(@Nullable Map<String, Object> left, @Nullable Map<String, Object> right) {
            int result;
            ComparisonChain chain = ComparisonChain.start();

            Sensor leftSensor = (Sensor) left.get(SENSOR);
            Sensor rightSensor = (Sensor) right.get(SENSOR);

            result = chain.compare(leftSensor.getSensorName(), rightSensor.getSensorName()).result();

            if (mStreamsReordered && mData.size() == mStreamPositions.size()) {
                result = chain.compare(getPosition(leftSensor.getSensorName()), getPosition(rightSensor.getSensorName())).result();
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

    public CurrentStreamsRecyclerAdapter(LayoutInflater inflater, StreamItemViewMvc.Listener listener, ResourceHelper resourceHelper) {
        mInflater = inflater;
        mListener = listener;
        mResourceHelper = resourceHelper;
    }

    @Override
    public void bindData(List data) {
        mData = data;
        Collections.sort(mData, mStreamComparator);
        prepareStreamPositions();

        notifyDataSetChanged();
    }

    private void prepareStreamPositions() {
        int position = 0;
        mStreamPositions.clear();

        for (Map<String, Object> dataItem : mData) {
            Sensor sensor = (Sensor) dataItem.get(SENSOR);

            mStreamPositions.put(sensor.getSensorName(), position);

            position++;
        }
    }

    @Override
    public void bindNowValues(Map recentMeasurementsData) {
        mNowData = recentMeasurementsData;
        notifyItemRangeChanged(0, recentMeasurementsData.size(), PAYLOAD_NOW_VALUES_UPDATE);
    }

    @Override
    public void bindChartData(Map liveCharts) {
        mChartData = liveCharts;
        notifyItemRangeChanged(0, liveCharts.size(), PAYLOAD_CHARTS_REFRESHED);
    }

    @Override
    public StreamViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CurrentStreamItemViewMvcImpl viewMvc = new CurrentStreamItemViewMvcImpl(mInflater, parent);
        viewMvc.registerListener(this);
        return new StreamViewHolder(viewMvc);
    }

    @Override
    public void onBindViewHolder(StreamViewHolder holder, int position) {
        holder.mViewMvc.bindData(mData.get(position), position, mResourceHelper);
    }

    @Override
    public void onBindViewHolder(StreamViewHolder holder, int position, List payloads) {
        if (payloads.isEmpty()) {
            holder.mViewMvc.bindData(mData.get(position), position, mResourceHelper);
        } else if (payloads.get(0) == PAYLOAD_NOW_VALUES_UPDATE) {
            if (mData.size() == mNowData.size()) {
                holder.mViewMvc.bindNowValue(mNowData);
            }
        } else if (payloads.get(0) == PAYLOAD_CHARTS_REFRESHED) {
            if (mData.size() == mChartData.size()) {
                holder.mViewMvc.bindChart(mChartData);
            }
        } else if (payloads.get(0) == PAYLOAD_TITLE_POSITION_CHANGED) {
            if (mData.size() == mChartData.size()) {
                holder.mViewMvc.bindSessionTitle(position);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    private int getPosition(String sensorName) {
        return mStreamPositions.get(sensorName);
    }

    @Override
    public void onStreamClicked(View view) {
        mListener.onStreamClicked(view);
    }

    @Override
    public void onSessionUpClicked(View view, long sessionId) {}

    @Override
    public void onSessionDownClicked(View view, long sessionId) {}

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
        String fromSensor = ((Sensor) mData.get(fromPosition).get(SENSOR)).getSensorName();
        String toSensor = ((Sensor) mData.get(toPosition).get(SENSOR)).getSensorName();

        mStreamPositions.put(fromSensor, toPosition);
        mStreamPositions.put(toSensor, fromPosition);
    }

    @Override
    public void finishDrag(RecyclerView.ViewHolder viewHolder) {
        notifyItemChanged(0, PAYLOAD_TITLE_POSITION_CHANGED);
    }

    @Override
    public void onItemSwipe(int position) {}
}
