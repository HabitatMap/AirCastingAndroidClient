package pl.llp.aircasting.util;

import android.util.Log;

public class Constants
{
  public static final int THREE_SECONDS = 3000;

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
    return true;
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
