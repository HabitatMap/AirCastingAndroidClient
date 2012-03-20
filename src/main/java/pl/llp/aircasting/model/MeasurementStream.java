package pl.llp.aircasting.model;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class MeasurementStream {
    private List<Measurement> measurements = newArrayList();

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public void add(Measurement measurement) {
        measurements.add(measurement);
    }
}
