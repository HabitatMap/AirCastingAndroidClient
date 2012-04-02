package pl.llp.aircasting.model;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.event.ui.ViewStreamEvent;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.mock;

@RunWith(InjectedTestRunner.class)
public class SensorManagerTest {
    @Inject SensorManager manager;
    private Sensor sensor;
    private SensorEvent event;

    @Before
    public void setup() {
        manager.eventBus = mock(EventBus.class);

        event = new SensorEvent("LHC", "Hadrons", "H", "number", "#", 0, 10, 20, 30, 40, 12);
        sensor = new Sensor(event);

        manager.onEvent(event);
    }

    @Test
    public void shouldStoreSensorInformation() {
        assertThat(manager.getSensors(), hasItem(sensor));
    }

    @Test
    public void shouldNotOverwriteSensors() {
        manager.onEvent(new SensorEvent("LHC", "Muons", "M", "number", "#", 0, 10, 20, 30, 40, 12));
        sensor = manager.getSensor("LHC");

        assertThat(sensor.getMeasurementType(), equalTo("Hadrons"));
    }

    @Test
    public void shouldReturnSensorsByName() {
        assertThat(manager.getSensor(sensor.getSensorName()), equalTo(sensor));
    }

    @Test
    public void shouldStoreInformationAboutCurrentVisibleSensor() {
        manager.onEvent(new ViewStreamEvent(sensor));

        assertThat(manager.getVisibleSensor(), equalTo(sensor));
    }
}
