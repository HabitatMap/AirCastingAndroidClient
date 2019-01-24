package pl.llp.aircasting.sensor.bioharness;

import pl.llp.aircasting.sensor.common.AbstractSensor;
import pl.llp.aircasting.sensor.bluetooth.BluetoothConnector;
import pl.llp.aircasting.sensor.common.ExternalSensorDescriptor;
import pl.llp.aircasting.sensor.common.ReaderWorker;
import pl.llp.aircasting.sensor.common.WriterWorker;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import com.google.common.eventbus.EventBus;

import java.io.IOException;

public class BioharnessSensor extends AbstractSensor {
    ReaderWorker readerWorker;
    WriterWorker writerWorker;

    public BioharnessSensor(ExternalSensorDescriptor descriptor, EventBus eventBus, BluetoothAdapter adapter) {
        super(descriptor, eventBus, adapter);
    }

    @Override
    protected BluetoothSocket getSocket(BluetoothConnector connector) {
        try {
            return connector.getSocketNormally();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void startWorking() {
        readerWorker.start();
        writerWorker.start();
        writerWorker.write(PacketType.SummaryPacket.getRequest(Packet.Request.ENABLED));
        writerWorker.write(PacketType.RtoRPacket.getRequest(Packet.Request.ENABLED));
    }

    @Override
    protected void injectSocket(BluetoothSocket socket) {
        BioharnessDataReader reader = new BioharnessDataReader(socket, eventBus);
        BioharnessWriter socketWriter = new BioharnessWriter(socket);
        readerWorker = new ReaderWorker(adapter, device, eventBus, reader);
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
}

