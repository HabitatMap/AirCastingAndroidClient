package pl.llp.aircasting.util;

import java.util.concurrent.TimeUnit;

public class Constants
{
  public static final int MILLIS_IN_SECOND = 1000;

  public static final long ONE_SECOND = TimeUnit.SECONDS.toMillis(1);
  public static final long THREE_SECONDS = TimeUnit.SECONDS.toMillis(3);

  public static final long ONE_MINUTE = TimeUnit.MINUTES.toMillis(1);
  public static final long TWO_MINUTES = TimeUnit.MINUTES.toMillis(2);

  public static final long CURRENT_SESSION_FAKE_ID = -1;

  public static final String TAG = "AirCasting";
  public static final String PERFORMANCE_TAG = "AirCasting/Performance";
  public static final String DB_PERFORMANCE_TAG = "AirCasting/Performance/Db";
  public static final String SENSORS_TAG = "AirCasting/Sensors";

  public static boolean isDevMode()
  {
    return false;
  }

  public static boolean logDbPerformance()
  {
    return false;
  }

  public static boolean logGraphPerformance()
  {
    return false;
  }
}
