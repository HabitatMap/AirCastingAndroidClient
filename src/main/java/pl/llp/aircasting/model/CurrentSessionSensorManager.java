package pl.llp.aircasting.model;

import android.content.Context;
import android.widget.Toast;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.events.SessionSensorsLoadedEvent;
import pl.llp.aircasting.activity.events.SessionStartedEvent;
import pl.llp.aircasting.event.ConnectionUnsuccessfulEvent;
import pl.llp.aircasting.helper.VisibleSession;
import pl.llp.aircasting.helper.ResourceHelper;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.events.MeasurementLevelEvent;
import pl.llp.aircasting.model.events.SensorEvent;
import pl.llp.aircasting.model.internal.MeasurementLevel;
import pl.llp.aircasting.model.internal.SensorName;
import pl.llp.aircasting.sensor.ExternalSensorDescriptor;
import pl.llp.aircasting.activity.events.SensorConnectedEvent;
import pl.llp.aircasting.activity.events.SensorStoppedEvent;
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

@Singleton
public class CurrentSessionSensorManager {
    @Inject ResourceHelper resourceHelper;
    @Inject ExternalSensors externalSensors;
    @Inject CurrentSessionManager currentSessionManager;
    @Inject EventBus eventBus;
    @Inject SettingsHelper settingsHelper;
    @Inject Context context;
    @Inject VisibleSession visibleSession;

    final Sensor AUDIO_SENSOR = SimpleAudioReader.getSensor();

    private volatile Map<SensorName, Sensor> currentSessionSensors = newConcurrentMap();
    private volatile Set<Sensor> disabled = newHashSet();

    @Inject
    public void init() {
        eventBus.register(this);
    }

    @Subscribe
    public void onEvent(SensorEvent event) {
        // IOIO
        Sensor currentSensor = visibleSession.getSensor();
        if (visibleSession.getSession() != null && currentSensor.matches(getSensorByName(event.getSensorName()))) {
            double now = (int) currentSessionManager.getNow(currentSensor);
            MeasurementLevel level = resourceHelper.getLevel(currentSensor, now);
            eventBus.post(new MeasurementLevelEvent(currentSensor, level));
        }
        // end of IOIO

        if (currentSessionSensors.containsKey(SensorName.from(event.getSensorName()))) {
            return;
        }

        if (externalSensors.knows(event.getAddress())) {
            Sensor sensor = new Sensor(event);
            if (disabled.contains(sensor)) {
                sensor.toggle();
            }
            SensorName name = SensorName.from(sensor.getSensorName());

            if (!currentSessionSensors.containsKey(name)) {
                currentSessionSensors.put(name, sensor);
                eventBus.post(new SensorConnectedEvent());
            }
        }
    }

    @Subscribe
    public void onEvent(SensorStoppedEvent event) {
        disconnectSensors(event.getDescriptor());
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

    /**
     * @param sensor toggle enabled/disabled status of this Sensor
     */
    public void toggleSensor(Sensor sensor) {
        String name = sensor.getSensorName();
        Sensor actualSensor = currentSessionSensors.get(SensorName.from(name));

        if (isSensorToggleable(actualSensor)) {
            actualSensor.toggle();
        } else {
            Toast.makeText(context, R.string.sensor_is_disabled_in_settings, Toast.LENGTH_SHORT).show();
        }
    }

    public void setSensorsStatusesToDefaultsFromSettings() {
        if (getSensorByName("Phone Microphone") != null) {
            if (settingsHelper.isSoundLevelMeasurementsDisabled()) {
                currentSessionSensors.get(SensorName.from("Phone Microphone")).disable();
            } else {
                currentSessionSensors.get(SensorName.from("Phone Microphone")).enable();
            }
        }
    }

    public boolean isSensorToggleable(Sensor sensor) {
        if (sensor.getSensorName() == "Phone Microphone" && settingsHelper.isSoundLevelMeasurementsDisabled()) {
            return false;
        } else {
            return true;
        }
    }

    @Subscribe
    public void onEvent(SessionStartedEvent event) {
        setSensorsStatusesToDefaultsFromSettings();
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
        String sensorName = visibleSession.getSensor().getSensorName();

        if (!currentSessionSensors.containsKey(SensorName.from(sensorName))) {
            visibleSession.setSensor(AUDIO_SENSOR.getSensorName());
        }
    }
}
