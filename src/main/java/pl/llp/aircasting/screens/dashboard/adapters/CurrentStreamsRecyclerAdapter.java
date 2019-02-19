package pl.llp.aircasting.screens.dashboard.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import pl.llp.aircasting.R;
import pl.llp.aircasting.screens.common.helpers.ResourceHelper;
import pl.llp.aircasting.screens.dashboard.helper.StreamItemTouchHelperAdapter;
import pl.llp.aircasting.screens.dashboard.views.StreamItemViewMvc;
import pl.llp.aircasting.screens.dashboard.views.CurrentStreamItemViewMvcImpl;

public class CurrentStreamsRecyclerAdapter extends RecyclerView.Adapter<CurrentStreamsRecyclerAdapter.StreamViewHolder>
        implements StreamItemViewMvc.Listener, StreamItemTouchHelperAdapter {
    private static final String PAYLOAD_NOW_VALUES = "payload_now_values";
    private static final String PAYLOAD_CHARTS = "payload_charts";
    private static final String PAYLOAD_TITLE = "payload_title";

    private final LayoutInflater mInflater;
    private final Listener mListener;
    private final ResourceHelper mResourceHelper;
    private List<Map> mData = new ArrayList();
    private TreeMap<String, Double> mNowData = new TreeMap();
    private Map mChartData;

    public interface Listener {
        void onStreamClicked(View view);
    }

    public CurrentStreamsRecyclerAdapter(LayoutInflater inflater, Listener listener, ResourceHelper resourceHelper) {
        mInflater = inflater;
        mListener = listener;
        mResourceHelper = resourceHelper;
    }

    public void bindData(List data) {
        mData = data;
        notifyDataSetChanged();
    }

    public void bindNowValues(TreeMap recentMeasurementsData) {
        mNowData = recentMeasurementsData;
        notifyItemRangeChanged(0, recentMeasurementsData.size(), PAYLOAD_NOW_VALUES);
    }

    public void bindChartData(Map liveCharts) {
        mChartData = liveCharts;
        notifyItemRangeChanged(0, liveCharts.size(), PAYLOAD_CHARTS);
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
        } else if (payloads.get(0) == PAYLOAD_NOW_VALUES) {
            if (mData.size() == mNowData.size()) {
                holder.mViewMvc.bindNowValue(mNowData);
            }
        } else if (payloads.get(0) == PAYLOAD_CHARTS) {
            if (mData.size() == mChartData.size()) {
                holder.mViewMvc.bindChart(mChartData);
            }
        } else if (payloads.get(0) == PAYLOAD_TITLE) {
            if (mData.size() == mChartData.size()) {
                holder.mViewMvc.bindSessionTitle(position);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public void onStreamClicked(View view) {
        mListener.onStreamClicked(view);
    }

    static class StreamViewHolder extends RecyclerView.ViewHolder {
        private final StreamItemViewMvc mViewMvc;

        public StreamViewHolder(StreamItemViewMvc viewMvc) {
            super(viewMvc.getRootView());
            mViewMvc = viewMvc;
        }
    }

    @Override
    public boolean onItemMove(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target, int fromPosition, int toPosition) {
        target.itemView.findViewById(R.id.title_container).setVisibility(View.GONE);

        if (fromPosition > toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mData, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mData, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);

        // make sure the first element is rebound to show the session title
        notifyItemChanged(0, PAYLOAD_TITLE);

        return true;
    }

    @Override
    public void finishDrag(RecyclerView.ViewHolder viewHolder) {
        notifyItemChanged(0, PAYLOAD_TITLE);
    }

    @Override
    public void onItemSwipe(int position) {}
}
