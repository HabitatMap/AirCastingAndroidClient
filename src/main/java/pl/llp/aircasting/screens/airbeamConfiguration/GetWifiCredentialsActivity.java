package pl.llp.aircasting.screens.airbeamConfiguration;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

        setContentView(mGetWifiCredentialsView.getRootView());
        initDialogToolbar("WiFi Name & Password");
    }

    @Override
    public void onStart() {
        super.onStart();

        registerWifiScanReceiver();
        startScan();
    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterReceiver(mWifiScanReceiver);
    }

    private void checkForLocationServices() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Dialog dialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.location_required)
                    .setPositiveButton(R.string.yes, null)
                    .setNegativeButton(R.string.no, null)
                    .setCancelable(false)
                    .create();

            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(final DialogInterface dialog) {
                    Button yes = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                    yes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            dialog.dismiss();
                        }
                    });

                    Button no = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                    no.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    });
                }
            });

            dialog.show();
        }
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

    private void startScan() {
        checkForLocationServices();
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
