package pl.llp.aircasting.screens.dashboard.views;

import android.view.View;

import java.util.Map;
import java.util.TreeMap;

import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.screens.common.helpers.ResourceHelper;

public interface StreamItemViewMvc {
    void bindSessionTitle(int position);

    interface Listener {
        void onStreamClicked(View view);

        void onSessionUpClicked(long sessionId);

        void onSessionDownClicked(long sessionId);
    }

    View getRootView();

    void registerListener(Listener listener);

    void unregisterListener(Listener listener);

    void bindData(Map<String, Object> dataItem, int position, ResourceHelper resourceHelper);

    void bindNowValue(Map<String, Double> nowValues);

    void bindRecentFixedMeasurement(Map<String, Measurement> recentFixedMeasurements);

    void bindChart(Map mChartData);
}
