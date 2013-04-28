package pl.llp.aircasting.sensor.external;

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.model.events.SensorEvent;

import com.google.common.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(InjectedTestRunner.class)
public class LineReaderTest
{
  LineDataReader reader;

  @Before
  public void setup() throws IOException
  {
    reader = new LineDataReader(null, "123");
    reader.parser = mock(ExternalSensorParser.class);
    reader.eventBus = mock(EventBus.class);
  }

  @Test
  public void shouldReadInputAndGenerateEvents() throws ParseException
  {
    SensorEvent event1 = mock(SensorEvent.class);
    doReturn(event1).when(reader.parser).parse("Reading 1");

    reader.process("Reading 1");

    verify(reader.eventBus).post(event1);
  }
}
