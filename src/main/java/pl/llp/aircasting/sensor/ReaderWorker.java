package pl.llp.aircasting.sensor;

import pl.llp.aircasting.activity.extsens.BluetoothConnector;
import pl.llp.aircasting.event.ConnectionUnsuccesfulEvent;
import pl.llp.aircasting.util.Constants;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import com.google.common.eventbus.EventBus;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.io.Closeables.closeQuietly;

public class ReaderWorker
{
  private static final long MAX_CONNECTION_FAILURE_TIME = Constants.ONE_MINUTE;

  BluetoothAdapter adapter;
  BluetoothDevice device;
  EventBus eventBus;
  BluetoothSocketReader reader;

  AtomicBoolean active = new AtomicBoolean(false);
  volatile BluetoothSocket socket;

  Status status = Status.NOT_YET_STARTED;

  private Thread thread;
  private volatile long connectionFailingSince = -1;

  public ReaderWorker(BluetoothAdapter adapter,
                      BluetoothDevice device,
                      EventBus eventBus,
                      BluetoothSocketReader reader)
  {
    this.adapter = adapter;
    this.device = device;
    this.eventBus = eventBus;
    this.reader = reader;
    reader.setEventBus(eventBus);
  }

  public void start()
  {
    active.set(true);
    status = Status.STARTED;
    thread = new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        while (active.get())
        {
          try
          {
            socket = connect();
            if (socket != null)
            {
              read(socket);
            }
          }
          catch (InterruptedException e)
          {
            considerStoppingOnFailure();
            status = Status.DID_NOT_CONNECT;
            Log.e(Constants.TAG, "Failed to establish adapter connection", e);
            return;
          }
        }
      }
    }, String.format("Reader [%s, %s]", device.getName(), device.getAddress()));
    thread.start();
  }

  private void read(BluetoothSocket socket)
  {
    try
    {
      reader.read(socket);
    }
    catch (IOException e)
    {
      considerStoppingOnFailure();
      status = Status.CONNECTION_INTERRUPTED;
      Log.w(Constants.TAG, "Bluetooth communication failure - mostly likely end of stream", e);
    }
    finally
    {
      closeQuietly(socket);
    }
  }

  private BluetoothSocket connect() throws InterruptedException
  {
    BluetoothSocket socket = null;
    while (socket == null && active.get())
    {
      try
      {
        synchronized (adapter)
        {
          if(adapter.isDiscovering())
          {
            adapter.cancelDiscovery();
          }
        }

        try
        {
          Method m = device.getClass().getMethod("createRfcommSocket", int.class);
          socket = (BluetoothSocket) m.invoke(device, 1);
        }
        catch (NoSuchMethodException e)
        {
          socket = device.createRfcommSocketToServiceRecord(BluetoothConnector.SPP_SERIAL);
        }
        socket.connect();
        status = Status.CONNECTED;
      }
      catch (Exception e)
      {
        considerStoppingOnFailure();
        Log.w(Constants.TAG, "Couldn't connect to device [" + device.getName() + ", " + device.getAddress() + "]", e);
        Thread.sleep(Constants.THREE_SECONDS);
        socket = null;
      }
    }
    return socket;
  }

  private void considerStoppingOnFailure()
  {
    long currentTime = System.currentTimeMillis();
    if(connectionFailingSince < 0)
    {
      connectionFailingSince = currentTime;
    }
    else
    {
      long difference = currentTime - connectionFailingSince;
      if(difference > MAX_CONNECTION_FAILURE_TIME)
      {
        eventBus.post(new ConnectionUnsuccesfulEvent(device));
        stop();
      }
    }
  }

  public void stop()
  {
    active.set(false);
    status = Status.STOPPED;
    thread.interrupt();
    try
    {
      if(socket != null)
      {
        socket.close();
      }
    }
    catch (IOException e)
    {
      Log.e(Constants.TAG, "Failed to close socket", e);
    }
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