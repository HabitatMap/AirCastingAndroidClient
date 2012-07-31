package pl.llp.aircasting.sensor;

import pl.llp.aircasting.model.ExternalSensorDescriptor;

import android.bluetooth.BluetoothAdapter;
import com.google.common.eventbus.EventBus;

/**
 * Created by ags on 23/07/12 at 19:02
 */
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
