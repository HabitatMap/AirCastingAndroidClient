package pl.llp.aircasting.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatDelegate;
import android.widget.Toast;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.*;
import pl.llp.aircasting.model.CurrentSessionManager;
import pl.llp.aircasting.model.CurrentSessionSensorManager;
import pl.llp.aircasting.model.Session;

/**
 * Created by radek on 21/06/17.
 */
public class ToggleAircastingManager {
    public AppCompatDelegate delegate;
    private Context context;
    private Activity activity;
    private CurrentSessionManager currentSessionManager;
    private SettingsHelper settingsHelper;
    private CurrentSessionSensorManager currentSessionSensorManager;
    private LocationHelper locationHelper;
    private DashboardChartManager dashboardChartManager;

    public ToggleAircastingManager(Activity activity,
                                   CurrentSessionManager currentSessionManager,
                                   SettingsHelper settingsHelper,
                                   CurrentSessionSensorManager currentSessionSensorManager,
                                   LocationHelper locationHelper,
                                   AppCompatDelegate delegate,
                                   Context context,
                                   DashboardChartManager dashboardChartManager) {
        this.activity = activity;
        this.currentSessionManager = currentSessionManager;
        this.settingsHelper = settingsHelper;
        this.currentSessionSensorManager = currentSessionSensorManager;
        this.locationHelper = locationHelper;
        this.delegate = delegate;
        this.context = context;
        this.dashboardChartManager = dashboardChartManager;
    }

    public void toggleAirCasting() {
        if (currentSessionManager.isSessionRecording()) {
            stopAirCasting();
        } else {
            startMobileAirCasting();
        }
    }

    public void stopAirCasting() {
        Session session = currentSessionManager.getCurrentSession();
        dashboardChartManager.stop();

        stopMobileAirCasting(session);
    }

    private void stopMobileAirCasting(Session session) {
        locationHelper.stop();
        Long sessionId = session.getId();
        if (session.isEmpty()) {
            ToastHelper.show(context, R.string.no_data, Toast.LENGTH_SHORT);
            currentSessionManager.discardSession(sessionId);
        } else {
            currentSessionManager.stopSession();

            if(session.isLocationless()) {
                currentSessionManager.finishSession(sessionId);
            } else if (settingsHelper.isContributingToCrowdMap()) {
                currentSessionManager.setContribute(sessionId, true);
                currentSessionManager.finishSession(sessionId);
            }
        }
    }

    public void startMobileAirCasting() {
        dashboardChartManager.start();

        activity.startActivity(new Intent(activity, StartMobileSessionActivity.class));
    }
}
