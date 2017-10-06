package pl.llp.aircasting.activity;

import android.app.Application;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.inject.Inject;
import pl.llp.aircasting.R;
import pl.llp.aircasting.helper.LocationHelper;
import pl.llp.aircasting.model.SessionManager;
import roboguice.inject.InjectView;

/**
 * Created by radek on 04/09/17.
 */
public class StartMobileSessionActivity extends DialogActivity implements View.OnClickListener {
    @InjectView(R.id.cancel) Button cancelButton;
    @InjectView(R.id.start_session) Button startButton;

    @InjectView(R.id.session_title) EditText sessionTitle;
    @InjectView(R.id.session_tags) EditText sessionTags;
    @InjectView(R.id.session_description) EditText sessionDescription;

    @Inject Application context;
    @Inject SessionManager sessionManager;
    @Inject LocationHelper locationHelper;
    @Inject LocationManager locationManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.start_mobile_session);

        cancelButton.setOnClickListener(this);
        startButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.start_session:
                startMobileSession();
                break;
            case R.id.cancel:
                finish();
                break;
        }
    }

    private void startMobileSession() {
        String title = sessionTitle.getText().toString();
        String tags = sessionTags.getText().toString();
        String description = sessionDescription.getText().toString();

        if (settingsHelper.areMapsDisabled()) {
            sessionManager.startMobileSession(title, tags, description, true);
        } else {
            if (locationHelper.getLastLocation() == null) {
                RecordWithoutGPSAlert recordAlert = new RecordWithoutGPSAlert(title, tags, description, this, delegate, sessionManager, true);
                recordAlert.display();
                return;
            } else {
                sessionManager.startMobileSession(title, tags, description, false);
                showWarnings();
            }
        }

        finish();
    }

    private void showWarnings() {
        if (settingsHelper.hasNoCredentials()) {
            Toast.makeText(context, R.string.account_reminder, Toast.LENGTH_LONG).show();
        }

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (locationHelper.hasNoGPSFix()) {
                Toast.makeText(context, R.string.no_gps_fix_warning, Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(context, R.string.gps_off_warning, Toast.LENGTH_LONG).show();
        }
    }
}
