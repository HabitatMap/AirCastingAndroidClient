package pl.llp.aircasting.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatDelegate;
import android.widget.Toast;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.*;
import pl.llp.aircasting.model.SensorManager;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.SessionManager;

/**
 * Created by radek on 21/06/17.
 */
public class ToggleAircastingHelper {
    public AppCompatDelegate delegate;
    private Context context;
    private Activity activity;
    private SessionManager sessionManager;
    private SettingsHelper settingsHelper;
    private SensorManager sensorManager;
    private LocationHelper locationHelper;
    private DashboardChartManager dashboardChartManager;

    public ToggleAircastingHelper(Activity activity,
                                  SessionManager sessionManager,
                                  SettingsHelper settingsHelper,
                                  SensorManager sensorManager,
                                  LocationHelper locationHelper,
                                  AppCompatDelegate delegate,
                                  Context context,
                                  DashboardChartManager dashboardChartManager) {
        this.activity = activity;
        this.sessionManager = sessionManager;
        this.settingsHelper = settingsHelper;
        this.sensorManager = sensorManager;
        this.locationHelper = locationHelper;
        this.delegate = delegate;
        this.context = context;
        this.dashboardChartManager = dashboardChartManager;
    }

    public void toggleAirCasting() {
        if (sessionManager.isSessionStarted()) {
            stopAirCasting();
        } else {
            if (sensorManager.getSensorByName("Phone Microphone") == null) {
                chooseSessionType();
            } else {
                startMobileAirCasting();
            }
        }
    }

    private void chooseSessionType() {
        Intent intent = new Intent(activity, ChooseSessionTypeActivity.class);
        activity.startActivityForResult(intent, Intents.CHOOSE_SESSION_TYPE);
    }

    public void stopAirCasting() {
        Session session = sessionManager.getCurrentSession();
        dashboardChartManager.stop();

        if (session.isFixed()) {
            stopFixedAirCasting(session);
        } else {
            stopMobileAirCasting(session);
        }
    }

    private void stopMobileAirCasting(Session session) {
        locationHelper.stop();
        Long sessionId = session.getId();
        if (session.isEmpty()) {
            Toast.makeText(context, R.string.no_data, Toast.LENGTH_SHORT).show();
            sessionManager.discardSession(sessionId);
        } else {
            sessionManager.stopSession();
            Intent intent = new Intent(activity, SaveSessionActivity.class);
            intent.putExtra(Intents.SESSION_ID, sessionId);
            activity.startActivityForResult(intent, Intents.SAVE_DIALOG);
        }
    }

    private void stopFixedAirCasting(Session session) {
        locationHelper.stop();
        Long sessionId = session.getId();
        if (session.isEmpty()) {
            Toast.makeText(context, R.string.no_data, Toast.LENGTH_SHORT).show();
            sessionManager.discardSession(sessionId);
        } else {
            sessionManager.stopSession();
            sessionManager.finishSession(sessionId);
        }
    }

    public void startMobileAirCasting() {
        dashboardChartManager.start();

        activity.startActivity(new Intent(activity, StartMobileSessionActivity.class));
    }

    public void startFixedAirCasting() {
        dashboardChartManager.start();

        if (settingsHelper.hasNoCredentials()) {
            Toast.makeText(context, R.string.account_reminder, Toast.LENGTH_LONG).show();
        } else {
            activity.startActivity(new Intent(activity, StartFixedSessionActivity.class));
        }
    }
}
