package pl.llp.aircasting.sensor.external;

import pl.llp.aircasting.event.network.ConnectionUnsuccessfulEvent;
import pl.llp.aircasting.screens.common.helpers.SettingsHelper;
import pl.llp.aircasting.sensor.common.AbstractSensor;
import pl.llp.aircasting.sensor.common.ExternalSensorDescriptor;
import pl.llp.aircasting.sensor.airbeam.AirbeamSensor;
import pl.llp.aircasting.sensor.bioharness.BioharnessSensor;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;
import pl.llp.aircasting.sensor.hxm.HXMHeartBeatMonitor;
import pl.llp.aircasting.sensor.ioio.IOIOFakeSensor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.internal.Nullable;

import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;

@Singleton
public class ExternalSensors {
    public static final String ZEPHYR_BIOHARNESS = "BH BHT";
    public static final String ZEPHYR_HEART_RATE_MONITOR = "HXM";
    public static final String IOIO_DISPLAY_STRIP = "IOIO";
    public static final String AIRBEAM = "airbeam";

    final Map<String, AbstractSensor> sensors = newHashMap();

    @Nullable
    @Inject BluetoothAdapter bluetoothAdapter;
    @Inject SettingsHelper settings;
    @Inject EventBus eventBus;

    @Inject
    public void init() {
        eventBus.register(this);
        Iterable<ExternalSensorDescriptor> descriptors = settings.knownSensors();

        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        for (ExternalSensorDescriptor descriptor : descriptors) {
            if (isPaired(descriptor, bondedDevices)) {
                if (sensors.containsKey(descriptor.getAddress())) {
                    continue;
                }
                AbstractSensor sensor = createExternalSensor(descriptor);
                sensors.put(descriptor.getAddress(), sensor);
            }
        }
    }

    private boolean isPaired(ExternalSensorDescriptor descriptor, Set<BluetoothDevice> bondedDevices) {
        for (BluetoothDevice device : bondedDevices) {
            if (descriptor.getAddress().equals(device.getAddress())) {
                return true;
            }
        }
        return false;
    }

    private AbstractSensor createExternalSensor(ExternalSensorDescriptor descriptor) {
        String sensorName = descriptor.getName();
        if (Strings.isNullOrEmpty(sensorName)) {
            return new ExternalSensor(descriptor, eventBus, bluetoothAdapter);
        }
        if (sensorName.toLowerCase().startsWith(AIRBEAM)) {
            return new AirbeamSensor(descriptor, eventBus, bluetoothAdapter);
        }
        if (sensorName.startsWith(ZEPHYR_BIOHARNESS)) {
            return new BioharnessSensor(descriptor, eventBus, bluetoothAdapter);
        }
        if (sensorName.startsWith(ZEPHYR_HEART_RATE_MONITOR)) {
            return new HXMHeartBeatMonitor(descriptor, eventBus, bluetoothAdapter);
        }
        if (sensorName.startsWith(IOIO_DISPLAY_STRIP)) {
            return new IOIOFakeSensor(descriptor, eventBus, bluetoothAdapter);
        }

        return new ExternalSensor(descriptor, eventBus, bluetoothAdapter);
    }

    public void start() {
        init();
        for (AbstractSensor sensor : sensors.values()) {
            sensor.start();
        }
    }

    public void disconnect(String address) {
        if (sensors.containsKey(address)) {
            sensors.remove(address).stop();
            settings.knownSensorsWithout(address);
        }
    }

    public void disconnectAllSensors() {
        Iterable<ExternalSensorDescriptor> descriptors = settings.knownSensors();

        for (ExternalSensorDescriptor descriptor : descriptors) {
            disconnect(descriptor.getAddress());
        }
    }

    public boolean knows(String address) {
        if (SimpleAudioReader.SENSOR_ADDRESS_BUILTIN.equals(address))
            return true;

        for (String knownAddress : sensors.keySet()) {
            if (knownAddress.equals(address))
                return true;
        }
        return false;
    }

    @Subscribe
    public void onEvent(ConnectionUnsuccessfulEvent event) {
        disconnect(event.getDevice().getAddress());
    }
}
