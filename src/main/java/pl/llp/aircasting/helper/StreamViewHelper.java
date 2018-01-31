package pl.llp.aircasting.helper;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.ApplicationState;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.CurrentSessionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 10/21/13
 * Time: 6:54 PM
 * To change this template use File | Settings | File Templates.
 */
@Singleton
public class StreamViewHelper {
    @Inject CurrentSessionManager currentSessionManager;
    @Inject ResourceHelper resourceHelper;
    @Inject SessionState sessionState;
    @Inject SessionDataFactory sessionData;
    @Inject ApplicationState applicationState;

    private static List<Integer> positionsWithTitle = new ArrayList<Integer>();

    public void setPositionsWithTitle(List positions) {
        positionsWithTitle = positions;
    }

    public void updateMeasurements(long sessionId, Sensor sensor, View view, int position) {
        int now = (int) sessionData.getNow(sensor, sessionId);
        TextView nowTextView = (TextView) view.findViewById(R.id.now);
        RelativeLayout sessionTitleContainer = (RelativeLayout) view.findViewById(R.id.title_container);

        if (positionsWithTitle.contains(position)) {
            setTitleView(sessionId, sessionTitleContainer);
        } else {
            sessionTitleContainer.setVisibility(View.GONE);
        }

        nowTextView.setBackgroundDrawable(resourceHelper.streamValueGrey);

        if (sessionState.sessionHasColoredBackground(sessionId)){
            setBackground(sensor, nowTextView, now);
        }

        if (sessionState.sessionHasNowValue(sessionId)) {
            nowTextView.setText(String.valueOf(now));
        } else {
            nowTextView.setText(R.string.empty);
        }
    }

    private void setTitleView(long sessionId, RelativeLayout sessionTitleView) {
        Session session = sessionData.getSession(sessionId);
        TextView sessionTitle = (TextView) sessionTitleView.findViewById(R.id.session_title);
        LinearLayout sessionButtonsContainer = (LinearLayout) sessionTitleView.findViewById(R.id.session_reorder_buttons);

        sessionTitleView.setVisibility(View.VISIBLE);
        sessionTitle.setCompoundDrawablesWithIntrinsicBounds(session.getDrawable(), 0, 0, 0);

        if (applicationState.dashboardState().isSessionReorderInProgress()) {
            sessionButtonsContainer.setVisibility(View.VISIBLE);
        } else {
            sessionButtonsContainer.setVisibility(View.GONE);
        }

        if (!session.hasTitle()) {
            sessionTitle.setText(R.string.unnamed);
        } else {
            sessionTitle.setText(session.getTitle());
        }
    }

    private void setBackground(Sensor sensor, View view, double value) {
        view.setBackgroundDrawable(resourceHelper.getStreamValueBackground(sensor, value));
    }
}
