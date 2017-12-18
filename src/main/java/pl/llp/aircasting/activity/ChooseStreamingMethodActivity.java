package pl.llp.aircasting.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.inject.Inject;
import pl.llp.aircasting.R;
import pl.llp.aircasting.helper.DashboardChartManager;
import roboguice.inject.InjectView;

/**
 * Created by radek on 01/09/17.
 */
public class ChooseStreamingMethodActivity extends DialogActivity implements View.OnClickListener {
    @InjectView(R.id.wifi_button) Button wifiButton;
    @InjectView(R.id.cellular_button) Button cellularButton;

    @Inject Context context;
    @Inject DashboardChartManager dashboardChartManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_streaming_method);

        wifiButton.setOnClickListener(this);
        cellularButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.wifi_button:
                startActivity(new Intent(this, GetWifiCredentialsActivity.class));
                break;
            case R.id.cellular_button:
                airbeam2Configurator.setCellular();
                startFixedAirCasting();
                break;
        }
        finish();
    }

    public void startFixedAirCasting() {
        dashboardChartManager.start();

        if (settingsHelper.hasNoCredentials()) {
            Toast.makeText(context, R.string.account_reminder, Toast.LENGTH_LONG).show();
        } else {
            startActivity(new Intent(this, StartFixedSessionActivity.class));
        }
    }
}
