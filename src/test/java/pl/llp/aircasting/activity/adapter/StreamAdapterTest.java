package pl.llp.aircasting.activity.adapter;

import android.app.Activity;
import android.view.View;
import com.google.common.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.R;
import pl.llp.aircasting.event.ui.ToggleStreamEvent;
import pl.llp.aircasting.event.ui.ViewStreamEvent;
import pl.llp.aircasting.helper.GaugeHelper;
import pl.llp.aircasting.model.SensorManager;
import pl.llp.aircasting.model.SessionManager;

import java.util.ArrayList;
import java.util.Map;

import static org.mockito.Mockito.*;

@RunWith(InjectedTestRunner.class)
public class StreamAdapterTest {
    private StreamAdapter adapter;
    private View view;

    @Before
    public void setup() {
        adapter = new StreamAdapter(mock(Activity.class), new ArrayList<Map<String, Object>>(),
                mock(EventBus.class), mock(SessionManager.class), mock(GaugeHelper.class), mock(SensorManager.class));

        view = mock(View.class);
        when(view.getTag()).thenReturn("LHC");
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

    @Test
    public void shouldPostEventsOnSensorToggle() {
        when(view.getId()).thenReturn(R.id.record_stream);

        adapter.onClick(view);

        ToggleStreamEvent expected = new ToggleStreamEvent("LHC");
        verify(adapter.eventBus, only()).post(expected);
    }

    @Test
    public void shouldPostEventsOnSensorView() {
        when(view.getId()).thenReturn(R.id.view_stream);

        adapter.onClick(view);

        ViewStreamEvent expected = new ViewStreamEvent("LHC");
        verify(adapter.eventBus, only()).post(expected);
    }
}
