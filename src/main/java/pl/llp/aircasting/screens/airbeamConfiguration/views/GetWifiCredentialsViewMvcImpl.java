package pl.llp.aircasting.screens.airbeamConfiguration.views;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import pl.llp.aircasting.R;
import pl.llp.aircasting.common.BaseViewMvc;
import pl.llp.aircasting.screens.airbeamConfiguration.WifiListRecyclerAdapter;

public class GetWifiCredentialsViewMvcImpl implements BaseViewMvc, WifiListItemViewMvcImpl.Listener {
    private final View mRootView;
    private final LayoutInflater mInflater;
    private final RecyclerView mWifiList;
    private final EditText mWifiPassword;
    private final Button mSubmit;
    private final WifiListRecyclerAdapter mWifiListRecyclerAdapter;
    private final AppCompatActivity mContext;
    private final ProgressBar mScanProgress;

    private Listener mOnClickListener;
    private WifiListItemViewMvcImpl.Listener mOnItemClickListener;

    public interface Listener {
        void onSubmitClick(TextView wifiPasswordTv);
    }

    public GetWifiCredentialsViewMvcImpl(AppCompatActivity context, ViewGroup parent) {
        mContext = context;
        mInflater = context.getLayoutInflater();
        mRootView = mInflater.inflate(R.layout.get_wifi_network, parent, false);
        mWifiList = findViewById(R.id.wifi_list);
        mWifiPassword = findViewById(R.id.wifi_password);
        mSubmit = findViewById(R.id.wifi_submit);
        mScanProgress = findViewById(R.id.scan_progress_bar);
        mScanProgress.getIndeterminateDrawable().setColorFilter(Color.parseColor("#0bb0f2"), PorterDuff.Mode.MULTIPLY);

        mWifiListRecyclerAdapter = new WifiListRecyclerAdapter(mInflater);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        mWifiList.setLayoutManager(layoutManager);
        mWifiListRecyclerAdapter.registerListener(this);
        mWifiList.setAdapter(mWifiListRecyclerAdapter);

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnClickListener.onSubmitClick((TextView) findViewById(R.id.wifi_password));
            }
        });
    }

    public void registerListeners(Listener onClickListener, WifiListItemViewMvcImpl.Listener itemClickListener) {
        mOnClickListener = onClickListener;
        mOnItemClickListener = itemClickListener;
    }

    public void bindData(List<String> data) {
        if (!data.isEmpty()) {
            mWifiList.setVisibility(View.VISIBLE);
        } else {
            mWifiList.setVisibility(View.GONE);
        }

        mWifiListRecyclerAdapter.bindData(data);
    }

    public void showProgress() {
        mScanProgress.setVisibility(View.VISIBLE);
    }

    public void hideProgress() {
        mScanProgress.setVisibility(View.GONE);
    }

    @Override
    public View getRootView() {
        return mRootView;
    }

    private <T extends View> T findViewById(int id) {
        return getRootView().findViewById(id);
    }

    @Override
    public void onWifiItemClick(TextView view) {
        swapToPasswordLayout();
        mOnItemClickListener.onWifiItemClick(view);
    }

    public void returnToWifiList() {
        mWifiList.setVisibility(View.VISIBLE);
        mWifiPassword.setVisibility(View.GONE);
        mSubmit.setVisibility(View.GONE);
    }

    private void swapToPasswordLayout() {
        mWifiList.setVisibility(View.GONE);
        mWifiPassword.setVisibility(View.VISIBLE);
        mSubmit.setVisibility(View.VISIBLE);
    }
}
