package pl.llp.aircasting.screens.airbeamConfiguration.views;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import pl.llp.aircasting.R;
import pl.llp.aircasting.common.BaseViewMvc;

public class WifiListItemViewMvcImpl implements BaseViewMvc {
    private View mRootView;
    private final TextView mNetworkNameTv;

    private Listener mListener;

    public interface Listener {
        void onWifiItemClick(TextView wifiNetworkNameTv);
    }

    public WifiListItemViewMvcImpl(LayoutInflater inflater, ViewGroup parent) {
        mRootView = inflater.inflate(R.layout.wifi_list_item, parent, false);
        mNetworkNameTv = findViewById(R.id.wifi_ssid);

        mRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onWifiItemClick(mNetworkNameTv);
            }
        });
    }

    public void registerListener(Listener listener) {
        mListener = listener;
    }

    public void bindWifiNetworkName(String networkName) {
        mNetworkNameTv.setText(networkName);
    }

    @Override
    public View getRootView() {
        return mRootView;
    }

    private <T extends View> T findViewById(int id) {
        return getRootView().findViewById(id);
    }
}
