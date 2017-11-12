package pl.llp.aircasting.activity.adapter;

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.activity.DashboardBaseActivity;
import pl.llp.aircasting.helper.SessionDataFactory;
import pl.llp.aircasting.helper.SessionState;
import pl.llp.aircasting.helper.StreamViewHelper;
import pl.llp.aircasting.helper.DashboardChartManager;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.CurrentSessionSensorManager;
import pl.llp.aircasting.model.CurrentSessionManager;

import android.view.View;
import com.google.common.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.model.ViewingSessionsSensorManager;

import java.util.ArrayList;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(InjectedTestRunner.class)
public class StreamAdapterTest {
    private StreamAdapter adapter;
    private View view;
    private Sensor sensor;

    @Before
    public void setup() {
        adapter = new StreamAdapter(mock(DashboardBaseActivity.class), new ArrayList<Map<String, Object>>(),
                mock(EventBus.class),
                mock(StreamViewHelper.class),
                mock(CurrentSessionSensorManager.class),
                mock(ViewingSessionsSensorManager.class),
                mock(DashboardChartManager.class),
                mock(SessionState.class),
                mock(SessionDataFactory.class));

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
}
