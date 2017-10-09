package pl.llp.aircasting.helper;

import android.view.View;
import android.widget.TextView;
import com.google.inject.Inject;
import pl.llp.aircasting.R;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.SensorManager;
import pl.llp.aircasting.model.Session;
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
    @Inject ResourceHelper resourceHelper;

    public void updateMeasurements(Sensor sensor, View view, int position) {
        int now = (int) sessionManager.getNow(sensor);
        TextView nowTextView = (TextView) view.findViewById(R.id.now);
        TextView sessionTitle = (TextView) view.findViewById(R.id.session_title);

        if (position != 0) {
            sessionTitle.setVisibility(View.GONE);
        } else {
            setTitleView(sessionTitle);
        }

        if (!sessionManager.isSessionRecording()) {
            nowTextView.setBackgroundDrawable(resourceHelper.streamValueGrey);
        } else {
            setBackground(sensor, nowTextView, now);
        }

        if (!sessionManager.isSessionBeingViewed()) {
            nowTextView.setText(String.valueOf(now));
        }
    }

    private void setTitleView(TextView sessionTitle) {
        Session session = sessionManager.getCurrentSession();

        if (sessionManager.isSessionRecording()) {
            sessionTitle.setCompoundDrawablesWithIntrinsicBounds(session.getDrawable(), 0, 0, 0);
            sessionTitle.setText("Recording session");
        } else if (sessionManager.isSessionBeingViewed()) {
            sessionTitle.setCompoundDrawablesWithIntrinsicBounds(session.getDrawable(), 0, 0, 0);
            sessionTitle.setText(session.getTitle());
        }
    }

    private void setBackground(Sensor sensor, View view, double value) {
        view.setBackgroundDrawable(resourceHelper.getStreamValueBackground(sensor, value));
    }
}
