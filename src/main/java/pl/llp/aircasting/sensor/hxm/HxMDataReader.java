package pl.llp.aircasting.sensor.hxm;

import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.sensor.BluetoothSocketReader;

import android.bluetooth.BluetoothSocket;
import com.google.common.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static pl.llp.aircasting.sensor.BluetoothConnector.sleepFor;

class HxMDataReader implements BluetoothSocketReader
{
  final PacketReader reader = new PacketReader();
  final BluetoothSocket socket;
  final String address;

  EventBus eventBus;
  ByteArrayOutputStream bos = new ByteArrayOutputStream();

  public HxMDataReader(BluetoothSocket socket)
  {
    this.socket = socket;
    this.address = socket.getRemoteDevice().getAddress();
  }

  @Override
  public void read() throws IOException
  {
    InputStream stream = socket.getInputStream();
    byte[] readBuffer = new byte[4096];

    int bytesRead = stream.read(readBuffer);
    if (bytesRead > 0)
    {
      bos.write(readBuffer, 0, bytesRead);
      int processed = reader.tryReading(bos);
      if(processed > 0)
      {
        byte[] bytes = bos.toByteArray();
        bos = new ByteArrayOutputStream();
        bos.write(bytes, processed, bytes.length - processed);
      }
    }
    else
    {
      sleepFor(100);
    }
  }

  class PacketReader
  {
    int STX = 0x02;
    int ETX = 0x03;
    int ID = 0x26;

    public Integer tryReading(ByteArrayOutputStream input)
    {
      if (input.size() > 59)
      {
        byte[] bytes = input.toByteArray();
        int index = 0;
        while (bytes.length - index > 59)
        {
          if (validate(bytes, index))
          {
            process(bytes, index);
            return index + 60;
          }
          else
          {
            index++;
          }
        }
      }
      return 0;
    }

    boolean validate(byte[] packet, int offset)
    {
      return packet[offset]         == STX
          && packet[(offset + 1)]   == ID
          && packet[(offset + 59)]  == ETX;
    }

    void process(byte[] packet, int index)
    {
      int heartRate = Math.abs(packet[12 + index]);
      SensorEvent event = heartRateEvent(heartRate);
      event.setAddress(address);
      eventBus.post(event);
    }

    private SensorEvent heartRateEvent(int heartRate)
    {
      return new SensorEvent("Zephyr", "Zephyr HxM", "Heart Rate", "HR", "beats per minute", "bpm", 40, 85, 130, 175, 220, heartRate);
    }
  }

  public void setEventBus(EventBus eventBus)
  {
    this.eventBus = eventBus;
  }
}
