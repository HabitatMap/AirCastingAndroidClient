package pl.llp.aircasting.activity.extsens;

import pl.llp.aircasting.Intents;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.ExternalSensorDescriptor;

import android.content.Context;

import java.util.List;

/**
 * Created by ags on 26/07/12 at 16:51
 */
class IOIOInteractor
{
  Context context;
  SettingsHelper settings;

  IOIOInteractor(Context context, SettingsHelper settings)
  {
    this.context = context;
    this.settings = settings;
  }

  public void startIfNecessary(ExternalSensorDescriptor descriptor)
  {
    if(descriptor.getName().startsWith("IOIO"))
    {
      Intents.startIOIO(context);
    }
  }

  void startPreviouslyConnectedIOIO()
  {
    List<ExternalSensorDescriptor> descriptors = settings.knownSensors();
    for (ExternalSensorDescriptor descriptor : descriptors)
    {
      startIfNecessary(descriptor);
    }
  }

  public void stopIfNecessary(ExternalSensorDescriptor disconnected)
  {
    if (disconnected.getName().startsWith("IOIO"))
    {
      Intents.stopIOIO(context);
    }
  }
}
