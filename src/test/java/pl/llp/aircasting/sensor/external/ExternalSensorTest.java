package pl.llp.aircasting.sensor.external;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.event.ExternalSensorEvent;

import java.io.IOException;

import static org.mockito.Mockito.*;

@RunWith(InjectedTestRunner.class)
public class ExternalSensorTest {
    @Inject ExternalSensor sensor;

    @Before
    public void setup() throws IOException {
        sensor.parser = mock(ExternalSensorParser.class);

        sensor.eventBus = mock(EventBus.class);
    }

    @Test
    public void shouldReadInputAndGenerateEvents() throws IOException, pl.llp.aircasting.sensor.external.ParseException {
        ExternalSensorEvent event1 = mock(ExternalSensorEvent.class);
        when(sensor.parser.parse("Reading 1")).thenReturn(event1);

        sensor.read("Reading 1");

        verify(sensor.eventBus).post(event1);
    }
}
