package pl.llp.aircasting.activity.extsens;

import pl.llp.aircasting.R;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.ExternalSensorDescriptor;

import android.bluetooth.BluetoothDevice;
import android.view.View;

import java.util.List;
import java.util.Map;

class AdapterInteractor
{
  private ExternalSensorActivity parent;
  private ConnectedSensorAdapter connectedSensorAdapter;
  private PairedSensorAdapter pairedSensorAdapter;
  private SettingsHelper settingsHelper;

  AdapterInteractor(ExternalSensorActivity parent, PairedSensorAdapter pairedSensorAdapter, ConnectedSensorAdapter connectedSensorAdapter, SettingsHelper settingsHelper)
  {
    this.parent = parent;
    this.pairedSensorAdapter = pairedSensorAdapter;
    this.connectedSensorAdapter = connectedSensorAdapter;
    this.settingsHelper = settingsHelper;
  }

  void updateKnownSensorListVisibility()
  {
    List<ExternalSensorDescriptor> descriptors = settingsHelper.knownSensors();
    connectedSensorAdapter.updatePreviouslyConnected(descriptors);
    for (ExternalSensorDescriptor descriptor : descriptors)
    {
      pairedSensorAdapter.markAsConnected(descriptor);
    }

    if(pairedSensorAdapter.getCount() > 0)
    {
      parent.findViewById(R.id.paired_sensor_list).setVisibility(View.VISIBLE);
      parent.findViewById(R.id.paired_sensors_label).setVisibility(View.VISIBLE);
    }
    else
    {
      parent.findViewById(R.id.paired_sensor_list).setVisibility(View.GONE);
      parent.findViewById(R.id.paired_sensors_label).setVisibility(View.GONE);
    }
    if (connectedSensorAdapter.getCount() > 0)
    {
      parent.findViewById(R.id.connected_sensors_label).setVisibility(View.VISIBLE);
      parent.findViewById(R.id.connected_sensors_list).setVisibility(View.VISIBLE);
    }
    else
    {
      parent.findViewById(R.id.connected_sensors_label).setVisibility(View.GONE);
      parent.findViewById(R.id.connected_sensors_list).setVisibility(View.GONE);
    }
  }

  public ExternalSensorDescriptor connectToActive(int position)
  {
    ExternalSensorDescriptor descriptor = pairedSensorAdapter.get(position);

    pairedSensorAdapter.markAsConnected(position);
    connectedSensorAdapter.addSensor(descriptor);

    return descriptor;
  }

  public ExternalSensorDescriptor disconnect(int position)
  {
    Map<String, String> removed = connectedSensorAdapter.remove(position);
    ExternalSensorDescriptor sensor = ExternalSensorDescriptor.from(removed);
    pairedSensorAdapter.connectionFailedWith(sensor.getAddress());
    updateKnownSensorListVisibility();
    return sensor;
  }

  public boolean knows(BluetoothDevice device)
  {
    return connectedSensorAdapter.knows(device.getAddress());
  }
}
