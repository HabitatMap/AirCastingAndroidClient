package pl.llp.aircasting.factory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.model.Sensor;

public class SensorFactory implements Provider<Sensor> {
    @Inject SensorEvent event;

    @Override
    public Sensor get() {
        return new Sensor(event);
    }
}
