package pl.llp.aircasting.sensor.external;

import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.ExternalSensorDescriptor;
import pl.llp.aircasting.model.SessionManager;
import pl.llp.aircasting.sensor.AbstractSensor;
import pl.llp.aircasting.sensor.hxm.HXMHeartBeatMonitor;
import pl.llp.aircasting.sensor.ioio.IOIOFakeSensor;

import android.bluetooth.BluetoothAdapter;
import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.internal.Nullable;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

@Singleton
public class ExternalSensors
{
  public static final String ZEPHYR_HEART_RATE_MONITOR = "HXM";
  public static final String IOIO_DISPLAY_STRIP = "IOIO";

  @Nullable @Inject BluetoothAdapter bluetoothAdapter;
  @Inject SettingsHelper settings;
  @Inject EventBus eventBus;
  @Inject SessionManager sessionManager;

  Map<String, AbstractSensor> sensors = newHashMap();

  @Inject
  public void init()
  {
    Iterable<ExternalSensorDescriptor> descriptors = settings.knownSensors();
    for (ExternalSensorDescriptor descriptor : descriptors)
    {
      if(sensors.containsKey(descriptor.getAddress()))
      {

      }
      else
      {
        AbstractSensor sensor = createExternalSensor(descriptor);
        sensors.put(descriptor.getAddress(), sensor);
      }
    }
  }

  private AbstractSensor createExternalSensor(ExternalSensorDescriptor descriptor)
  {
    String sensorName = descriptor.getName();
    if(Strings.isNullOrEmpty(sensorName))
    {
      return new ExternalSensor(descriptor, eventBus, bluetoothAdapter);
    }
    if(sensorName.startsWith(ZEPHYR_HEART_RATE_MONITOR))
    {
      return new HXMHeartBeatMonitor(descriptor, eventBus, bluetoothAdapter);
    }
    if(sensorName.startsWith(IOIO_DISPLAY_STRIP))
    {
      return new IOIOFakeSensor(descriptor, eventBus, bluetoothAdapter);
    }

    return new ExternalSensor(descriptor, eventBus, bluetoothAdapter);
  }

  public void start()
  {
    init();
    for (AbstractSensor sensor : sensors.values())
    {
      sensor.start();
    }
  }

  public void disconnect(String address)
  {
    if(sensors.containsKey(address))
    {
      sensors.get(address).stop();
    }
  }
}
