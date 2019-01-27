package pl.llp.aircasting.screens.dashboard.views;

import android.view.View;

import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.screens.common.helpers.ResourceHelper;
import pl.llp.aircasting.screens.common.sessionState.SessionState;

public interface StreamItemViewMvc {
    public interface Listener {
        void onStreamClicked(Sensor sensor);
    }

    View getRootView();

    void registerListener(Listener listener);

    void unregisterListener(Listener listener);

    void bindData(long sessionId, Sensor sensor, Session session, SessionState sessionState, ResourceHelper resourceHelper, int now, Boolean positionWithTitle, Boolean sessionReorderInProgress);
}
