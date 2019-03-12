package pl.llp.aircasting.screens.dashboard.views;

import android.arch.lifecycle.LiveData;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import pl.llp.aircasting.model.Measurement;

public interface DashboardViewMvc {
    public interface Listener {
        void onDashboardButtonClicked(View view);

        void onStreamClicked(View view);
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
