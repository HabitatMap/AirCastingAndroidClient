package pl.llp.aircasting.activity.adapter;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.event.sensor.SensorEvent;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(InjectedTestRunner.class)
public class StreamAdapterTest {
    @Inject StreamAdapter adapter;

    @Before
    public void setup() {
        adapter.eventBus = mock(EventBus.class);
    }

    @Test
    public void shouldRegisterForEventsOnStart() {
        adapter.start();

        verify(adapter.eventBus).register(adapter);
    }

    @Test
    public void shouldUnregisterFromEventsOnStop() {
        adapter.stop();

        verify(adapter.eventBus).unregister(adapter);
    }
}
