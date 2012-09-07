package pl.llp.aircasting.activity.extsens;

import pl.llp.aircasting.R;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.ExternalSensorDescriptor;

import android.bluetooth.BluetoothDevice;
import android.view.View;

import java.util.List;
import java.util.Map;

/**
 * Created by ags on 26/07/12 at 16:51
 */
class AdapterInteractor
{
  private ExternalSensorActivity parent;
  private KnownSensorAdapter knownSensorAdapter;
  private AvailableSensorAdapter availableSensorAdapter;
  private SettingsHelper settingsHelper;

  AdapterInteractor(ExternalSensorActivity parent, KnownSensorAdapter knownSensorAdapter, AvailableSensorAdapter availableSensorAdapter, SettingsHelper settingsHelper)
  {
    this.parent = parent;
    this.knownSensorAdapter = knownSensorAdapter;
    this.availableSensorAdapter = availableSensorAdapter;
    this.settingsHelper = settingsHelper;
  }

  void updateKnownSensorListVisibility()
  {
    List<ExternalSensorDescriptor> descriptors = settingsHelper.knownSensors();
    knownSensorAdapter.updatePreviouslyConnected(descriptors);
    if (knownSensorAdapter.getCount() > 0)
    {
      parent.findViewById(R.id.connected_sensor_label).setVisibility(View.VISIBLE);
      parent.findViewById(R.id.connected_sensor_list).setVisibility(View.VISIBLE);
    }
    else
    {
      parent.findViewById(R.id.connected_sensor_label).setVisibility(View.GONE);
      parent.findViewById(R.id.connected_sensor_list).setVisibility(View.GONE);
    }
  }

  public ExternalSensorDescriptor connectToActive(int position)
  {
    ExternalSensorDescriptor descriptor = availableSensorAdapter.get(position);

    availableSensorAdapter.remove(position);
    knownSensorAdapter.addSensor(descriptor);

    return descriptor;
  }

  public ExternalSensorDescriptor disconnect(int position)
  {
    Map<String, String> removed = knownSensorAdapter.remove(position);
    ExternalSensorDescriptor sensor = ExternalSensorDescriptor.from(removed);
    updateKnownSensorListVisibility();
    return sensor;
  }

  public boolean knows(BluetoothDevice device)
  {
    return knownSensorAdapter.knows(device.getAddress());
  }
}
