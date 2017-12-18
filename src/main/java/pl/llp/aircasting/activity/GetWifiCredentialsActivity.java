package pl.llp.aircasting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import pl.llp.aircasting.R;
import pl.llp.aircasting.sensor.airbeam.Airbeam2Configurator;
import roboguice.inject.InjectView;

/**
 * Created by radek on 12/12/17.
 */
public class GetWifiNetworkActivity extends DialogActivity implements View.OnClickListener {
    @InjectView(R.id.wifi_ssid) EditText wifi_ssid;
    @InjectView(R.id.wifi_password) EditText wifi_password;
    @InjectView(R.id.wifi_submit) Button wifi_submit;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_wifi_network);

        wifi_submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.wifi_submit:
                String ssid = wifi_ssid.getText().toString();
                String password = wifi_password.getText().toString();

//                Airbeam2Configurator
                startActivity(new Intent(this, StartFixedSessionActivity.class));
                break;
        }
        finish();
    }
}
