package pl.llp.aircasting.helper;

import android.view.View;
import android.widget.TextView;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.model.internal.MeasurementLevel;
import pl.llp.aircasting.R;
import pl.llp.aircasting.model.Sensor;

import static java.lang.String.valueOf;

@Singleton
public class TopBarHelper {
    @Inject SettingsHelper settingsHelper;

    /**
     * Updates the top bar view with thresholds for the given sensor
     *
     * @param sensor the Sensor whose thresholds need to be displayed
     * @param parent a view which contains all the top bar labels
     */
    public void updateTopBar(Sensor sensor, View parent) {
        fill(sensor, parent, R.id.top_bar_very_low, MeasurementLevel.VERY_LOW);
        fill(sensor, parent, R.id.top_bar_low, MeasurementLevel.LOW);
        fill(sensor, parent, R.id.top_bar_mid, MeasurementLevel.MID);
        fill(sensor, parent, R.id.top_bar_high, MeasurementLevel.HIGH);
        fill(sensor, parent, R.id.top_bar_very_high, MeasurementLevel.VERY_HIGH);
    }

    private void fill(Sensor sensor, View parent, int id, MeasurementLevel level) {
        TextView veryLow = (TextView) parent.findViewById(id);

        int veryLowValue = settingsHelper.getThreshold(sensor, level);
        veryLow.setText(valueOf(veryLowValue));
    }
}
