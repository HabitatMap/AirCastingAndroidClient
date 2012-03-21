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
import java.io.*;
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
    private Thread readerThread;

    public synchronized void start() {
        String address = settingsHelper.getSensorAddress();
        if (bluetoothAdapter != null && (device == null || !device.getAddress().equals(address))) {
            if (device != null) {
                stop();
            }
            device = bluetoothAdapter.getRemoteDevice(address);

            ReaderWorker readerWorker = new ReaderWorker(device);
            readerThread = new Thread(readerWorker);
            readerThread.start();
        }
    }

    private void stop() {
        if (readerThread != null) {
            readerThread.interrupt();
        }
    }

    public void process(String line) {
        try {
            SensorEvent event = parser.parse(line);
            eventBus.post(event);
        } catch (pl.llp.aircasting.sensor.external.ParseException e) {
            Log.e(TAG, "External sensor error", e);
        }
    }

    private class ReaderWorker implements Runnable {
        public static final int ONE_SECOND = 1000;
        public static final int EIGHT_SECONDS = 8000;

        BluetoothSocket socket = null;
        InputStream stream = null;
        BluetoothDevice device;

        public ReaderWorker(BluetoothDevice device) {
            this.device = device;
        }

        @Override
        public void run() {
            connect();

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

        private void connect() {
            int sleep = ONE_SECOND;
            while (socket == null) {
                try {
                    socket = device.createRfcommSocketToServiceRecord(SPP_SERIAL);
                    socket.connect();

                    if (socket == null) {
                        Thread.sleep(sleep);
                    }

                    sleep *= 2;
                    sleep = Math.min(sleep, EIGHT_SECONDS);
                } catch (Exception e) {
                    socket = null;
                }
            }
        }
    }
}
