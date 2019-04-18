package pl.llp.aircasting.screens.airbeamConfiguration;

import android.net.wifi.ScanResult;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pl.llp.aircasting.screens.airbeamConfiguration.views.WifiListItemViewMvcImpl;

public class WifiListRecyclerAdapter extends RecyclerView.Adapter<WifiListRecyclerAdapter.WifiItemViewHolder> implements WifiListItemViewMvcImpl.Listener {
    private final LayoutInflater mInflater;
    private List<String> mData = new ArrayList<>();

    private WifiListItemViewMvcImpl.Listener mListener;

    public WifiListRecyclerAdapter(LayoutInflater inflater) {
        mInflater = inflater;
    }

    @Override
    public void onWifiItemClick(TextView view) {
        mListener.onWifiItemClick(view);
    }

    public class WifiItemViewHolder extends RecyclerView.ViewHolder {
        private final WifiListItemViewMvcImpl mViewMvc;

        public WifiItemViewHolder(WifiListItemViewMvcImpl viewMvc) {
            super(viewMvc.getRootView());
            mViewMvc = viewMvc;
        }
    }

    public void registerListener(WifiListItemViewMvcImpl.Listener listener) {
        mListener = listener;
    }

    public void bindData(List<String> data) {
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    public WifiItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        WifiListItemViewMvcImpl viewMvc = new WifiListItemViewMvcImpl(mInflater, parent);
        viewMvc.registerListener(this);
        return new WifiItemViewHolder(viewMvc);
    }

    @Override
    public void onBindViewHolder(WifiItemViewHolder holder, int position) {
        holder.mViewMvc.bindWifiNetworkName(mData.get(position));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
