package pl.llp.aircasting.screens.dashboard.views;

import android.view.View;

import java.util.Map;

import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.screens.common.helpers.ResourceHelper;
import pl.llp.aircasting.screens.common.sessionState.SessionState;

public interface StreamItemViewMvc {
    public interface Listener {
        void onStreamClicked(View view);
    }

    View getRootView();

    void registerListener(Listener listener);

    void unregisterListener(Listener listener);

    void bindData(Map<String, Object> dataItem, Boolean displayTitle, ResourceHelper resourceHelper);
}
