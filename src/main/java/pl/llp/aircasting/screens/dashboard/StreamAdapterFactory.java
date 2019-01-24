package pl.llp.aircasting.screens.dashboard;

import pl.llp.aircasting.screens.common.ApplicationState;
import pl.llp.aircasting.screens.common.sessionState.SessionDataFactory;
import pl.llp.aircasting.screens.common.sessionState.SessionState;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionSensorManager;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.screens.common.sessionState.ViewingSessionsSensorManager;

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
  @Inject ApplicationState applicationState;

  public StreamAdapter getAdapter(DashboardBaseActivity context) {
    return new StreamAdapter(context,
                             new ArrayList<Map<String, Object>>(),
                             eventBus,
                             streamViewHelper,
                             currentSessionSensorManager,
                             viewingSessionsSensorManager,
                             dashboardChartManager,
                             sessionState,
                             sessionData,
                             applicationState);
  }
}
