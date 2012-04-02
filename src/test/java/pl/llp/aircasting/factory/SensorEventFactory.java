package pl.llp.aircasting.factory;

import com.google.inject.Provider;
import pl.llp.aircasting.event.sensor.SensorEvent;

public class SensorEventFactory implements Provider<SensorEvent> {
    @Override
    public SensorEvent get() {
        return new SensorEvent("LHC", "Hadrons", "H", "number", "#", 0, 10, 20, 30, 40, 12);
    }
}
