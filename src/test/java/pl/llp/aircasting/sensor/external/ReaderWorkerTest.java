package pl.llp.aircasting.sensor.external;

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.event.sensor.SensorEvent;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import com.google.common.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(InjectedTestRunner.class)
public class ReaderWorkerTest
{
  ReaderWorker worker;

  @Before
  public void setup() throws IOException
  {
    worker = new ReaderWorker(mock(BluetoothAdapter.class),
                              mock(BluetoothDevice.class),
                              mock(EventBus.class));
    worker.parser = mock(ExternalSensorParser.class);
  }

  @Test
  public void shouldReadInputAndGenerateEvents() throws IOException, pl.llp.aircasting.sensor.external.ParseException
  {
    SensorEvent event1 = mock(SensorEvent.class);
    doReturn(event1).when(worker.parser).parse("Reading 1");

    worker.process("Reading 1");

    verify(worker.eventBus).post(event1);
  }
}
