package pl.llp.aircasting.sensor.external;

import pl.llp.aircasting.model.ExternalSensorDescriptor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import com.google.common.eventbus.EventBus;

public class ExternalSensor
{
  EventBus eventBus;
  BluetoothAdapter bluetooth;

  private BluetoothDevice device;
  ReaderWorker readerWorker;
  private ExternalSensorDescriptor descriptor;

  public ExternalSensor(ExternalSensorDescriptor descriptor, EventBus eventBus, BluetoothAdapter adapter)
  {
    this.descriptor = descriptor;
    this.eventBus = eventBus;
    this.bluetooth = adapter;

    if(descriptor == null || eventBus == null || adapter == null)
    {
      throw new NullPointerException("Cannot have nulls!");
    }
  }

  public synchronized void start()
  {
    if (device == null || addressChanged(descriptor.getAddress()))
    {
      device = bluetooth.getRemoteDevice(descriptor.getAddress());

      readerWorker = new ReaderWorker(bluetooth, device, eventBus);
      readerWorker.start();
    }
  }

  private boolean addressChanged(String address)
  {
    return !device.getAddress().equals(address);
  }

  void stop()
  {
    if(readerWorker != null)
    {
      readerWorker.stop();
    }
  }

  public String getName()
  {
    return descriptor.getName();
  }
}

