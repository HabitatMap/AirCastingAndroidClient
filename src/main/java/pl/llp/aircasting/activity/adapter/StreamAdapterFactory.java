package pl.llp.aircasting.activity.adapter;

import pl.llp.aircasting.activity.ButtonsActivity;
import pl.llp.aircasting.helper.GaugeHelper;
import pl.llp.aircasting.helper.TopBarHelper;
import pl.llp.aircasting.model.SensorManager;
import pl.llp.aircasting.model.SessionManager;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.Map;

@Singleton
public class StreamAdapterFactory
{
  @Inject EventBus eventBus;
  @Inject GaugeHelper gaugeHelper;
  @Inject SessionManager sessionManager;
  @Inject SensorManager sensorManager;

  public StreamAdapter getAdapter(ButtonsActivity context) {
    return new StreamAdapter(context, new ArrayList<Map<String, Object>>(), eventBus,
                             gaugeHelper, sensorManager, sessionManager);
  }
}
