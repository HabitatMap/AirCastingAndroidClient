package pl.llp.aircasting.helper;

import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.MarkerSize;
import pl.llp.aircasting.R;

import static java.lang.String.valueOf;

@Singleton
public class GaugeHelper {
    @Inject ResourceHelper resourceHelper;
    @Inject ThresholdsHelper thresholdHelper;

    /**
     * Update a set of now/avg/peak gauges
     *
     * @param view The view containing the three gauges
     * @param now  Value to set for the now gauge
     * @param avg  Value to set for the avg gauge
     * @param peak Value to set for the peak gauge
     */
    public void updateGauges(View view, String sensorName, int now, int avg, int peak) {
        TextView nowGauge = (TextView) view.findViewById(R.id.now_gauge);
        TextView avgGauge = (TextView) view.findViewById(R.id.avg_gauge);
        TextView peakGauge = (TextView) view.findViewById(R.id.peak_gauge);

        updateGauge(nowGauge, sensorName, MarkerSize.BIG, now);
        updateGauge(avgGauge, sensorName, MarkerSize.SMALL, avg);
        updateGauge(peakGauge, sensorName, MarkerSize.SMALL, peak);
    }

    private void updateGauge(TextView view, String sensorName, MarkerSize size, int value) {
        view.setText(valueOf(value));
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, resourceHelper.getTextSize(value, size));
        view.setBackgroundDrawable(resourceHelper.getGaugeAbsolute(sensorName, size, value));
    }
}
