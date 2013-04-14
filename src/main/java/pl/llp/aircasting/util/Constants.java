package pl.llp.aircasting.util;

import pl.llp.aircasting.android.Logger;

import java.util.concurrent.TimeUnit;

public class Constants
{
  public static final int MILLIS_IN_SECOND = 1000;

  public static final long ONE_SECOND = TimeUnit.SECONDS.toMillis(1);
  public static final long THREE_SECONDS = TimeUnit.SECONDS.toMillis(3);

  public static final long ONE_MINUTE = TimeUnit.MINUTES.toMillis(1);
  public static final long TWO_MINUTES = TimeUnit.MINUTES.toMillis(2);

  public static final String TAG = "AirCasting";

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

  public static void logDbPerformance(String message)
  {
    if(logDbPerformance())
    {
      Logger.i(message);
    }
  }

  public static void logGraphPerformance(String message)
  {
    if(logGraphPerformance())
    {
      Logger.i(message);
    }
  }
}
