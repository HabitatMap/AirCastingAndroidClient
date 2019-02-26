package pl.llp.aircasting.screens.dashboard.adapters;

import java.util.List;
import java.util.Map;

public interface StreamRecyclerAdapter {
    void bindData(List data);

    void bindNowValues(Map nowValue);

    void bindChartData(Map charts);
}
