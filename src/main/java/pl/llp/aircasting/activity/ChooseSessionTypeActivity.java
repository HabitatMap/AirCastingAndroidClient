package pl.llp.aircasting.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.inject.Inject;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.model.CurrentSessionManager;
import pl.llp.aircasting.model.ViewingSessionsManager;
import roboguice.inject.InjectView;

import java.util.UUID;

/**
 * Created by radek on 11/12/17.
 */
public class ChooseSessionTypeActivity extends DialogActivity implements View.OnClickListener {
    @InjectView(R.id.mobile_session_button) Button mobileSessionButton;
    @InjectView(R.id.fixed_session_button) Button fixedSessionButton;

    @Inject ViewingSessionsManager viewingSessionsManager;
    @Inject Context context;

    private final Handler handler = new Handler();
    private int sleepTime = 1000;

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
                airbeam2Configurator.configureBluetooth();
                Intents.startDashboardActivity(this, true);
                break;
            case R.id.fixed_session_button:
                Toast.makeText(context, R.string.configuring_airbeam, sleepTime).show();

                viewingSessionsManager.createAndSetFixedSession();
                UUID uuid = viewingSessionsManager.getStreamingSession().getUUID();
                final String authToken = settingsHelper.getAuthToken();

                // UUID and auth are sent to prolong the AB2 configuration mode
                airbeam2Configurator.sendUUIDAndAuthToken(uuid, authToken);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(context, ChooseStreamingMethodActivity.class));
                    }
                }, sleepTime);

                break;
        }

        finish();
    }
}
