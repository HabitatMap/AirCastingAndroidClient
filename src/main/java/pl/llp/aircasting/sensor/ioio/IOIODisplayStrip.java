package pl.llp.aircasting.sensor.ioio;

import pl.llp.aircasting.model.ExternalSensorDescriptor;
import pl.llp.aircasting.sensor.AbstractSensor;

import android.bluetooth.BluetoothAdapter;
import com.google.common.eventbus.EventBus;

/**
 * Created by ags on 23/07/12 at 19:02
 */
public class IOIODisplayStrip extends AbstractSensor
{
  public IOIODisplayStrip(ExternalSensorDescriptor descriptor, EventBus eventBus, BluetoothAdapter bluetoothAdapter)
  {
    super(descriptor, eventBus, bluetoothAdapter);
  }
}
