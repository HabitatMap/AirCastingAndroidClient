package pl.llp.aircasting.sensor.hxm;

import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.sensor.BluetoothSocketReader;

import android.bluetooth.BluetoothSocket;
import com.google.common.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

class HxMDataReader implements BluetoothSocketReader
{
  PacketReader packetReader = new PacketReader();
  EventBus eventBus;

  boolean active;
  String address;

  public void read(BluetoothSocket socket) throws IOException
  {
    active = true;
    address = socket.getRemoteDevice().getAddress();

    InputStream stream = socket.getInputStream();
    byte[] readBuffer = new byte[4096];

    while (active)
    {
      int bytesRead = stream.read(readBuffer);
      if (bytesRead > 0)
      {
        byte[] data = new byte[bytesRead];
        System.arraycopy(readBuffer, 0, data, 0, bytesRead);
        packetReader.tryReading(data);
      }
    }
  }

  class PacketReader
  {
    int STX = 0x02;
    int ETX = 0x03;
    int ID = 0x26;

    ByteArrayOutputStream bos = new ByteArrayOutputStream();

    public void tryReading(byte[] input)
    {
      writeBytesToBuffer(bos, input);
      if (bos.size() > 59)
      {
        byte[] bytes = bos.toByteArray();
        int index = 0;
        while (bytes.length - index > 59)
        {
          if (validate(bytes, index))
          {
            process(bytes, index);
            bos = new ByteArrayOutputStream();
            int from = index + 60;
            int len = bytes.length - from;
            bos.write(bytes, from, len);
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
      catch (IOException ignored)
      {
      }
    }

    boolean validate(byte[] packet, int offset)
    {
      return packet[(offset + 0)] == STX
          && packet[(offset + 1)] == ID
          && packet[(offset + 59)] == ETX;
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

  public void stop()
  {
    active = false;
  }
}
