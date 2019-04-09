package pl.llp.aircasting.screens.dashboard.events;

public class NewChartAveragesEvent {
    private String chartType;

    public NewChartAveragesEvent(String chartType) {
        this.chartType = chartType;
    }

    public String getChartType() {
        return chartType;
    }
}
