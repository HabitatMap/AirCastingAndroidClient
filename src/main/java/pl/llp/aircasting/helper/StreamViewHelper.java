package pl.llp.aircasting.helper;

import android.app.Application;
import android.view.View;
import android.widget.TextView;
import com.google.inject.Inject;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.ApplicationState;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.CurrentSessionManager;
import pl.llp.aircasting.util.Constants;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 10/21/13
 * Time: 6:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class StreamViewHelper {
    @Inject CurrentSessionManager currentSessionManager;
    @Inject ResourceHelper resourceHelper;
    @Inject ApplicationState state;

    public void updateMeasurements(long sessionId, Sensor sensor, View view, int position) {
        int now = (int) currentSessionManager.getNow(sensor);
        TextView nowTextView = (TextView) view.findViewById(R.id.now);
        TextView sessionTitle = (TextView) view.findViewById(R.id.session_title);

        if (position != 0) {
            sessionTitle.setVisibility(View.GONE);
        } else {
            setTitleView(sessionId, sessionTitle);
        }

        nowTextView.setBackgroundDrawable(resourceHelper.streamValueGrey);

        if (isSessionRecording(sessionId)){
            setBackground(sensor, nowTextView, now);
        }

        if (!isSessionBeingViewed(sessionId)) {
            nowTextView.setText(String.valueOf(now));
        }
    }

    private void setTitleView(long sessionId, TextView sessionTitle) {
        Session session = currentSessionManager.getCurrentSession();

        if (isSessionRecording(sessionId)) {
            sessionTitle.setCompoundDrawablesWithIntrinsicBounds(session.getDrawable(), 0, 0, 0);
            sessionTitle.setText("Recording session");
        } else {
            sessionTitle.setCompoundDrawablesWithIntrinsicBounds(session.getDrawable(), 0, 0, 0);
            sessionTitle.setText(session.getTitle());
        }
    }

    private void setBackground(Sensor sensor, View view, double value) {
        view.setBackgroundDrawable(resourceHelper.getStreamValueBackground(sensor, value));
    }

    private boolean isSessionRecording(long sessionId) {
        return sessionId == Constants.CURRENT_SESSION_FAKE_ID && state.recording().isRecording();
    }

    private boolean isSessionBeingViewed(long sessionId) {
        return sessionId != Constants.CURRENT_SESSION_FAKE_ID;
    }
}
