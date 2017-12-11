package pl.llp.aircasting.tracking;

import com.google.common.eventbus.EventBus;
import pl.llp.aircasting.helper.MetadataHelper;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.*;
import pl.llp.aircasting.model.events.MeasurementEvent;
import pl.llp.aircasting.model.events.FixedSessionsMeasurementEvent;
import pl.llp.aircasting.storage.DatabaseTaskQueue;
import pl.llp.aircasting.storage.repository.SessionRepository;
import pl.llp.aircasting.sync.FixedSessionUploader;

import java.util.Map;
import java.util.UUID;

import static com.google.common.collect.Maps.newHashMap;

public class FixedSessionTracker extends ActualSessionTracker {
    private final FixedSessionUploader fixedSessionUploader;

    private Map<String, MeasurementsBuffer> pendingMeasurements = newHashMap();

    FixedSessionTracker(EventBus eventBus, final Session session, DatabaseTaskQueue dbQueue, SettingsHelper settingsHelper, MetadataHelper metadataHelper, SessionRepository sessions, FixedSessionUploader fixedSessionUploader, boolean locationLess) {
        super(eventBus, session, dbQueue, settingsHelper, metadataHelper, sessions, locationLess);
        session.setEnd(session.getStart());
        this.fixedSessionUploader = fixedSessionUploader;
    }

    @Override
    public void addMeasurement(Sensor sensor, MeasurementStream stream, Measurement measurement) {
        MeasurementsBuffer measurementsBuffer = getMeasurementBuffer(stream.getSensorName());
        measurementsBuffer.add(measurement.getValue());

        if (measurementsBuffer.isGettable()) {
            Double average = measurementsBuffer.get();

            measurement.setValue(average);
            measurement.setMeasuredValue(average);

            addActualMeasurement(sensor, stream, measurement);
        }
    }

    @Override
    protected boolean beforeSave(final Session session) {
        if (fixedSessionUploader.create(session))
            return true;
        else
            return false;
    }

    private MeasurementsBuffer getMeasurementBuffer(String sensorName) {
        if (!pendingMeasurements.containsKey(sensorName))
            pendingMeasurements.put(sensorName, new MeasurementsBuffer());

        return pendingMeasurements.get(sensorName);
    }

    private void addActualMeasurement(Sensor sensor, MeasurementStream stream, Measurement measurement) {
        measurementTracker.add(stream, measurement);
        recentMeasurements.put(stream.getSensorName(), measurement.getValue());
        eventBus.post(new MeasurementEvent(measurement, sensor));

        streamMeasurement(this.session.getUUID(), stream, measurement);
    }

    private void streamMeasurement(UUID sessionUUID, MeasurementStream stream, Measurement measurement) {
        FixedSessionsMeasurement fixedSessionsMeasurement = new FixedSessionsMeasurement(sessionUUID, stream, measurement);
        eventBus.post(new FixedSessionsMeasurementEvent(fixedSessionsMeasurement));
    }
}
