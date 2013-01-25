package pl.llp.aircasting.sensor;

import java.io.IOException;

public interface BluetoothSocketWriter
{
  void write(byte[] bytes) throws IOException;

  void writeCyclic() throws IOException;
}
