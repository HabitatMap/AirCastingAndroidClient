package pl.llp.aircasting.helper;

import android.view.View;
import android.widget.TextView;
import com.google.inject.Inject;
import pl.llp.aircasting.R;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.SensorManager;
import pl.llp.aircasting.model.SessionManager;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 10/21/13
 * Time: 6:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class StreamViewHelper {

    @Inject SessionManager sessionManager;
    @Inject SensorManager sensorManager;
    @Inject ResourceHelper resourceHelper;


    public void updateMeasurements(Sensor sensor, View view) {
        int now = (int) sessionManager.getNow(sensor);

        View nowView = view.findViewById(R.id.now_pane);
        View avgView = view.findViewById(R.id.avg_pane);
        View peakView = view.findViewById(R.id.peak_pane);

        TextView nowTextView = (TextView) nowView.findViewById(R.id.now);
        TextView avgTextView = (TextView) avgView.findViewById(R.id.avg);
        TextView peakTextView = (TextView) peakView.findViewById(R.id.peak);

        if (sensorManager.isSessionBeingViewed()) {
            nowView.setBackgroundColor(resourceHelper.gray);
            nowTextView.setVisibility(View.GONE);
        } else {
            nowTextView.setText(String.valueOf(now));
            nowTextView.setVisibility(View.VISIBLE);
            setBackground(sensor, nowView, now);
        }

        if (sessionManager.isSessionStarted() || sessionManager.isSessionSaved()) {
            int avg = (int) sessionManager.getAvg(sensor);
            int peak = (int) sessionManager.getPeak(sensor);

            avgTextView.setText(String.valueOf(avg));
            peakTextView.setText(String.valueOf(peak));

            setBackground(sensor, avgView, avg);
            setBackground(sensor, peakView, peak);

            avgView.setVisibility(View.VISIBLE);
            peakView.setVisibility(View.VISIBLE);
        } else {
            avgView.setVisibility(View.GONE);
            peakView.setVisibility(View.GONE);
        }
    }

    private void setBackground(Sensor sensor, View view, double value) {
        view.setBackgroundColor(resourceHelper.getColorAbsolute(sensor, value));
    }
}
