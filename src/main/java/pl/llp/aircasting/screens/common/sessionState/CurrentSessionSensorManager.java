package pl.llp.aircasting.screens.common.sessionState;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.location.Location;
import android.util.Log;

import pl.llp.aircasting.event.measurements.MeasurementEvent;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.screens.common.ApplicationState;
import pl.llp.aircasting.event.sensor.SessionSensorsLoadedEvent;
import pl.llp.aircasting.event.network.ConnectionUnsuccessfulEvent;
import pl.llp.aircasting.model.internal.SensorName;
import pl.llp.aircasting.sensor.common.ExternalSensorDescriptor;
import pl.llp.aircasting.event.sensor.SensorConnectedEvent;
import pl.llp.aircasting.event.sensor.SensorStoppedEvent;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;
import pl.llp.aircasting.sensor.external.ExternalSensors;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.util.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newConcurrentMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.inject.internal.Maps.newHashMap;

@Singleton
public class CurrentSessionSensorManager {
    @Inject ExternalSensors externalSensors;
    @Inject CurrentSessionManager currentSessionManager;
    @Inject EventBus eventBus;
    @Inject
    ApplicationState state;
    @Inject SimpleAudioReader audioReader;

    final Sensor AUDIO_SENSOR = SimpleAudioReader.getSensor();

    private volatile Map<SensorName, Sensor> currentSessionSensors = newConcurrentMap();
    private volatile Set<Sensor> disabled = newHashSet();
    private Map<String, Double> recentMeasurements = newHashMap();

    @Inject
    public void init() {
        eventBus.register(this);
    }

    @Subscribe
    public void onEvent(SensorEvent event) {
        String sensorName = event.getSensorName();

        if (!sensorKnown(sensorName)) {
            connectNewSensor(event);
        }

        double value = event.getValue();
        recentMeasurements.put(sensorName, value);

//        Location location = currentSessionManager.getLocation();
//
//        if (location != null && sensor != null && sensor.isEnabled()) {
//            double latitude = location.getLatitude();
//            double longitude = location.getLongitude();
//            Measurement measurement = new Measurement(latitude, longitude, value, event.getMeasuredValue(), event.getDate());
//
//            if (state.recording().isRecording()) {
//                MeasurementStream stream = currentSessionManager.prepareStream(event);
//                tracker.addMeasurement(sensor, stream, measurement);
//            } else {
//                eventBus.post(new MeasurementEvent(measurement, sensor));
//            }
//        }
    }

    private boolean sensorKnown(String sensorName) {
        return currentSessionSensors.containsKey(SensorName.from(sensorName));
    }

    private void connectNewSensor(SensorEvent event) {
        if (externalSensors.knows(event.getAddress())) {
            Sensor sensor = new Sensor(event, Constants.CURRENT_SESSION_FAKE_ID);
            if (disabled.contains(sensor)) {
                sensor.toggle();
            }
            SensorName name = SensorName.from(sensor.getSensorName());

            if (!currentSessionSensors.containsKey(name)) {
                currentSessionSensors.put(name, sensor);
                eventBus.post(new SensorConnectedEvent());
                eventBus.post(new SessionSensorsLoadedEvent(Constants.CURRENT_SESSION_FAKE_ID));
            }
        }
    }

    @Subscribe
    public void onEvent(SensorStoppedEvent event) {
        disconnectSensors(event.getDescriptor());
    }

    public synchronized double getNow(Sensor sensor) {
//        if (state.recording().isRecording()) {
//            return tracker.getNow(sensor);
//        } else {
            if (!recentMeasurements.containsKey(sensor.getSensorName())) {
                return 0;
            }

            return recentMeasurements.get(sensor.getSensorName());
//        }
    }

    public LiveData<Map<SensorName, Sensor>> getCurrentSensorsData() {
        final MutableLiveData<Map<SensorName, Sensor>> data = new MutableLiveData<>();
        data.postValue(currentSessionSensors);
        return data;
    }

    public void startSensors() {
        if (!state.sensors().started()) {
            externalSensors.start();
            state.sensors().start();
        }
    }

    public void startAudioSensor() {
        audioReader.start();
        state.microphoneState().start();
    }

    public void stopAudioSensor() {
        audioReader.stop();
        disconnectPhoneMicrophone();
        state.microphoneState().stop();
    }

    public void stopSensors() {
        if (state.recording().isRecording()) {
            return;
        }

        state.sensors().stop();
    }

    public void restartSensors() {
        externalSensors.start();
    }

    public List<Sensor> getSensorsList() {
        ArrayList<Sensor> result = newArrayList();
        result.addAll(currentSessionSensors.values());
        return result;
    }

    public Map<SensorName, Sensor> getSensorsMap() {
        return currentSessionSensors;
    }

    public Sensor getSensorByName(String name) {
        SensorName sensorName = SensorName.from(name);
        Sensor sensor = currentSessionSensors.get(sensorName);
        return sensor;
    }

    public boolean anySensorConnected() {
        return !getSensorsMap().isEmpty();
    }

    public void deleteSensorFromCurrentSession(Sensor sensor) {
        String sensorName = sensor.getSensorName();
        currentSessionSensors.remove(SensorName.from(sensorName));
    }

    @Subscribe
    public void onEvent(ConnectionUnsuccessfulEvent e) {
        disconnectSensors(new ExternalSensorDescriptor(e.getDevice()));
    }

    public void disconnectPhoneMicrophone() {
        currentSessionSensors.remove(SensorName.from("Phone Microphone"));
        eventBus.post(new SessionSensorsLoadedEvent(Constants.CURRENT_SESSION_FAKE_ID));
        eventBus.post(new SensorConnectedEvent());
    }

    public void disconnectSensors(ExternalSensorDescriptor descriptor) {
        String address = descriptor.getAddress();
        Collection<MeasurementStream> streams = currentSessionManager.getMeasurementStreams();

        for (MeasurementStream stream : streams) {
            if (address.equals(stream.getAddress())) {
                stream.markAs(MeasurementStream.Visibility.INVISIBLE_DISCONNECTED);
            }
        }

        Set<SensorName> newSensorNames = newHashSet();
        for (Map.Entry<SensorName, Sensor> entry : currentSessionSensors.entrySet()) {
            if (!address.equals(entry.getValue().getAddress())) {
                newSensorNames.add(entry.getKey());
            }
        }

        Map<SensorName, Sensor> newSensors = newConcurrentMap();
        for (SensorName sensorName : newSensorNames) {
            Sensor sensor = currentSessionSensors.get(sensorName);
            newSensors.put(sensorName, sensor);
        }

        currentSessionSensors = newSensors;
    }
}
