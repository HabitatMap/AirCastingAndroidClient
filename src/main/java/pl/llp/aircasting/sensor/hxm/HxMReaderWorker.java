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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.io.Closeables.closeQuietly;

class HxMReaderWorker
{
  private static final long MAX_CONNECTION_FAILURE_TIME = Constants.ONE_MINUTE;

  BluetoothAdapter adapter;
  BluetoothDevice device;
  EventBus eventBus;

  AtomicBoolean active = new AtomicBoolean(false);

  Status status = Status.NOT_YET_STARTED;

  private Thread thread;

  private volatile long connectionFailingSince = -1;

  PacketReader packetReader = new PacketReader();

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
      byte[] readBuffer = new byte[4096];

      while(active.get())
      {
        int bytesRead = stream.read(readBuffer);
        if(bytesRead > 0)
        {
          byte[] data = new byte[bytesRead];
          System.arraycopy(readBuffer, 0, data, 0, bytesRead);
          packetReader.tryReading(data);
        }
      }
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
          Method m = device.getClass().getMethod("createRfcommSocket", int.class);
          socket = (BluetoothSocket) m.invoke(device, 1);
        }
        catch (NoSuchMethodException e)
        {
          socket = device.createRfcommSocketToServiceRecord(BluetoothConnector.SPP_SERIAL);
        }
        socket.connect();
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

  public void stop()
  {
    active.set(false);
    status = Status.STOPPED;
    thread.interrupt();
  }

  @Override
  public String toString()
  {
    return "ReaderWorker{" +
        "device=" + device +
        ", status=" + status +
        '}';
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
      stop();
      long difference = currentTime - connectionFailingSince;
      if(difference > MAX_CONNECTION_FAILURE_TIME)
      {
        eventBus.post(new ConnectionUnsuccesfulEvent(device));
      }
    }
  }

  class PacketReader
  {
    int STX = 0x02;
    int ETX = 0x03;
    int ID = 0x26;

    ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);

    public void tryReading(byte[] input)
    {
      writeBytesToBuffer(bos, input);
      if(bos.size() > 59)
      {
        byte[] bytes = bos.toByteArray();
        int index = 0;
        while(bytes.length - index > 59)
        {
          if(validate(bytes, index))
          {
            process(bytes, index);
            bos = new ByteArrayOutputStream(4096);
            bos.write(bytes, index + 60, Math.min(index + 120, bytes.length - 60));
            return;
          }
          else
          {
            index++;
          }
        }
      }
    }

    private void writeBytesToBuffer(ByteArrayOutputStream bos, byte[] bytes)
    {
      try
      {
        bos.write(bytes);
      }
      catch (IOException ignored) { }
    }

    boolean validate(byte[] packet, int offset)
    {
      return packet[0 + offset] == STX
          && packet[1 + offset] == ID
          && packet[59 + offset] == ETX;
    }

    void process(byte[] packet, int index)
    {
      int heartRate = Math.abs(packet[12 + index]);
      SensorEvent event = heartRateEvent(heartRate);
      event.setAddress(device.getAddress());
      eventBus.post(event);
    }

    private SensorEvent heartRateEvent(int heartRate)
    {
      return new SensorEvent("Zephyr", "Zephyr HxM", "Heart Rate", "HR", "beats per minute", "bpm", 40, 85, 130, 175, 220, heartRate);
    }
  }
}
