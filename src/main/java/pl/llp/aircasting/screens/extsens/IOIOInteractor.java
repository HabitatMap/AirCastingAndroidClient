package pl.llp.aircasting.screens.extsens;

import pl.llp.aircasting.Intents;
import pl.llp.aircasting.screens.common.helpers.SettingsHelper;
import pl.llp.aircasting.sensor.common.ExternalSensorDescriptor;

import android.content.Context;

import java.util.List;

class IOIOInteractor {
    public void startIfNecessary(ExternalSensorDescriptor descriptor, Context context) {
        if (descriptor.getName().startsWith("IOIO")) {
            Intents.startIOIO(context);
        }
    }

    void startPreviouslyConnectedIOIO(SettingsHelper settings, Context context) {
        List<ExternalSensorDescriptor> descriptors = settings.knownSensors();
        for (ExternalSensorDescriptor descriptor : descriptors) {
            startIfNecessary(descriptor, context);
        }
    }

    public void stopIfNecessary(ExternalSensorDescriptor disconnected, Context context) {
        if (disconnected.getName().startsWith("IOIO")) {
            Intents.stopIOIO(context);
        }
    }
}
