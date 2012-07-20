package pl.llp.aircasting.sensor.external;

import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.ExternalSensorDescriptor;

import android.bluetooth.BluetoothAdapter;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.internal.Nullable;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

@Singleton
public class ExternalSensors
{
  @Nullable @Inject BluetoothAdapter bluetoothAdapter;
  @Inject SettingsHelper settings;
  @Inject EventBus eventBus;

  Map<String, ExternalSensor> sensors = newHashMap();

  @Inject
  public void init()
  {
    Iterable<ExternalSensorDescriptor> descriptors = settings.sensorsFromSettings();
    for (ExternalSensorDescriptor descriptor : descriptors)
    {
      if(sensors.containsKey(descriptor.getAddress()))
      {

      }
      else
      {
        sensors.put(descriptor.getAddress(), new ExternalSensor(descriptor, eventBus, bluetoothAdapter));
      }
    }
  }

  public void start()
  {
    init();
    for (ExternalSensor sensor : sensors.values())
    {
      if (!sensor.getName().startsWith("IOIO"))
      {
        sensor.start();
      }
    }
  }

  public void disconnect(String address)
  {
    if(sensors.containsKey(address))
      sensors.get(address).stop();
  }
}
