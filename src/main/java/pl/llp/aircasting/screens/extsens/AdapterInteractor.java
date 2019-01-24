package pl.llp.aircasting.screens.extsens;

import android.widget.ListView;
import pl.llp.aircasting.R;
import pl.llp.aircasting.screens.common.helpers.SettingsHelper;
import pl.llp.aircasting.sensor.common.ExternalSensorDescriptor;

import android.view.View;

import java.util.List;
import java.util.Map;

class AdapterInteractor {
    private ExternalSensorActivity parent;
    private ConnectedSensorAdapter connectedSensorAdapter;
    private PairedSensorAdapter pairedSensorAdapter;
    private SettingsHelper settingsHelper;

    AdapterInteractor(ExternalSensorActivity parent, PairedSensorAdapter pairedSensorAdapter, ConnectedSensorAdapter connectedSensorAdapter, SettingsHelper settingsHelper) {
        this.parent = parent;
        this.pairedSensorAdapter = pairedSensorAdapter;
        this.connectedSensorAdapter = connectedSensorAdapter;
        this.settingsHelper = settingsHelper;
    }

    void updateKnownSensorListVisibility() {
        List<ExternalSensorDescriptor> descriptors = settingsHelper.knownSensors();
        connectedSensorAdapter.updatePreviouslyConnected(descriptors);
        for (ExternalSensorDescriptor descriptor : descriptors) {
            pairedSensorAdapter.markAsConnected(descriptor);
        }

        pairedSensorAdapter.updateIfNecessary();
        if (pairedSensorAdapter.getCount() > 0) {
            parent.findViewById(R.id.paired_sensor_list).setVisibility(View.VISIBLE);
        } else {
            parent.findViewById(R.id.paired_sensor_list).setVisibility(View.GONE);
        }
        if (connectedSensorAdapter.getCount() > 0) {
            ListView lv = (ListView) parent.findViewById(R.id.connected_sensors_list);
            lv.addFooterView(new View(parent), null, true);
            lv.setVisibility(View.VISIBLE);

            parent.findViewById(R.id.connected_sensors_list).setVisibility(View.VISIBLE);
        } else {
            parent.findViewById(R.id.connected_sensors_list).setVisibility(View.GONE);
        }
    }

    public ExternalSensorDescriptor connectToActive(int position) {
        ExternalSensorDescriptor descriptor = pairedSensorAdapter.get(position);
        descriptor.setAction("disconnect");

        pairedSensorAdapter.markAsConnected(position);
        connectedSensorAdapter.addSensor(descriptor);

        return descriptor;
    }

    public ExternalSensorDescriptor disconnect(int position) {
        Map<String, String> removed = connectedSensorAdapter.remove(position);
        ExternalSensorDescriptor sensor = ExternalSensorDescriptor.from(removed);
        pairedSensorAdapter.connectionFailedWith(sensor.getAddress());
        updateKnownSensorListVisibility();
        return sensor;
    }
}
