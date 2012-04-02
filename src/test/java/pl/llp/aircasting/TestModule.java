package pl.llp.aircasting;

import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.factory.SensorEventFactory;
import pl.llp.aircasting.factory.SensorFactory;
import pl.llp.aircasting.model.Sensor;
import roboguice.config.AbstractAndroidModule;

public class TestModule extends AbstractAndroidModule {
    @Override
    protected void configure() {
        bind(SensorEvent.class).toProvider(SensorEventFactory.class);
        bind(Sensor.class).toProvider(SensorFactory.class);
    }
}
