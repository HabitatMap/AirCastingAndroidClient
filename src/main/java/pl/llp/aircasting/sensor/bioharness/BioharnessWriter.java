package pl.llp.aircasting.sensor.bioharness;

import pl.llp.aircasting.sensor.bluetooth.BluetoothSocketWriter;
import pl.llp.aircasting.util.Constants;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.OutputStream;

public class BioharnessWriter implements BluetoothSocketWriter {
    private OutputStream outputStream;
    BluetoothSocket socket;

    long last = 0;

    public BioharnessWriter(BluetoothSocket socket) {
        this.socket = socket;
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        if (outputStream == null) {
            outputStream = socket.getOutputStream();
        }
        outputStream.write(bytes);
    }

    @Override
    public void writeCyclic() throws IOException {
        long current = System.currentTimeMillis();
        if (current - last > Constants.THREE_SECONDS) {
            write(new byte[]{2, 35, 0, 0, 3});
            last = System.currentTimeMillis();
        }
    }
}
