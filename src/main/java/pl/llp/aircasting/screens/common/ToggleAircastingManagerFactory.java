package pl.llp.aircasting.screens.common;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatDelegate;
import com.google.inject.Inject;

import pl.llp.aircasting.screens.common.helpers.LocationHelper;
import pl.llp.aircasting.screens.common.helpers.SettingsHelper;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionManager;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionSensorManager;
import pl.llp.aircasting.screens.dashboard.DashboardChartManager;

/**
 * Created by radek on 13/07/17.
 */
public class ToggleAircastingManagerFactory {
    @Inject
    CurrentSessionManager currentSessionManager;
    @Inject
    SettingsHelper settingsHelper;
    @Inject
    LocationHelper locationHelper;
    @Inject
    CurrentSessionSensorManager currentSessionSensorManager;
    @Inject Context context;
    @Inject
    DashboardChartManager dashboardChartManager;

    public ToggleAircastingManager getAircastingHelper(Activity activity, AppCompatDelegate delegate) {
        return new ToggleAircastingManager(activity,
                currentSessionManager,
                settingsHelper,
                currentSessionSensorManager,
                locationHelper,
                delegate,
                context,
                dashboardChartManager);
    }
}
