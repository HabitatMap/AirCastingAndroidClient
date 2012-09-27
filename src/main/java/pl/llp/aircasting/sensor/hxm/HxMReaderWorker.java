package pl.llp.aircasting.sensor.hxm;

import pl.llp.aircasting.activity.extsens.BluetoothConnector;
import pl.llp.aircasting.event.ConnectionUnsuccesfulEvent;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.sensor.Status;
import pl.llp.aircasting.util.Constants;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import com.google.common.eventbus.EventBus;
import com.google.common.io.ByteProcessor;
import com.google.common.io.ByteStreams;
import com.google.common.io.InputSupplier;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.io.Closeables.closeQuietly;

/**
 * Created by ags on 21/06/12 at 13:23
 */
class HxMReaderWorker
{
  private static final long MAX_CONNECTION_FAILURE_TIME = Constants.ONE_MINUTE;

  BluetoothAdapter adapter;
  BluetoothDevice device;
  BluetoothConnector connector = new BluetoothConnector();
  EventBus eventBus;

  AtomicBoolean active = new AtomicBoolean(false);

  Status status = Status.NOT_YET_STARTED;

  private Thread thread;

  private volatile long connectionFailingSince = -1;

  public HxMReaderWorker(BluetoothAdapter adapter,
                         BluetoothDevice device,
                         EventBus eventBus)
  {
    this.adapter = adapter;
    this.device = device;
    this.eventBus = eventBus;
  }

  public void start()
  {
    active.set(true);
    status = Status.STARTED;
    thread = new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        while (active.get())
        {
          try
          {
            BluetoothSocket socket = connect();
            status = Status.CONNECTED;
            if (socket != null)
              read(socket);
          }
          catch (InterruptedException e)
          {
            considerStoppingOnFailure();
            status = Status.DID_NOT_CONNECT;
            Log.e(Constants.TAG, "Failed to establish adapter connection", e);
            return;
          }
        }
      }
    }, String.format("Reader [%s, %s]", device.getName(), device.getAddress()));
    thread.start();
  }

  private void read(BluetoothSocket socket)
  {
    InputStream stream = null;
    try
    {
      stream = socket.getInputStream();

      ByteStreams.readBytes(
          inputSupplier(stream),
          byteProcessor());
    }
    catch (IOException e)
    {
      considerStoppingOnFailure();
      status = Status.CONNECTION_INTERRUPTED;
      Log.w(Constants.TAG, "Bluetooth communication failure - mostly likely end of stream", e);
    }
    finally
    {
      closeQuietly(stream);
      closeQuietly(socket);
    }
  }

  private BluetoothSocket connect() throws InterruptedException
  {
    BluetoothSocket socket = null;
    while (active.get())
    {
      try
      {
        synchronized (adapter)
        {
          if(adapter.isDiscovering())
          {
            adapter.cancelDiscovery();
          }
        }

        socket = connector.connect(device);
      }
      catch (Exception e)
      {
        considerStoppingOnFailure();
        Log.w(Constants.TAG, "Couldn't connect to device [" + device.getName() + ", " + device.getAddress() + "]", e);
        Thread.sleep(Constants.THREE_SECONDS);
        socket = null;
      }
    }
    return socket;
  }

  void process(byte[] packet)
  {
    int heartRate = Math.abs(packet[11]);
    SensorEvent event = heartRateEvent(heartRate);
    event.setAddress(device.getAddress());
    eventBus.post(event);
  }

  private SensorEvent heartRateEvent(int heartRate)
  {
    return new SensorEvent("Zephyr", "Zephyr HxM", "Heart Rate", "HR", "beats per minute", "bpm", 40, 85, 130, 175, 220, heartRate);
  }

  public void stop()
  {
    active.set(false);
    status = Status.STOPPED;
    thread.interrupt();
  }

  private ByteProcessor<Void> byteProcessor()
  {
    return new ByteProcessor<Void>()
    {
      @Override
      public boolean processBytes(byte[] buf, int off, int len) throws IOException
      {
        connectionFailingSince = -1;
        if(len > 58)
        {
          byte[] data = new byte[len];
          System.arraycopy(buf, off, data, 0, len);
          process(data);
        }
        return active.get();
      }

      @Override
      public Void getResult()
      {
        return null;
      }
    };
  }

  @Override
  public String toString()
  {
    return "ReaderWorker{" +
        "device=" + device +
        ", status=" + status +
        '}';
  }

  private InputSupplier<InputStream> inputSupplier(final InputStream inputStream)
  {
    return new InputSupplier<InputStream>()
    {
      @Override
      public InputStream getInput() throws IOException
      {
        return inputStream;
      }
    };
  }

  private void considerStoppingOnFailure()
  {
    long currentTime = System.currentTimeMillis();
    if(connectionFailingSince < 0)
    {
      connectionFailingSince = currentTime;
    }
    else
    {
      long difference = currentTime - connectionFailingSince;
      if(difference > MAX_CONNECTION_FAILURE_TIME)
      {
        eventBus.post(new ConnectionUnsuccesfulEvent(device));
      }
    }
  }
}
