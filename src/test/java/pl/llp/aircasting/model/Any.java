package pl.llp.aircasting.model;

import java.util.Date;
import java.util.Random;

public class Any
{
  static Random random = new Random();

  public static Session session()
  {
    Session sess = new Session();
    sess.setOffset60DB(random.nextInt(100));
    sess.setCalibration(random.nextInt(5));
    sess.setStart(new Date());
    sess.setEnd(new Date());

    return sess;
  }

  public static MeasurementStream stream()
  {
    MeasurementStream stream = new MeasurementStream("sensor0",
                                                     "type1",
                                                     "unit2",
                                                     "symbol3", 1, 2, 3, 4, 5);


    return stream;
  }
}
