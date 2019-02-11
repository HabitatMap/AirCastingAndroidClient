package pl.llp.aircasting.screens.dashboard.views;

import android.view.View;

import java.util.List;

public interface DashboardViewMvc {
    public interface Listener {
        void onDashboardButtonClicked(View view);

        void onStreamClicked(View view);
    }

    View getRootView();

    void registerListener(Listener listener);

    void unregisterListener();

    void bindSensorData(List data);
}
