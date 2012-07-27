package pl.llp.aircasting.sensor.external;

import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.sensor.Status;
import pl.llp.aircasting.util.Constants;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import com.google.common.eventbus.EventBus;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;
import com.google.common.io.LineProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.io.Closeables.closeQuietly;

/**
 * Created by ags on 21/06/12 at 13:23
 */
class ReaderWorker
{
  public static final UUID SPP_SERIAL = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

  BluetoothAdapter adapter;
  BluetoothDevice device;
  EventBus eventBus;

  AtomicBoolean active = new AtomicBoolean(false);

  Status status = Status.NOT_YET_STARTED;

  ExternalSensorParser parser = new ExternalSensorParser();

  public ReaderWorker(BluetoothAdapter adapter,
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
    new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        while (active.get())
        {
          try
          {
            BluetoothSocket socket = connect();
            if(socket != null)
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
    }).start();
  }

  private void read(BluetoothSocket socket)
  {
    InputStream stream = null;
    try
    {
      stream = socket.getInputStream();
      final Reader finalReader = new InputStreamReader(stream);

      CharStreams.readLines(
          inputSupplier(finalReader),
          lineProcessor());
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
        status = Status.CONNECTED;
        return socket;
      }
      catch (Exception e)
      {
        Log.w(Constants.TAG, "Couldn't connect to device [" + device.getName() + ", " + device.getAddress() + " ]", e);
        Thread.sleep(Constants.THREE_SECONDS);
        socket = null;
      }
    }
    return socket;
  }

  void process(String line)
  {
    try
    {
      SensorEvent event = parser.parse(line);
      event.setAddress(device.getAddress());
      eventBus.post(event);
    }
    catch (ParseException e)
    {
      Log.e(Constants.TAG, "External sensor error", e);
    }
  }

  public void stop()
  {
    active.set(false);
    status = Status.STOPPED;
  }

  private LineProcessor<Void> lineProcessor()
  {
    return new LineProcessor<Void>()
    {
      @Override
      public boolean processLine(String line) throws IOException
      {
        process(line);
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

  private InputSupplier<Reader> inputSupplier(final Reader finalReader)
  {
    return new InputSupplier<Reader>()
    {
      @Override
      public Reader getInput() throws IOException
      {
        return finalReader;
      }
    };
  }

}
