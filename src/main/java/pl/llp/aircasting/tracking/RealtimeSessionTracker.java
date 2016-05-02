package pl.llp.aircasting.tracking;

import com.google.common.eventbus.EventBus;
import pl.llp.aircasting.helper.MetadataHelper;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.*;
import pl.llp.aircasting.model.events.RealtimeMeasurementEvent;
import pl.llp.aircasting.storage.DatabaseTaskQueue;
import pl.llp.aircasting.storage.repository.SessionRepository;

import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Maps.newHashMap;

public class RealtimeSessionTracker extends ActualSessionTracker
{
  private Map<String, MeasurementsBuffer> pendingMeasurements = newHashMap();

  RealtimeSessionTracker(EventBus eventBus, final Session session, DatabaseTaskQueue dbQueue, SettingsHelper settingsHelper, MetadataHelper metadataHelper, SessionRepository sessions, boolean locationLess)
  {
    super(eventBus, session, dbQueue, settingsHelper, metadataHelper, sessions, locationLess);
  }

  @Override
  public void addMeasurement(MeasurementStream stream, Measurement measurement)
  {
    MeasurementsBuffer measurementsBuffer = getMeasurementBuffer(stream.getSensorName());
    measurementsBuffer.add(measurement.getValue());

    if(measurementsBuffer.isGettable()) {
      Double average = measurementsBuffer.get();

      measurement.setValue(average);
      measurement.setMeasuredValue(average);

      addActualMeasurement(stream, measurement);
    }
  }

  private MeasurementsBuffer getMeasurementBuffer(String sensorName) {
    if(!pendingMeasurements.containsKey(sensorName))
      pendingMeasurements.put(sensorName, new MeasurementsBuffer());

    return pendingMeasurements.get(sensorName);
  }

  private void addActualMeasurement(MeasurementStream stream, Measurement measurement) {
    measurementTracker.add(stream, measurement);
    streamMeasurement(this.session.getUUID(), stream, measurement);
  }

  private void streamMeasurement(UUID sessionUUID, MeasurementStream stream, Measurement measurement)
  {
    RealtimeMeasurement realtimeMeasurement = new RealtimeMeasurement(sessionUUID, stream, measurement);
    eventBus.post(new RealtimeMeasurementEvent(realtimeMeasurement));
  }
}
