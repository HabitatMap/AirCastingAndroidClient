package pl.llp.aircasting.activity.extsens;

import pl.llp.aircasting.util.Constants;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by ags on 26/09/12 at 15:58
 */
public class BluetoothConnector
{
  public static final UUID SPP_SERIAL = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

  public BluetoothSocket connect(BluetoothDevice device) throws IOException
  {
    BluetoothSocket socket = connectUsingHack(device, 1);
    if(socket != null)
    {
      return socket;
    }
    socket = connectNormally(device);
    if(socket != null)
    {
      return socket;
    }
    socket = connectUsingHack(device, 2);
    if(socket != null)
        {
          return socket;
        }
    socket = connectUsingHack(device, 3);
    if(socket != null)
    {
      return socket;
    }
    Log.e(Constants.TAG, String.format("Couldn't connect to [%s, %s]", device.getName(), device.getAddress()));
    throw new IOException("Could not connect via Bluetooth");
  }

  private BluetoothSocket connectNormally(BluetoothDevice device)
  {
    try
    {
      BluetoothSocket socket = device.createRfcommSocketToServiceRecord(SPP_SERIAL);
      socket.connect();
      return socket;
    }
    catch (Exception ignore) { }
    return null;
  }

  private BluetoothSocket connectUsingHack(BluetoothDevice device, int channel)
  {
    try
    {
      Method m = device.getClass().getMethod("createRfcommSocket", int.class);
      BluetoothSocket socket = (BluetoothSocket) m.invoke(device, channel);
      socket.connect();
      return socket;
    }
    catch (Exception ignore) { }
    return null;
  }


}
