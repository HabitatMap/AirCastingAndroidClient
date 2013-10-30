package pl.llp.aircasting.activity.adapter;

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.ButtonsActivity;
import pl.llp.aircasting.event.ui.ViewStreamEvent;
import pl.llp.aircasting.helper.GaugeHelper;
import pl.llp.aircasting.helper.StreamViewHelper;
import pl.llp.aircasting.helper.TopBarHelper;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.SensorManager;
import pl.llp.aircasting.model.SessionManager;

import android.view.View;
import com.google.common.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(InjectedTestRunner.class)
public class StreamAdapterTest {
    private StreamAdapter adapter;
    private View view;
    private Sensor sensor;

    @Before
    public void setup() {
        adapter = new StreamAdapter(mock(ButtonsActivity.class), new ArrayList<Map<String, Object>>(),
                mock(EventBus.class), mock(StreamViewHelper.class), mock(SensorManager.class), mock(SessionManager.class));

        sensor = mock(Sensor.class);

        view = mock(View.class);
        when(view.getTag()).thenReturn(sensor);
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
    public void shouldSuppressNextTapWhenAButtonIsClicked() {
        adapter.onClick(view);

        verify(adapter.context).suppressNextTap();
    }
}
