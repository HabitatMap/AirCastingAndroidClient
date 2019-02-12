package pl.llp.aircasting.screens.dashboard.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import pl.llp.aircasting.screens.common.helpers.ResourceHelper;
import pl.llp.aircasting.screens.dashboard.views.StreamItemViewMvc;
import pl.llp.aircasting.screens.dashboard.views.StreamItemViewMvcImpl;

public class DashboardRecyclerAdapter extends RecyclerView.Adapter<DashboardRecyclerAdapter.StreamViewHolder> implements StreamItemViewMvc.Listener {
    private static final String PAYLOAD_NOW_VALUES = "payload_now_values";

    private final LayoutInflater mInflater;
    private final Listener mListener;
    private final ResourceHelper mResourceHelper;
    private List<Map> mData = new ArrayList();
    private TreeMap<String, Double> mNowData = new TreeMap();

    public interface Listener {
        void onStreamClicked(View view);
    }

    public DashboardRecyclerAdapter(LayoutInflater inflater, Listener listener, ResourceHelper resourceHelper) {
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

    @Override
    public StreamViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        StreamItemViewMvcImpl viewMvc = new StreamItemViewMvcImpl(mInflater, parent);
        viewMvc.registerListener(this);
        return new StreamViewHolder(viewMvc);
    }

    @Override
    public void onBindViewHolder(StreamViewHolder holder, int position) {
        holder.mViewMvc.bindData(mData.get(position), false, mResourceHelper);
    }

    @Override
    public void onBindViewHolder(StreamViewHolder holder, int position, List payloads) {
        if (payloads.isEmpty()) {
            holder.mViewMvc.bindData(mData.get(position), false, mResourceHelper);
        } else if (payloads.get(0) == PAYLOAD_NOW_VALUES) {
            if (mData.size() == mNowData.size()) {
                holder.mViewMvc.bindNowValue(mNowData);
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
}
