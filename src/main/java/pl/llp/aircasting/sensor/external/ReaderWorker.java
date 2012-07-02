package pl.llp.aircasting.sensor.external;

import pl.llp.aircasting.event.sensor.SensorEvent;
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
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.io.Closeables.closeQuietly;

/**
 * Created by ags on 21/06/12 at 13:23
 */
class ReaderWorker
{
  public static final int THREE_SECONDS = 3000;

  BluetoothAdapter adapter;
  BluetoothDevice device;
  ExternalSensorParser parser;
  EventBus eventBus;

  AtomicBoolean active = new AtomicBoolean(false);

  public ReaderWorker(BluetoothAdapter adapter,
                      BluetoothDevice device,
                      ExternalSensorParser parser,
                      EventBus eventBus)
  {
    this.adapter = adapter;
    this.device = device;
    this.parser = parser;
    this.eventBus = eventBus;
  }

  public void start()
  {
    active.set(true);
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
            read(socket);
          }
          catch (InterruptedException e)
          {
            Log.e(Constants.TAG, "Failed to establish bluetooth connection", e);
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
    while (socket == null)
    {
      try
      {
        adapter.cancelDiscovery();
        socket = device.createRfcommSocketToServiceRecord(ExternalSensor.SPP_SERIAL);
        socket.connect();
      }
      catch (Exception e)
      {
        Thread.sleep(THREE_SECONDS);
        socket = null;
      }
    }
    return socket;
  }

  public void process(String line)
  {
    try
    {
      SensorEvent event = parser.parse(line);
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
