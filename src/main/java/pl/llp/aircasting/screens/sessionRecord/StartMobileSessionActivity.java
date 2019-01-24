package pl.llp.aircasting.screens.sessionRecord;

import android.app.Application;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.inject.Inject;
import pl.llp.aircasting.R;
import pl.llp.aircasting.screens.common.helpers.LocationHelper;
import pl.llp.aircasting.screens.common.ToastHelper;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionManager;
import pl.llp.aircasting.screens.common.base.DialogActivity;
import roboguice.inject.InjectView;

/**
 * Created by radek on 04/09/17.
 */
public class StartMobileSessionActivity extends DialogActivity implements View.OnClickListener {
    @InjectView(R.id.start_session) Button startButton;
    @InjectView(R.id.start_session_and_share) Button startAndShareButton;

    @InjectView(R.id.session_title) EditText sessionTitle;
    @InjectView(R.id.session_tags) EditText sessionTags;

    @Inject Application context;
    @Inject CurrentSessionManager currentSessionManager;
    @Inject LocationHelper locationHelper;
    @Inject LocationManager locationManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.start_mobile_session);
        initDialogToolbar("Session Details");

        startButton.setOnClickListener(this);

        if (settingsHelper.isContributingToCrowdMap()) {
            startAndShareButton.setVisibility(View.GONE);
        } else {
            startAndShareButton.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.start_session:
                startMobileSession();
                break;
            case R.id.start_session_and_share:
                settingsHelper.setContributeToCrowdmap(true);
                startMobileSession();
                break;
        }
    }

    private void startMobileSession() {
        locationHelper.start();

        String title = sessionTitle.getText().toString();
        String tags = sessionTags.getText().toString();

        if (settingsHelper.areMapsDisabled()) {
            currentSessionManager.startMobileSession(title, tags, true);
        } else {
            if (locationHelper.getLastLocation() == null) {
                RecordWithoutGPSAlert recordAlert = new RecordWithoutGPSAlert(title, tags, this, delegate, currentSessionManager, true);
                recordAlert.display();
                return;
            } else {
                currentSessionManager.startMobileSession(title, tags, false);
                showWarnings();
            }
        }

        finish();
    }

    private void showWarnings() {
        if (settingsHelper.hasNoCredentials()) {
            ToastHelper.show(context, R.string.account_reminder, Toast.LENGTH_LONG);
        }

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (locationHelper.hasNoGPSFix()) {
                ToastHelper.show(context, R.string.no_gps_fix_warning, Toast.LENGTH_LONG);
            }
        } else {
            ToastHelper.show(context, R.string.gps_off_warning, Toast.LENGTH_LONG);
        }
    }
}
