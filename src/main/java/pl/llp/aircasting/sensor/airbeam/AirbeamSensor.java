package pl.llp.aircasting.sensor.airbeam;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import pl.llp.aircasting.sensor.AbstractSensor;
import pl.llp.aircasting.sensor.ExternalSensorDescriptor;
import pl.llp.aircasting.sensor.ReaderWorker;
import pl.llp.aircasting.sensor.WriterWorker;
import pl.llp.aircasting.sensor.external.LineDataReader;

/**
 * Created by radek on 07/12/17.
 */
public class AirbeamSensor extends AbstractSensor {
    ReaderWorker readerWorker;
    LineDataReader lineReader;
    WriterWorker writerWorker;

    public AirbeamSensor(ExternalSensorDescriptor descriptor, EventBus eventBus, BluetoothAdapter bluetoothAdapter) {
        super(descriptor, eventBus, bluetoothAdapter);
        eventBus.register(this);
    }

    @Override
    protected void startWorking() {
        readerWorker.start();
        writerWorker.start();
        Log.i("airbeam", "started working");
    }

    @Override
    protected void injectSocket(BluetoothSocket socket) {
        lineReader = new LineDataReader(socket, socket.getRemoteDevice().getAddress());
        AirBeamWriter socketWriter = new AirBeamWriter(socket);
        readerWorker = new ReaderWorker(adapter, device, eventBus, lineReader);
        writerWorker = new WriterWorker(device, eventBus, socketWriter);
    }

    @Override
    protected void customStop() {
        if (readerWorker != null) {
            readerWorker.stop();
        }

        if (writerWorker != null) {
            writerWorker.stop();
        }
    }

    @Subscribe
    public void onEvent(Airbeam2ConfigMessageEvent event) {
        writerWorker.write(event.getMessage());
    }
}
