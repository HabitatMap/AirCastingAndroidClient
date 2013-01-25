package pl.llp.aircasting.sensor.ioio;

import pl.llp.aircasting.sensor.ExternalSensorDescriptor;
import pl.llp.aircasting.sensor.AbstractSensor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import com.google.common.eventbus.EventBus;

/**
 * Created by ags on 24/07/12 at 18:43
 */
public class IOIOFakeSensor extends AbstractSensor
{
  public IOIOFakeSensor(ExternalSensorDescriptor descriptor, EventBus eventBus, BluetoothAdapter adapter)
  {
    super(descriptor, eventBus, adapter);
  }

  @Override
  protected void startWorking()
  {
    // do nothing
  }

  @Override
  protected void injectSocket(BluetoothSocket socket)
  {
    // do nothing
  }

  @Override
  protected void customStop()
  {
    // do nothing
  }
}
