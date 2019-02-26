package pl.llp.aircasting.screens.dashboard.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pl.llp.aircasting.screens.common.helpers.ResourceHelper;
import pl.llp.aircasting.screens.dashboard.helper.StreamItemTouchHelperAdapter;
import pl.llp.aircasting.screens.dashboard.views.StreamItemViewMvc;
import pl.llp.aircasting.screens.dashboard.views.ViewingStreamItemViewMvcImpl;

public class ViewingStreamsRecyclerAdapter extends RecyclerView.Adapter<ViewingStreamsRecyclerAdapter.StreamViewHolder>
        implements StreamRecyclerAdapter, StreamItemViewMvc.Listener, StreamItemTouchHelperAdapter {
    private final LayoutInflater mInflater;
    private final StreamItemViewMvc.Listener mListener;
    private final ResourceHelper mResourceHelper;

    private List<Map<String, Object>> mData = new ArrayList();
    private Map<String, Double> mNowData = new HashMap<>();
    private Map mChartData;


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
        mData = data;
//        Collections.sort(mData, mStreamComparator);
//        prepareStreamPositions();

        notifyDataSetChanged();
    }

    @Override
    public void bindNowValues(Map nowValues) {
        mNowData = nowValues;
        notifyDataSetChanged();
    }

    @Override
    public void bindChartData(Map charts) {
        mChartData = charts;
        notifyDataSetChanged();
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
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public boolean onItemMove(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target, int fromPosition, int toPosition) {
        return false;
    }

    @Override
    public void finishDrag(RecyclerView.ViewHolder viewHolder) {

    }

    @Override
    public void onItemSwipe(int position) {

    }

    @Override
    public void onStreamClicked(View view) {
        mListener.onStreamClicked(view);
    }
}
