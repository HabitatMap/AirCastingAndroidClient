package pl.llp.aircasting.sensor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import com.google.common.eventbus.EventBus;
import pl.llp.aircasting.activity.events.SensorStoppedEvent;

import static java.lang.Thread.State;

public abstract class AbstractSensor {
    protected final ExternalSensorDescriptor descriptor;
    protected final EventBus eventBus;
    protected final BluetoothAdapter adapter;
    protected final BluetoothDevice device;
    private Thread thread;

    public AbstractSensor(ExternalSensorDescriptor descriptor, EventBus eventBus, BluetoothAdapter adapter) {
        this.descriptor = descriptor;
        this.eventBus = eventBus;
        this.adapter = adapter;

        if (descriptor == null || eventBus == null || adapter == null) {
            throw new NullPointerException("Cannot have nulls!");
        }

        this.device = adapter.getRemoteDevice(descriptor.getAddress());
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    BluetoothConnector connector = new BluetoothConnector(adapter, device, eventBus);
                    BluetoothSocket socket = getSocket(connector);
                    injectSocket(socket);
                    if (connector.connect(socket) != null) {
                        startWorking();
                    }
                }
            });
        }

        if (State.NEW.equals(thread.getState())) {
            thread.start();
        }
    }

    protected BluetoothSocket getSocket(BluetoothConnector connector) {
        return connector.getSocket();
    }

    protected abstract void startWorking();

    protected abstract void injectSocket(BluetoothSocket socket);

    public void stop() {
        eventBus.post(new SensorStoppedEvent(descriptor));
        thread = null;
        customStop();
    }

    protected abstract void customStop();
}
