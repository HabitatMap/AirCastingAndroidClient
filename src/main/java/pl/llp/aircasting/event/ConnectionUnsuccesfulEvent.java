package pl.llp.aircasting.event;

import android.bluetooth.BluetoothDevice;

/**
 * Created by ags on 27/09/12 at 14:47
 */
public class ConnectionUnsuccesfulEvent
{
  final BluetoothDevice device;

  public ConnectionUnsuccesfulEvent(BluetoothDevice device)
  {
    this.device = device;
  }

  public BluetoothDevice getDevice()
  {
    return device;
  }
}
