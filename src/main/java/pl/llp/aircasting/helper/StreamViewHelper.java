package pl.llp.aircasting.helper;

import android.view.View;
import android.widget.TextView;
import com.google.inject.Inject;
import pl.llp.aircasting.R;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.CurrentSessionManager;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 10/21/13
 * Time: 6:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class StreamViewHelper {
    @Inject
    CurrentSessionManager currentSessionManager;
    @Inject ResourceHelper resourceHelper;

    public void updateMeasurements(Sensor sensor, View view, int position) {
        int now = (int) currentSessionManager.getNow(sensor);
        TextView nowTextView = (TextView) view.findViewById(R.id.now);
        TextView sessionTitle = (TextView) view.findViewById(R.id.session_title);

        if (position != 0) {
            sessionTitle.setVisibility(View.GONE);
        } else {
            setTitleView(sessionTitle);
        }

        if (!currentSessionManager.isSessionRecording()) {
            nowTextView.setBackgroundDrawable(resourceHelper.streamValueGrey);
        } else {
            setBackground(sensor, nowTextView, now);
        }

        if (!currentSessionManager.isSessionBeingViewed()) {
            nowTextView.setText(String.valueOf(now));
        }
    }

    private void setTitleView(TextView sessionTitle) {
        Session session = currentSessionManager.getCurrentSession();

        if (currentSessionManager.isSessionRecording()) {
            sessionTitle.setCompoundDrawablesWithIntrinsicBounds(session.getDrawable(), 0, 0, 0);
            sessionTitle.setText("Recording session");
        } else if (currentSessionManager.isSessionBeingViewed()) {
            sessionTitle.setCompoundDrawablesWithIntrinsicBounds(session.getDrawable(), 0, 0, 0);
            sessionTitle.setText(session.getTitle());
        }
    }

    private void setBackground(Sensor sensor, View view, double value) {
        view.setBackgroundDrawable(resourceHelper.getStreamValueBackground(sensor, value));
    }
}
