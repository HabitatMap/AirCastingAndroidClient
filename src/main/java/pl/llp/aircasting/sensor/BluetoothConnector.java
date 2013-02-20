package pl.llp.aircasting.sensor;

import pl.llp.aircasting.event.ConnectionUnsuccessfulEvent;
import pl.llp.aircasting.util.Constants;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import com.google.common.eventbus.EventBus;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ags on 26/09/12 at 15:58
 */
public class BluetoothConnector
{
  public static final UUID SPP_SERIAL = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

  private final BluetoothAdapter adapter;
  private final BluetoothDevice device;
  private final EventBus eventBus;
  private final AtomicBoolean active = new AtomicBoolean(false);

  private static final long MAX_CONNECTION_FAILURE_TIME = Constants.ONE_MINUTE;

  private volatile long connectionFailingSince = -1;
  private boolean noSuchMethod;

  public BluetoothConnector(BluetoothAdapter adapter, BluetoothDevice device, EventBus eventBus)
  {
    this.adapter = adapter;
    this.device = device;
    this.eventBus = eventBus;
  }

  public BluetoothSocket getSocket()
  {
    active.set(true);
    BluetoothSocket socket = null;
    cancelDiscovery();
    while (socket == null && active.get())
    {
      try
      {
        try
        {
          socket = getSocketByHack(1);
        }
        catch (NoSuchMethodException e)
        {
          socket = getSocketNormally();
        }
      }
      catch (Exception e)
      {
        considerStoppingOnFailure();
        Log.w(Constants.TAG, "Couldn't connect to device [" + device.getName() + ", " + device.getAddress() + "]", e);
        sleepFor(Constants.THREE_SECONDS);
        socket = null;
      }
    }
    return socket;
  }

  public static void sleepFor(long sleepTime)
  {
    try
    {
      Thread.sleep(sleepTime);
    }
    catch (InterruptedException ignore)
    {

    }
  }

  public BluetoothSocket getSocketNormally() throws IOException
  {
    return device.createRfcommSocketToServiceRecord(SPP_SERIAL);
  }

  private BluetoothSocket getSocketByHack(int channel) throws Exception
  {
    if(noSuchMethod)
    {
      return null;
    }

    try
    {
      Method m = device.getClass().getMethod("createRfcommSocket", int.class);
      return (BluetoothSocket) m.invoke(device, channel);
    }
    catch (NoSuchMethodException e)
    {
      noSuchMethod = true;
      throw e;
    }
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
        eventBus.post(new ConnectionUnsuccessfulEvent(device));
        stop();
      }
    }
  }

  public BluetoothSocket connect(BluetoothSocket socket)
  {
    cancelDiscovery();
    active.set(true);
    while (active.get())
    {
      try
      {
        socket.connect();
        return socket;
      }
      catch (Exception e)
      {
        considerStoppingOnFailure();
        Log.w(Constants.TAG, "Couldn't connect to device [" + device.getName() + ", " + device.getAddress() + "]", e);
        sleepFor(Constants.THREE_SECONDS);
      }
    }
    return null;
  }

  public void stop()
  {
    active.set(false);
  }

  private void cancelDiscovery()
  {
    synchronized (adapter)
    {
      if(adapter.isDiscovering())
      {
        adapter.cancelDiscovery();
      }
    }
  }
}
