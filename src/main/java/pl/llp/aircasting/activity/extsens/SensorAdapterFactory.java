package pl.llp.aircasting.activity.extsens;

import pl.llp.aircasting.helper.SettingsHelper;

import android.app.Activity;
import com.google.inject.Singleton;

@Singleton
public class SensorAdapterFactory
{
  public PairedSensorAdapter getPairedSensorAdapter(Activity context)
  {
    return new PairedSensorAdapter(context);
  }

  public ConnectedSensorAdapter getConnectedSensorAdapter(Activity context, SettingsHelper settingsHelper)
  {
    return new ConnectedSensorAdapter(context, settingsHelper);
  }
}