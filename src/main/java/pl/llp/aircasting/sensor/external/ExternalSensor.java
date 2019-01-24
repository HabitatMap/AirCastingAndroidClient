package pl.llp.aircasting.sensor.external;

import pl.llp.aircasting.sensor.common.AbstractSensor;
import pl.llp.aircasting.sensor.common.ExternalSensorDescriptor;
import pl.llp.aircasting.sensor.common.ReaderWorker;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import com.google.common.eventbus.EventBus;

public class ExternalSensor extends AbstractSensor {
    ReaderWorker readerWorker;

    public ExternalSensor(ExternalSensorDescriptor descriptor, EventBus eventBus, BluetoothAdapter adapter) {
        super(descriptor, eventBus, adapter);
    }

    @Override
    protected void startWorking() {
        readerWorker.start();
    }

    @Override
    protected void injectSocket(BluetoothSocket socket) {
        LineDataReader reader = new LineDataReader(socket, socket.getRemoteDevice().getAddress());
        readerWorker = new ReaderWorker(adapter, device, eventBus, reader);
    }

    @Override
    protected void customStop() {
        if (readerWorker != null) {
            readerWorker.stop();
        }
    }
}

