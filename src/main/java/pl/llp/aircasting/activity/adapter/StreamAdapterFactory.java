package pl.llp.aircasting.activity.adapter;

import android.view.View;
import pl.llp.aircasting.activity.DashboardBaseActivity;
import pl.llp.aircasting.helper.DashboardChartManager;
import pl.llp.aircasting.helper.SessionDataFactory;
import pl.llp.aircasting.helper.SessionState;
import pl.llp.aircasting.helper.StreamViewHelper;
import pl.llp.aircasting.model.CurrentSessionSensorManager;
import pl.llp.aircasting.model.CurrentSessionManager;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.model.ViewingSessionsSensorManager;

import java.util.ArrayList;
import java.util.Map;

@Singleton
public class StreamAdapterFactory
{
  @Inject EventBus eventBus;
  @Inject StreamViewHelper streamViewHelper;
  @Inject CurrentSessionSensorManager currentSessionSensorManager;
  @Inject ViewingSessionsSensorManager viewingSessionsSensorManager;
  @Inject DashboardChartManager dashboardChartManager;
  @Inject SessionState sessionState;
  @Inject SessionDataFactory sessionData;

  public StreamAdapter getAdapter(DashboardBaseActivity context) {
    return new StreamAdapter(context,
                             new ArrayList<Map<String, Object>>(),
                             eventBus,
                             streamViewHelper,
                             currentSessionSensorManager,
                             viewingSessionsSensorManager,
                             dashboardChartManager,
                             sessionState,
                             sessionData);
  }
}
