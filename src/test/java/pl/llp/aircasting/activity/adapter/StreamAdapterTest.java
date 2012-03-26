package pl.llp.aircasting.activity.adapter;

import android.app.Activity;
import com.google.common.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.helper.GaugeHelper;
import pl.llp.aircasting.model.SessionManager;

import java.util.ArrayList;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(InjectedTestRunner.class)
public class StreamAdapterTest {
    private StreamAdapter adapter;

    @Before
    public void setup() {
        adapter = new StreamAdapter(mock(Activity.class), new ArrayList<Map<String, Object>>(),
                mock(EventBus.class), mock(SessionManager.class), mock(GaugeHelper.class));
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
