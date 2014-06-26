package pl.llp.aircasting.sensor;

import pl.llp.aircasting.android.Logger;
import pl.llp.aircasting.event.ConnectionUnsuccessfulEvent;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import com.google.common.eventbus.EventBus;

import java.io.IOException;
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
    reader.setBus(eventBus);
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
    catch (IOException e)
    {
      handlePersistentFailure();
      stop();
      status = Status.CONNECTION_INTERRUPTED;
      Logger.w("Bluetooth communication failure - most likely end of stream", e);
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
    eventBus.post(new ConnectionUnsuccessfulEvent(device));
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