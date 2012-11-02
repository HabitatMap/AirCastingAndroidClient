package pl.llp.aircasting.sensor;

import android.bluetooth.BluetoothAdapter;
import com.google.common.eventbus.EventBus;

public abstract class AbstractSensor
{
  protected ExternalSensorDescriptor descriptor;
  protected EventBus eventBus;
  protected final BluetoothAdapter adapter;

  public AbstractSensor(ExternalSensorDescriptor descriptor, EventBus eventBus, BluetoothAdapter adapter)
  {
    this.descriptor = descriptor;
    this.eventBus = eventBus;
    this.adapter = adapter;

    if(descriptor == null || eventBus == null || adapter == null)
    {
      throw new NullPointerException("Cannot have nulls!");
    }
  }

  public void start()
  {

  }

  public void stop()
  {
    eventBus.post(new SensorStoppedEvent(descriptor));
    customStop();
  }

  protected abstract void customStop();
}
