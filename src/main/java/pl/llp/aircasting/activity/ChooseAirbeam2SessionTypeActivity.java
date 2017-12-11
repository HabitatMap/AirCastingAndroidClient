package pl.llp.aircasting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.sensor.airbeam.Airbeam2ConfigMessageEvent;

/**
 * Created by radek on 11/12/17.
 */
public class ChooseAirbeam2SessionTypeActivity extends ChooseSessionTypeActivity {
    @Inject EventBus eventBus;

    private byte[] message;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.mobile_session_button:
                message = new byte[] { (byte) 0xfe, (byte) 0x01, (byte) 0xff, (byte) '\n' };
                Intents.startDashboardActivity(this, true);
                break;
            case R.id.fixed_session_button:
                startActivity(new Intent(this, ChooseStreamingMethodActivity.class));
                break;
        }

        eventBus.post(new Airbeam2ConfigMessageEvent(message));
        finish();
    }
}
