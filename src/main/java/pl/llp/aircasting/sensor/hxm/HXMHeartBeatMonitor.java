package pl.llp.aircasting.sensor.hxm;

import pl.llp.aircasting.sensor.AbstractSensor;
import pl.llp.aircasting.sensor.ExternalSensorDescriptor;
import pl.llp.aircasting.sensor.ReaderWorker;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import com.google.common.eventbus.EventBus;

public class HXMHeartBeatMonitor extends AbstractSensor {
    ReaderWorker readerWorker;

    public HXMHeartBeatMonitor(ExternalSensorDescriptor descriptor, EventBus eventBus, BluetoothAdapter adapter) {
        super(descriptor, eventBus, adapter);
    }

    @Override
    protected void startWorking() {
        readerWorker.start();
    }

    @Override
    protected void injectSocket(BluetoothSocket socket) {
        readerWorker = new ReaderWorker(adapter, device, eventBus, new HxMDataReader(socket));
    }

    private boolean addressChanged(String address) {
        return !device.getAddress().equals(address);
    }

    @Override
    protected void customStop() {
        if (readerWorker != null) {
            readerWorker.stop();
        }
    }
}