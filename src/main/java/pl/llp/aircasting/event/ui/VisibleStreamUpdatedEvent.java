package pl.llp.aircasting.event.ui;

import pl.llp.aircasting.model.Sensor;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 14/11/13
 * Time: 12:30
 * To change this template use File | Settings | File Templates.
 */
public class VisibleStreamUpdatedEvent {
    private Sensor sensor;

    public VisibleStreamUpdatedEvent(Sensor sensor) {
        this.sensor = sensor;
    }

    public Sensor getSensor() {
        return sensor;
    }
}
