package pl.llp.aircasting.sensor;

import android.bluetooth.BluetoothSocket;
import com.google.common.eventbus.EventBus;

import java.io.IOException;

/**
 * Created by ags on 01/10/12 at 17:01
 */
public interface BluetoothSocketReader
{
  public void read(BluetoothSocket socket) throws IOException;

  void setEventBus(EventBus eventBus);
}
