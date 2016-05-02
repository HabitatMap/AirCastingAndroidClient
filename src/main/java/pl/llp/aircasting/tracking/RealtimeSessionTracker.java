package pl.llp.aircasting.tracking;

import com.google.common.eventbus.EventBus;
import pl.llp.aircasting.helper.MetadataHelper;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.*;
import pl.llp.aircasting.model.events.RealtimeMeasurementEvent;
import pl.llp.aircasting.storage.DatabaseTaskQueue;
import pl.llp.aircasting.storage.repository.SessionRepository;
import pl.llp.aircasting.sync.RealtimeSessionUploader;

import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Maps.newHashMap;

public class RealtimeSessionTracker extends ActualSessionTracker
{
  private final RealtimeSessionUploader realtimeSessionUploader;

  private Map<String, MeasurementsBuffer> pendingMeasurements = newHashMap();

  RealtimeSessionTracker(EventBus eventBus, final Session session, DatabaseTaskQueue dbQueue, SettingsHelper settingsHelper, MetadataHelper metadataHelper, SessionRepository sessions, RealtimeSessionUploader realtimeSessionUploader, boolean locationLess)
  {
    super(eventBus, session, dbQueue, settingsHelper, metadataHelper, sessions, locationLess);
    this.realtimeSessionUploader = realtimeSessionUploader;
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

  @Override
  protected boolean beforeSave(final Session session)
  {
    if (realtimeSessionUploader.create(session))
      return true;
    else
      return false;
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
