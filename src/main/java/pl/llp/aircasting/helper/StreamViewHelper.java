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

//        View avgView = view.findViewById(R.id.avg_pane);
//        View peakView = view.findViewById(R.id.peak_pane);
//        View sensorDetails = view.findViewById(R.id.sensor_details);
//        View sessionStats = view.findViewById(R.id.session_stats);

        TextView nowTextView = (TextView) view.findViewById(R.id.now);

        if (sensorManager.isSessionBeingViewed()) {
            nowTextView.setBackgroundColor(resourceHelper.gray);
        } else {
            nowTextView.setText(String.valueOf(now));
            setBackground(sensor, nowTextView, now);
        }

        if (!(sensor.isEnabled() && sessionManager.isSessionStarted()) || !sessionManager.isSessionSaved()) {
            nowTextView.setBackgroundColor(resourceHelper.gray);
        }
    }

    private void setBackground(Sensor sensor, View view, double value) {
        view.setBackgroundColor(resourceHelper.getColorAbsolute(sensor, value));
    }
}
