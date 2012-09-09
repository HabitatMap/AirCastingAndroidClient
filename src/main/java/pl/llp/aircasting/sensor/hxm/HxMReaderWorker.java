package pl.llp.aircasting.sensor.hxm;

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
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.io.Closeables.closeQuietly;

/**
 * Created by ags on 21/06/12 at 13:23
 */
class HxMReaderWorker
{
  public static final UUID SPP_SERIAL = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

  BluetoothAdapter adapter;
  BluetoothDevice device;
  EventBus eventBus;

  AtomicBoolean active = new AtomicBoolean(false);

  Status status = Status.NOT_YET_STARTED;

  private Thread thread;

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
            if (socket != null)
              read(socket);
          }
          catch (InterruptedException e)
          {
            status = Status.DID_NOT_CONNECT;
            Log.e(Constants.TAG, "Failed to establish adapter connection", e);
            return;
          }
        }
      }
    });
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
    while (socket == null && active.get())
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

        try
        {
          Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
          socket = (BluetoothSocket) m.invoke(device, 1);
        }
        catch (NoSuchMethodException e)
        {
          socket = device.createRfcommSocketToServiceRecord(SPP_SERIAL);
        }
        socket.connect();
        Thread.sleep(300);

        status = Status.CONNECTED;
        return socket;
      }
      catch (Exception e)
      {
        Log.w(Constants.TAG, "Couldn't connect to device [" + device.getName() + ", " + device.getAddress() + "]", e);
        Thread.sleep(Constants.THREE_SECONDS);
        socket = null;
      }
    }
    return socket;
  }

  void process(byte[] packet)
  {
    int heartRate = HxMPacket.getHeartRate(packet);
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
        if(len > 58)
        {
          byte[] data = new byte[len];
          System.arraycopy(buf, off, data, 0, len);
          if(check(data))
            process(data);
        }
        return active.get();
      }

      private boolean check(byte[] bytes)
      {
        return bytes[1] == 0x26;
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

}
