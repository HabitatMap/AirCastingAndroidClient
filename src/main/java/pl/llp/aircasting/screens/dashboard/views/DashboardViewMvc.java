package pl.llp.aircasting.screens.dashboard.views;

import android.arch.lifecycle.LiveData;
import android.view.View;

import com.github.mikephil.charting.charts.LineChart;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public interface DashboardViewMvc {
    public interface Listener {
        void onDashboardButtonClicked(View view);

        void onStreamClicked(View view);
    }

    View getRootView();

    void registerListener(Listener listener);

    void unregisterListener();

    void bindSensorData(List data);

    void bindNowValues(TreeMap recentMeasurementsData);

    void bindChartData(Map liveCharts);

    void bindViewingSensorsData(List data);
}
