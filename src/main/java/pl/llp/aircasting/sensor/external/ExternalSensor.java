package pl.llp.aircasting.sensor.external;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import com.google.common.eventbus.EventBus;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.common.io.LineProcessor;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.helper.SettingsHelper;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.UUID;

import static com.google.common.io.Closeables.closeQuietly;

@Singleton
public class ExternalSensor {
  private static final String TAG = ExternalSensor.class.getSimpleName();
  public static final UUID SPP_SERIAL = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

  @Inject EventBus eventBus;
  @Inject SettingsHelper settingsHelper;
  @Nullable @Inject BluetoothAdapter bluetoothAdapter;
  @Inject ExternalSensorParser parser;

  private BluetoothDevice device;
  private ReaderWorker readerWorker;

  public synchronized void start() {
    String address = settingsHelper.getSensorAddress();
    if (bluetoothAdapter != null && address != null && (device == null || addressChanged(address))) {
      if (device != null) {
        stop();
      }
      device = bluetoothAdapter.getRemoteDevice(address);

      readerWorker = new ReaderWorker(bluetoothAdapter, device);
      readerWorker.start();
    }
  }

  private boolean addressChanged(String address) {
    return !device.getAddress().equals(address);
  }

  private void stop() {
    readerWorker.stop();
  }

  public void process(String line) {
    try {
      SensorEvent event = parser.parse(line);
      eventBus.post(event);
    } catch (pl.llp.aircasting.sensor.external.ParseException e) {
      Log.e(TAG, "External sensor error", e);
    }
  }

  private class ReaderWorker {
    public static final int THREE_SECONDS = 3000;

    BluetoothSocket socket = null;
    InputStream stream = null;
    BluetoothAdapter adapter;
    BluetoothDevice device;
    private boolean stop;

    public ReaderWorker(BluetoothAdapter adapter, BluetoothDevice device) {
      this.adapter = adapter;
      this.device = device;
    }

    public void start() {
      stop = false;
      new Thread(new Runnable() {
        @Override
        public void run() {
          while (!stop)
          {
            try {
              connect();
              read();
            } catch (InterruptedException e) {
              Log.e(TAG, "Failed to establish bluetooth connection", e);
              return;
            }
          }
        }
      }).start();
    }

    private void read() {
      try {
        stream = socket.getInputStream();
        final Reader finalReader = new InputStreamReader(stream);

        CharStreams.readLines(
            new InputSupplier<Reader>() {
              @Override
              public Reader getInput() throws IOException {
                return finalReader;
              }
            },
            new LineProcessor<Void>() {
              @Override
              public boolean processLine(String line) throws IOException {
                process(line);
                return true;
              }

              @Override
              public Void getResult() {
                return null;
              }
            }
                             );
      } catch (IOException e) {
        Log.w(TAG, "Bluetooth communication failure - mostly likely end of stream", e);
      } finally {
        closeQuietly(stream);
        closeQuietly(socket);
      }
    }

    public void stop() {
      stop = true;
      closeQuietly(stream);
      closeQuietly(socket);
    }

    private void connect() throws InterruptedException {
      socket = null;
      while (socket == null) {
        try {
          adapter.cancelDiscovery();
          socket = device.createRfcommSocketToServiceRecord(SPP_SERIAL);
          socket.connect();
        } catch (Exception e) {
          Thread.sleep(THREE_SECONDS);
          socket = null;
        }
      }
    }
  }
}
