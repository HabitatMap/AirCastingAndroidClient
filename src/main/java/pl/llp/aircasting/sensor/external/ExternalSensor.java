package pl.llp.aircasting.sensor.external;

import pl.llp.aircasting.model.ExternalSensorDescriptor;
import pl.llp.aircasting.sensor.AbstractSensor;
import pl.llp.aircasting.sensor.ReaderWorker;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import com.google.common.eventbus.EventBus;

public class ExternalSensor extends AbstractSensor
{
  private BluetoothDevice device;
  ReaderWorker readerWorker;

  public ExternalSensor(ExternalSensorDescriptor descriptor, EventBus eventBus, BluetoothAdapter adapter)
  {
    super(descriptor, eventBus, adapter);
  }

  public synchronized void start()
  {
    if (device == null || addressChanged(descriptor.getAddress()))
    {
      device = adapter.getRemoteDevice(descriptor.getAddress());

      readerWorker = new ReaderWorker(adapter, device, eventBus, new LineDataReader());
      readerWorker.start();
    }
  }

  private boolean addressChanged(String address)
  {
    return !device.getAddress().equals(address);
  }

  @Override
  protected void customStop()
  {
    if(readerWorker != null)
    {
      readerWorker.stop();
      device = null;
    }
  }
}

