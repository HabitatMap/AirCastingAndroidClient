package pl.llp.aircasting.event;

import android.bluetooth.BluetoothDevice;

/**
 * Created by ags on 27/09/12 at 14:47
 */
public class ConnectionUnsuccessfulEvent
{
  final BluetoothDevice device;

  public ConnectionUnsuccessfulEvent(BluetoothDevice device)
  {
    this.device = device;
  }

  public BluetoothDevice getDevice()
  {
    return device;
  }
}
