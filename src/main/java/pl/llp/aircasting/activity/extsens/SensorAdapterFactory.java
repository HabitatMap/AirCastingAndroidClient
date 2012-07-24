package pl.llp.aircasting.activity.extsens;

import pl.llp.aircasting.helper.SettingsHelper;

import android.app.Activity;
import com.google.inject.Singleton;

@Singleton
public class SensorAdapterFactory
{
  public AvailableSensorAdapter getAvailabelSensorAdapter(Activity context)
  {
    return new AvailableSensorAdapter(context);
  }

  public KnownSensorAdapter getKnownSensorAdapter(Activity context, SettingsHelper settingsHelper)
  {
    return new KnownSensorAdapter(context, settingsHelper);
  }
}