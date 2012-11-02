package pl.llp.aircasting.activity.extsens;

import pl.llp.aircasting.Intents;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.sensor.ExternalSensorDescriptor;

import android.content.Context;

import java.util.List;

class IOIOInteractor
{
  public void startIfNecessary(ExternalSensorDescriptor descriptor, Context context)
  {
    if(descriptor.getName().startsWith("IOIO"))
    {
      Intents.startIOIO(context);
    }
  }

  void startPreviouslyConnectedIOIO(SettingsHelper settings, Context context)
  {
    List<ExternalSensorDescriptor> descriptors = settings.knownSensors();
    for (ExternalSensorDescriptor descriptor : descriptors)
    {
      startIfNecessary(descriptor, context);
    }
  }

  public void stopIfNecessary(ExternalSensorDescriptor disconnected, Context context)
  {
    if (disconnected.getName().startsWith("IOIO"))
    {
      Intents.stopIOIO(context);
    }
  }
}
