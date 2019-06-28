package pl.llp.aircasting.sensor.airbeam;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Singleton;
import pl.llp.aircasting.sensor.common.AbstractSensor;
import pl.llp.aircasting.sensor.common.ExternalSensorDescriptor;
import pl.llp.aircasting.sensor.common.ReaderWorker;
import pl.llp.aircasting.sensor.common.WriterWorker;
import pl.llp.aircasting.sensor.external.LineDataReader;

/**
 * Created by radek on 07/12/17.
 */
@Singleton
public class AirbeamSensor extends AbstractSensor {
    ReaderWorker readerWorker;
    LineDataReader lineReader;
    WriterWorker writerWorker;

    private boolean mStopped = true;

    public AirbeamSensor(ExternalSensorDescriptor descriptor, EventBus eventBus, BluetoothAdapter bluetoothAdapter) {
        super(descriptor, eventBus, bluetoothAdapter);
        eventBus.register(this);
    }

    @Override
    protected void startWorking() {
        mStopped = false;
        readerWorker.start();
        writerWorker.start();
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
        if (!mStopped) {
            if (readerWorker != null) {
                readerWorker.stop();
            }

            if (writerWorker != null) {
                writerWorker.stop();
            }

            eventBus.unregister(this);
            mStopped = true;
        }
    }

    @Subscribe
    public void onEvent(Airbeam2ConfigMessageEvent event) {
        writerWorker.write(event.getMessage());
    }
}
