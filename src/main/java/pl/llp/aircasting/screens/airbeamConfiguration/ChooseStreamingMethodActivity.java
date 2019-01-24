package pl.llp.aircasting.screens.airbeamConfiguration;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.inject.Inject;
import pl.llp.aircasting.R;
import pl.llp.aircasting.screens.dashboard.DashboardChartManager;
import pl.llp.aircasting.screens.common.ToastHelper;
import pl.llp.aircasting.screens.sessionRecord.StartFixedSessionActivity;
import pl.llp.aircasting.screens.common.base.DialogActivity;
import roboguice.inject.InjectView;

import static pl.llp.aircasting.Intents.CONTINUE_STREAMING;

/**
 * Created by radek on 01/09/17.
 */
public class ChooseStreamingMethodActivity extends DialogActivity implements View.OnClickListener {
    @InjectView(R.id.wifi_button) Button wifiButton;
    @InjectView(R.id.cellular_button) Button cellularButton;

    @Inject Context context;
    @Inject DashboardChartManager dashboardChartManager;

    private boolean continueStreaming;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        continueStreaming = getIntent().getBooleanExtra(CONTINUE_STREAMING, false);
        setContentView(R.layout.choose_streaming_method);
        initDialogToolbar("Streaming Method");

        wifiButton.setOnClickListener(this);
        cellularButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.wifi_button:
                Intent intent = new Intent(this, GetWifiCredentialsActivity.class);
                intent.putExtra(CONTINUE_STREAMING, continueStreaming);

                startActivity(intent);
                break;
            case R.id.cellular_button:
                airbeam2Configurator.setCellular();

                if (continueStreaming) {
                    airbeam2Configurator.sendFinalAb2Config();
                } else {
                    startFixedAirCasting();
                }
                break;
        }
        finish();
    }

    public void startFixedAirCasting() {
        dashboardChartManager.start();

        if (settingsHelper.hasNoCredentials()) {
            ToastHelper.show(context, R.string.account_reminder, Toast.LENGTH_LONG);
        } else {
            startActivity(new Intent(this, StartFixedSessionActivity.class));
        }
    }
}
