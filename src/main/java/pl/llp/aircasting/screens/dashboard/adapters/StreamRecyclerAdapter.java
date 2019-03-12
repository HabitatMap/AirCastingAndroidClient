package pl.llp.aircasting.screens.dashboard.adapters;

import java.util.List;
import java.util.Map;

import pl.llp.aircasting.model.Measurement;

public interface StreamRecyclerAdapter {
    public static final String PAYLOAD_NOW_VALUES_UPDATE = "payload_now_values";
    public static final String PAYLOAD_CHARTS_UPDATE = "payload_charts";
    public static final String PAYLOAD_TITLE_POSITION_UPDATE = "payload_title";

    void bindData(List data);

    void bindNowValues(Map nowValue);

    void bindRecentFixedMeasurements(Map<String, Measurement> recentFixedMeasurements);

    void bindChartData(Map charts);
}
