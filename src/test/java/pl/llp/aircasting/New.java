package pl.llp.aircasting;

import pl.llp.aircasting.helper.MetadataHelper;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.events.SensorEvent;

import java.util.Date;
import java.util.Random;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by ags on 29/06/12 at 18:32
 */
public class New
{
  static Random random = new Random();

  public static Session session() {
    Session sess = new Session();
    sess.setOffset60DB(random.nextInt(100));
    sess.setCalibration(random.nextInt(5));
    sess.setStart(new Date());
    sess.setEnd(new Date());

    return sess;
  }

  public static MeasurementStream stream() {
    MeasurementStream stream = new MeasurementStream("someVendor",
                                                     "sensor0",
                                                     "type1",
                                                     "t1",
                                                     "unit2",
                                                     "symbol3", 1, 2, 3, 4, 5);

    return stream;
  }

  public static SensorEvent sensorEvent() {
    return new SensorEvent("CERN", "LHC", "Hadrons", "H", "number", "#", 50, 60, 70, 85, 100, 12);
  }

  public static Sensor sensor() {
    return new Sensor(sensorEvent(), Constants.CURRENT_SESSION_FAKE_ID);
  }

  public static SensorEvent sensorEvent(String sensorName, double value)
  {
    return new SensorEvent("CERN", sensorName, "Higgs boson", "HB", "number", "#", 1, 2, 3, 4, 5, value);
  }

  public static Note note(String text)
  {
    return new Note(new Date(), text, null, null);
  }

  public static Measurement measurement(int value)
  {
    return new Measurement(0, 0, value);
  }

  public static MetadataHelper metadataHelper()
  {
    MetadataHelper metadataHelper = mock(MetadataHelper.class);

    when(metadataHelper.getPhoneModel()).thenReturn("byePhone");

    return metadataHelper;
  }
}
