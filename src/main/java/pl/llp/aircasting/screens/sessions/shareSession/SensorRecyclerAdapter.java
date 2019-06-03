package pl.llp.aircasting.screens.sessions.shareSession;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public class SensorRecyclerAdapter extends RecyclerView.Adapter<SensorRecyclerAdapter.SensorItemViewHolder>
        implements SensorItemViewMvcImpl.Listener {
    private final LayoutInflater mInflater;
    private List<String> mData;
    private SensorItemViewMvcImpl.Listener mListener;

    public SensorRecyclerAdapter(LayoutInflater inflater) {
        mInflater = inflater;
    }

    @Override
    public void onSensorSelected(View view) {
        mListener.onSensorSelected(view);
    }

    public class SensorItemViewHolder extends RecyclerView.ViewHolder {
        private final SensorItemViewMvcImpl mViewMvc;

        public SensorItemViewHolder(SensorItemViewMvcImpl viewMvc) {
            super(viewMvc.getRootView());
            mViewMvc = viewMvc;
        }
    }

    public void bindData(List<String> sensorNames) {
        mData = sensorNames;
        notifyDataSetChanged();
    }

    public void registerListener(SensorItemViewMvcImpl.Listener listener) {
        mListener = listener;
    }

    @Override
    public SensorItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SensorItemViewMvcImpl view = new SensorItemViewMvcImpl(mInflater, parent);
        view.registerListener(this);

        return new SensorItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SensorItemViewHolder holder, int position) {
        holder.mViewMvc.bindData(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
