package pl.llp.aircasting.screens.dashboard.views;

import android.view.View;

import java.util.Map;
import java.util.TreeMap;

import pl.llp.aircasting.screens.common.helpers.ResourceHelper;

public interface StreamItemViewMvc {
    public interface Listener {
        void onStreamClicked(View view);
    }

    View getRootView();

    void registerListener(Listener listener);

    void unregisterListener(Listener listener);

    void bindData(Map<String, Object> dataItem, int position, ResourceHelper resourceHelper);

    void bindNowValue(TreeMap<String, Double> nowValues);

    void bindChart(Map mChartData);
}
