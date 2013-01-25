package pl.llp.aircasting.sensor;

import pl.llp.aircasting.event.ConnectionUnsuccesfulEvent;
import pl.llp.aircasting.util.Constants;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import com.google.common.eventbus.EventBus;

import java.util.concurrent.atomic.AtomicBoolean;

public class ReaderWorker extends Worker
{
  final BluetoothAdapter adapter;
  final BluetoothDevice device;
  final BluetoothSocketReader reader;

  AtomicBoolean active = new AtomicBoolean(false);

  final Thread thread;

  public ReaderWorker(BluetoothAdapter adapter,
                      BluetoothDevice device,
                      EventBus eventBus,
                      BluetoothSocketReader reader)
  {
    super(eventBus);
    this.adapter = adapter;
    this.device = device;
    this.reader = reader;
    this.thread = new Thread(new Runnable()
        {
          @Override
          public void run()
          {
            while (active.get())
            {
              read();
            }
          }
    }, String.format("Reader [%s, %s]", device.getName(), device.getAddress()));
    reader.setEventBus(eventBus);
  }

  public void customStart()
  {
    active.set(true);
    thread.start();
  }

  private void read()
  {
    try
    {
      reader.read();
    }
    catch (Exception e)
    {
      considerStoppingOnFailure();
      status = Status.CONNECTION_INTERRUPTED;
      Log.w(Constants.TAG, "Bluetooth communication failure - mostly likely end of stream", e);
    }
  }

  public void customStop()
  {
    active.set(false);
    if(thread != null)
    {
      thread.interrupt();
    }
  }

  @Override
  public void handlePersistentFailure()
  {
    eventBus.post(new ConnectionUnsuccesfulEvent(device));
  }

  @Override
  public String toString()
  {
    return "ReaderWorker{" +
        "device=" + device +
        ", status=" + status +
        '}';
  }
}