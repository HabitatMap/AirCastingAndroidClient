package pl.llp.aircasting.screens.airbeamConfiguration;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pl.llp.aircasting.R;
import pl.llp.aircasting.screens.airbeamConfiguration.views.GetWifiCredentialsViewMvcImpl;
import pl.llp.aircasting.screens.airbeamConfiguration.views.WifiListItemViewMvcImpl;
import pl.llp.aircasting.screens.sessionRecord.StartFixedSessionActivity;
import pl.llp.aircasting.screens.common.base.DialogActivity;

import static pl.llp.aircasting.Intents.CONTINUE_STREAMING;

/**
 * Created by radek on 12/12/17.
 */
public class GetWifiCredentialsActivity extends DialogActivity implements GetWifiCredentialsViewMvcImpl.Listener, WifiListItemViewMvcImpl.Listener {
    private boolean mContinueStreaming;
    private String mWifiSsid;
    private GetWifiCredentialsViewMvcImpl mGetWifiCredentialsView;
    private WifiManager mWifiManager;
    private BroadcastReceiver mWifiScanReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContinueStreaming = getIntent().getBooleanExtra(CONTINUE_STREAMING, false);
        mGetWifiCredentialsView= new GetWifiCredentialsViewMvcImpl(this, null);
        mGetWifiCredentialsView.registerListeners(this, this);
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        registerWifiScanReceiver();
        startScan();

        setContentView(mGetWifiCredentialsView.getRootView());
        initDialogToolbar("WiFi Name & Password");
    }

    private void registerWifiScanReceiver() {
        mWifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                onScanResult();
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mWifiScanReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        unregisterReceiver(mWifiScanReceiver);
    }

    private void startScan() {
        mGetWifiCredentialsView.showProgress();
        mWifiManager.startScan();
    }

    private void onScanResult() {
        List<String> wifiSSIDs = prepareScanResults(mWifiManager.getScanResults());

        mGetWifiCredentialsView.hideProgress();
        mGetWifiCredentialsView.bindData(wifiSSIDs);
    }

    private List<String> prepareScanResults(List<ScanResult> scanResults) {
        List<String> wifiSSIDs = new ArrayList<>();

        for (ScanResult result : scanResults) {
            String ssid = result.SSID;
            if (!wifiSSIDs.contains(ssid) && !ssid.isEmpty()) {
                wifiSSIDs.add(ssid);
            }
        }

        return wifiSSIDs;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.toolbar_refresh_wifi_list, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        super.onOptionsItemSelected(menuItem);

        mGetWifiCredentialsView.returnToWifiList();
        startScan();

        return true;
    }

    @Override
    public void onSubmitClick(TextView wifiPasswordTv) {
        String password = wifiPasswordTv.getText().toString();

        airbeam2Configurator.setWifi(mWifiSsid, password);

        if (mContinueStreaming) {
            airbeam2Configurator.sendFinalAb2Config();
        } else {
            startActivity(new Intent(this, StartFixedSessionActivity.class));
        }

        finish();
    }

    @Override
    public void onWifiItemClick(TextView view) {
        mWifiSsid = String.valueOf(view.getText());
    }
}
