package pl.llp.aircasting.util;

import android.util.Log;

public class Constants
{
  public static final int MILLIS_IN_SECOND = 1000;

  public static final int ONE_SECOND = MILLIS_IN_SECOND;
  public static final int THREE_SECONDS = 3 * MILLIS_IN_SECOND;

  public static final long ONE_MINUTE = 60 * ONE_SECOND;

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
      Log.v(Constants.TAG, message);
    }
  }

  public static void logGraphPerformance(String message)
  {
    if(logGraphPerformance())
    {
      Log.i(Constants.TAG, message);
    }
  }
}
