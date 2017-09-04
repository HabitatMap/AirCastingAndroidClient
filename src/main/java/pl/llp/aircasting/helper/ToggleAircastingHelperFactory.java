package pl.llp.aircasting.helper;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.support.v7.app.AppCompatDelegate;
import com.google.inject.Inject;
import pl.llp.aircasting.model.SensorManager;
import pl.llp.aircasting.model.SessionManager;
import roboguice.inject.InjectView;

/**
 * Created by radek on 13/07/17.
 */
public class ToggleAircastingHelperFactory {
    @Inject SessionManager sessionManager;
    @Inject SettingsHelper settingsHelper;
    @Inject LocationHelper locationHelper;
    @Inject SensorManager sensorManager;
    @Inject Context context;
    @Inject DashboardChartManager dashboardChartManager;

    public ToggleAircastingHelper getAircastingHelper(Activity activity, AppCompatDelegate delegate) {
        return new ToggleAircastingHelper(activity, sessionManager, settingsHelper, sensorManager, locationHelper, delegate, context, dashboardChartManager);
    }
}
