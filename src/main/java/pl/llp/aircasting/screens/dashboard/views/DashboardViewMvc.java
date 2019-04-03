package pl.llp.aircasting.screens.dashboard.views;

import android.view.View;

import java.util.List;
import java.util.Map;

import pl.llp.aircasting.model.Measurement;

public interface DashboardViewMvc {
    public static final int CURRENT_ITEM = 0;
    public static final int VIEWING_ITEM = 1;

    interface Listener {
        void onDashboardButtonClicked(View view);

        void onStreamClicked(View view);

        void onItemSwipe(int position, Map dataItem, boolean noStreamsLeft, int direction, int itemType);
    }

    View getRootView();

    void registerListener(Listener listener);

    void unregisterListener();

    void bindSensorData(List data);

    void bindRecentMeasurements(Map<String, Double> recentMeasurements);

    void bindRecentFixedMeasurements(Map<String, Measurement> recentFixedMeasurements);

    void bindChartData(Map liveCharts);

    void bindStaticChartData(Map staticCharts);

    void bindViewingSensorsData(List data);
}
