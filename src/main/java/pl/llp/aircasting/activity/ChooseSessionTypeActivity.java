package pl.llp.aircasting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.model.CurrentSessionManager;
import pl.llp.aircasting.sensor.airbeam.Airbeam2ConfigMessageEvent;
import pl.llp.aircasting.sensor.airbeam.Airbeam2Configurator;
import roboguice.inject.InjectView;

import java.util.UUID;

/**
 * Created by radek on 11/12/17.
 */
public class ChooseAirbeam2SessionTypeActivity extends DialogActivity implements View.OnClickListener {
    @InjectView(R.id.mobile_session_button) Button mobileSessionButton;
    @InjectView(R.id.fixed_session_button) Button fixedSessionButton;

    @Inject EventBus eventBus;
    @Inject CurrentSessionManager currentSessionManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.session_type);

        mobileSessionButton.setOnClickListener(this);
        fixedSessionButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.mobile_session_button:
                byte[] message = Airbeam2Configurator.BLUETOOTH_CONFIGURATION_MESSAGE;
                eventBus.post(new Airbeam2ConfigMessageEvent(message));
                Intents.startDashboardActivity(this, true);
                break;
            case R.id.fixed_session_button:
                currentSessionManager.createAndSetFixedSession();
                UUID uuid = currentSessionManager.getCurrentSession().getUUID();
                String authToken = settingsHelper.getAuthToken();

                airbeam2Configurator.sendUUIDAndAuthToken(uuid, authToken);
                startActivity(new Intent(this, ChooseStreamingMethodActivity.class));
                break;
        }

        finish();
    }
}
