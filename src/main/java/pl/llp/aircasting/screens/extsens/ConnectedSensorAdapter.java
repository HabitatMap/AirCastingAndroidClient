package pl.llp.aircasting.screens.extsens;

import pl.llp.aircasting.screens.common.helpers.SettingsHelper;
import pl.llp.aircasting.sensor.common.ExternalSensorDescriptor;

import android.content.Context;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

public class ConnectedSensorAdapter extends SensorAdapter {
    SettingsHelper settingsHelper;

    ConnectedSensorAdapter(Context context, SettingsHelper settingsHelper) {
        super(context);
        this.settingsHelper = settingsHelper;
    }

    public boolean knows(String address) {
        return containsAddress(address);
    }

    private boolean containsAddress(String address) {
        for (Map<String, String> keyValues : data) {
            if (address.equalsIgnoreCase(keyValues.get(ADDRESS))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Map<String, String> remove(int position) {
        Map<String, String> removed = super.remove(position);
        notifyDataSetChanged();
        updateSettings();
        return removed;
    }

    void updateSettings() {
        Iterator<Map<String, String>> iterator = data.iterator();
        List<ExternalSensorDescriptor> sensors = newArrayList();
        while (iterator.hasNext()) {
            Map<String, String> next = iterator.next();
            String address = next.get(ADDRESS);
            String name = next.get(NAME);
            String action = next.get(ACTION);
            sensors.add(sensor(name, address, action));
        }
        settingsHelper.setExternalSensors(sensors);
    }

    public void addSensor(ExternalSensorDescriptor sensor) {
        if (!knows(sensor.getAddress())) {
            data.add(sensor.asMap());
        }
        notifyDataSetChanged();
        updateSettings();
    }

    ExternalSensorDescriptor sensor(String name, String address, String action) {
        return new ExternalSensorDescriptor(name, address, action);
    }

    public void updatePreviouslyConnected(List<ExternalSensorDescriptor> descriptors) {
        for (ExternalSensorDescriptor sensor : descriptors) {
            if (!containsAddress(sensor.getAddress())) {
                Map<String, String> item = newHashMap();
                item.put(ADDRESS, sensor.getAddress());
                item.put(NAME, sensor.getName());
                item.put(ACTION, sensor.getAction());

                data.add(item);
            }
        }

        notifyDataSetChanged();
    }

    public void disconnect() {
        throw new RuntimeException("Not implemented yet");
    }

}


