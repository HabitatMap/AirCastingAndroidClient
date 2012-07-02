package pl.llp.aircasting.sensor.external;

import pl.llp.aircasting.helper.SettingsHelper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.UUID;

import javax.annotation.Nullable;

@Singleton
public class ExternalSensor
{
  public static final UUID SPP_SERIAL = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

  @Inject EventBus eventBus;
  @Inject SettingsHelper settingsHelper;
  @Nullable @Inject BluetoothAdapter bluetoothAdapter;
  @Inject ExternalSensorParser parser;

  private BluetoothDevice device;
  ReaderWorker readerWorker;

  public synchronized void start()
  {
    String address = settingsHelper.getSensorAddress();
    if (bluetoothAdapter != null && address != null && (device == null || addressChanged(address)))
    {
      if (device != null)
      {
        stop();
      }
      device = bluetoothAdapter.getRemoteDevice(address);

      readerWorker = new ReaderWorker(bluetoothAdapter, device, parser, eventBus);
      readerWorker.start();
    }
  }

  private boolean addressChanged(String address)
  {
    return !device.getAddress().equals(address);
  }

  private void stop()
  {
    readerWorker.stop();
  }
}

