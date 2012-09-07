package pl.llp.aircasting.sensor;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import ioio.lib.android.bluetooth.BluetoothIOIOConnection;

/**
 * Created by ags on 07/09/12 at 13:21
 */
public class BluetoothBondEnforcer
{
  public static void forceBonding(BluetoothDevice device)
  {
    if (device.getBondState() == BluetoothDevice.BOND_NONE)
    {
      try
      {
        BluetoothSocket temp = BluetoothIOIOConnection.createSocket(device);
        if (temp != null)
        {
          temp.connect();
          temp.close();
        }
      }
      catch (Exception ignore)
      {
        System.out.println(ignore);
      }
    }
  }
}
