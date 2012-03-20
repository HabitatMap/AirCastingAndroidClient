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
    private Thread readerThread;
    private ReaderWorker readerWorker;

    public synchronized void start() {
        if (bluetoothAdapter != null && readerWorker == null) {
            String address = settingsHelper.getSensorAddress();
            device = bluetoothAdapter.getRemoteDevice(address);

            readerWorker = new ReaderWorker();
            readerThread = new Thread(readerWorker);
            readerThread.start();
        }
    }

    public synchronized void stop() {
        if (readerWorker != null) {
            readerWorker.stop();
            readerWorker = null;

            try {
                readerThread.join();
            } catch (InterruptedException e) {
                Log.e(TAG, "Bluetooth thread didn't finish", e);
            }
        }
    }

    public void read(String line) {
        try {
            SensorEvent event = parser.parse(line);
            eventBus.post(event);
        } catch (pl.llp.aircasting.sensor.external.ParseException e) {
            Log.e(TAG, "External sensor error", e);
        }
    }

    private class ReaderWorker implements Runnable {
        private boolean stopped = false;

        @Override
        public void run() {
            BluetoothSocket socket = null;
            InputStreamReader reader = null;

            try {
                socket = device.createRfcommSocketToServiceRecord(SPP_SERIAL);
                socket.connect();
                reader = new InputStreamReader(socket.getInputStream());
                final InputStreamReader finalReader = reader;

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
                                read(line);
                                return !stopped;
                            }

                            @Override
                            public Void getResult() {
                                return null;
                            }
                        }
                );
            } catch (IOException e) {
                Log.e(TAG, "Bluetooth communication failure", e);
            } finally {
                closeQuietly(reader);
                closeQuietly(socket);
            }
        }

        public void stop() {
            stopped = true;
        }
    }
}
