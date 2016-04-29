package pl.llp.aircasting.tracking;

import com.google.common.eventbus.EventBus;
import pl.llp.aircasting.helper.MetadataHelper;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.*;
import pl.llp.aircasting.model.events.RealtimeMeasurementEvent;
import pl.llp.aircasting.storage.DatabaseTaskQueue;
import pl.llp.aircasting.storage.repository.SessionRepository;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class RealtimeSessionTracker extends ActualSessionTracker
{
  public static final long MEASUREMENTS_INTERVAL = 60000;

  private Map<String, Long> pendingMeasurements = newHashMap();

  RealtimeSessionTracker(EventBus eventBus, final Session session, DatabaseTaskQueue dbQueue, SettingsHelper settingsHelper, MetadataHelper metadataHelper, SessionRepository sessions, boolean locationLess)
  {
    super(eventBus, session, dbQueue, settingsHelper, metadataHelper, sessions, locationLess);
  }

  @Override
  public void addMeasurement(MeasurementStream stream, Measurement measurement)
  {
    long currentTime = System.currentTimeMillis();
    Long lastMeauserementTime = pendingMeasurements.get(stream.getSensorName());

    if(lastMeauserementTime == null || lastMeauserementTime < currentTime - MEASUREMENTS_INTERVAL) {
      pendingMeasurements.put(stream.getSensorName(), currentTime);

      measurementTracker.add(stream, measurement);
      RealtimeMeasurement realtimeMeasurement = new RealtimeMeasurement(this.session.getUUID(), stream, measurement);
      eventBus.post(new RealtimeMeasurementEvent(realtimeMeasurement));
    }
  }
}
