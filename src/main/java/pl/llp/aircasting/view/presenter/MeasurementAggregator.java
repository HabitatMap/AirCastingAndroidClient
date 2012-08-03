package pl.llp.aircasting.view.presenter;

import pl.llp.aircasting.model.Measurement;

import java.util.Date;

/**
* Created by ags on 02/08/12 at 16:09
*/
class MeasurementAggregator
{
    private double longitude = 0;
    private double latitude = 0;
    private double value = 0;
    private long time = 0;
    private int count = 0;

    public void add(Measurement measurement) {
        longitude += measurement.getLongitude();
        latitude += measurement.getLatitude();
        value += measurement.getValue();
        time += measurement.getTime().getTime();
        count += 1;
    }

    public void reset() {
        longitude = latitude = value = time = count = 0;
    }

    public Measurement getAverage() {
        return new Measurement(latitude / count, longitude / count, value / count, new Date(time / count));
    }

    public boolean isComposite() {
        return count > 1;
    }

    public boolean isEmpty() {
        return count == 0;
    }
}
