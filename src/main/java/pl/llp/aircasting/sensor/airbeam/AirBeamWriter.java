package pl.llp.aircasting.sensor.airbeam;

import android.bluetooth.BluetoothSocket;
import android.util.Log;
import com.google.inject.Singleton;
import pl.llp.aircasting.sensor.BluetoothSocketWriter;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by radek on 07/12/17.
 */
@Singleton
public class AirBeamWriter implements BluetoothSocketWriter {
    private OutputStream outputStream;
    private BluetoothSocket socket;

    public AirBeamWriter(BluetoothSocket socket) {
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
    public void writeCyclic() throws IOException {}
}
