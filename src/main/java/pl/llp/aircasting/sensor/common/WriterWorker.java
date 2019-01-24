package pl.llp.aircasting.sensor.common;

import pl.llp.aircasting.sensor.bluetooth.BluetoothSocketWriter;
import pl.llp.aircasting.util.Logger;
import pl.llp.aircasting.event.network.ConnectionUnsuccessfulEvent;
import android.bluetooth.BluetoothDevice;
import com.google.common.eventbus.EventBus;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class WriterWorker extends Worker {
    private final AtomicBoolean active = new AtomicBoolean(false);
    private final BluetoothDevice device;
    private final BluetoothSocketWriter socketWriter;

    final Thread thread;
    final ConcurrentLinkedQueue<byte[]> queue = new ConcurrentLinkedQueue<byte[]>();

    public WriterWorker(BluetoothDevice device, EventBus eventBus, final BluetoothSocketWriter socketWriter) {
        super(eventBus);
        this.device = device;
        this.socketWriter = socketWriter;
        queue.clear();
        this.thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (active.get()) {
                    sleepFor(500);
                    try {
                        socketWriter.writeCyclic();
                        if (queue.isEmpty()) {
                            sleepFor(500);
                        } else {
                            byte[] bytes = queue.remove();
                            socketWriter.write(bytes);
                        }
                    } catch (IOException e) {
                        considerStoppingOnFailure();
                        Logger.e("Error writing to writer!", e);
                    }
                }
            }
        });
    }

    public void write(byte[] toWrite) {
        queue.add(toWrite);
    }

    public void customStart() {
        active.set(true);
        thread.start();
    }

    public void customStop() {
        active.set(false);
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public String toString() {
        return "WriterWorker{" +
                "device=" + device +
                ", status=" + status +
                '}';
    }

    public void handlePersistentFailure() {
        eventBus.post(new ConnectionUnsuccessfulEvent(device));
    }

    private void sleepFor(long sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException ignore) {

        }
    }
}
