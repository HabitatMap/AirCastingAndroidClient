package pl.llp.aircasting.sensor.hxm;

import pl.llp.aircasting.model.ExternalSensorDescriptor;
import pl.llp.aircasting.sensor.AbstractSensor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import com.google.common.eventbus.EventBus;

public class HXMHeartBeatMonitor extends AbstractSensor
{
  private BluetoothDevice device;
  HxMReaderWorker readerWorker;

  public HXMHeartBeatMonitor(ExternalSensorDescriptor descriptor, EventBus eventBus, BluetoothAdapter adapter)
  {
    super(descriptor, eventBus, adapter);
  }

  public synchronized void start()
  {
    if (device == null || addressChanged(descriptor.getAddress()))
    {
      device = adapter.getRemoteDevice(descriptor.getAddress());

      readerWorker = new HxMReaderWorker(adapter, device, eventBus);
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
    if (readerWorker != null)
    {
      readerWorker.stop();
      device = null;
    }
  }
}