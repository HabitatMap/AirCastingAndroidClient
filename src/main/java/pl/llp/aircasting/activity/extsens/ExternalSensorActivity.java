package pl.llp.aircasting.activity.extsens;

import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.DialogActivity;
import pl.llp.aircasting.helper.NoOpOnClickListener;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.ExternalSensorDescriptor;
import pl.llp.aircasting.sensor.external.ExternalSensors;
import pl.llp.aircasting.util.Constants;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import roboguice.inject.InjectView;

import java.util.List;
import java.util.Map;

public class ExternalSensorActivity extends DialogActivity
{
    @Inject Context context;
    @Inject SensorAdapterFactory adapterFactory;
    @Inject SettingsHelper settingsHelper;
    @Inject ExternalSensors externalSensors;

    @InjectView(R.id.external_sensor_list) ListView availableSensorList;
    @InjectView(R.id.connected_sensor_list) ListView connectedSensorList;

    BluetoothAdapter bluetoothAdapter;
    BroadcastReceiver receiver = new BluetoothFoundReceiver();
    AvailableSensorAdapter availableSensorAdapter;
    KnownSensorAdapter knownSensorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.external_sensors_list);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        final ExternalSensorActivity a = this;

        knownSensorAdapter = adapterFactory.getKnownSensorAdapter(this, settingsHelper);
        availableSensorAdapter = adapterFactory.getAvailabelSensorAdapter(this);

        availableSensorList.setAdapter(availableSensorAdapter);
        availableSensorList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id)
          {
            String address = availableSensorAdapter.getAddress(position);
            String name = availableSensorAdapter.getName(position);
            if(name.startsWith("IOIO"))
            {
              Intents.startIOIO(context);
            }

            availableSensorAdapter.remove(position);
            knownSensorAdapter.addSensor(Strings.nullToEmpty(name), address);

            Intents.restartSensors(context);
            Intents.startStreamsActivity(a);
          }
        });

        connectedSensorList.setAdapter(knownSensorAdapter);
        connectedSensorList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
          {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(R.string.disconnect_sensor).
            setCancelable(true).
                setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                  @Override
                  public void onClick(DialogInterface dialog, int which)
                  {
                    Map<String, String> removed = knownSensorAdapter.remove(position);
                    externalSensors.disconnect(removed.get(SensorAdapter.ADDRESS));
                    showPreviouslyConnectedSensor();

                    Intents.restartSensors(context);
                    if (removed.get(SensorAdapter.NAME).startsWith("IOIO"))
                    {
                      Intents.stopIOIO(context);
                    }
                  }
                }).setNegativeButton("No", new NoOpOnClickListener());
            AlertDialog dialog = builder.create();
            dialog.show();
          }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (bluetoothAdapter == null) {
            Toast.makeText(context, R.string.bluetooth_not_supported, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        showPreviouslyConnectedSensor();
        registerReceiver(receiver, filter);

        if (!bluetoothAdapter.isEnabled()) {
            Intents.requestEnableBluetooth(this);
        } else {
            findDevices();
        }
    }

  void showPreviouslyConnectedSensor()
  {
    List<ExternalSensorDescriptor> descriptors = settingsHelper.sensorsFromSettings();
    knownSensorAdapter.updatePreviouslyConnected(descriptors);
    if (knownSensorAdapter.getCount() > 0)
    {
      findViewById(R.id.connected_sensor_label).setVisibility(View.VISIBLE);
      findViewById(R.id.connected_sensor_list).setVisibility(View.VISIBLE);
    }
    else
    {
      findViewById(R.id.connected_sensor_label).setVisibility(View.GONE);
      findViewById(R.id.connected_sensor_list).setVisibility(View.GONE);
    }
    for (ExternalSensorDescriptor descriptor : descriptors)
    {
      if(descriptor.getName().startsWith("IOIO"))
      {
        Intents.startIOIO(context);
      }
    }
  }

  @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        if (bluetoothAdapter != null) {
            bluetoothAdapter.cancelDiscovery();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Intents.REQUEST_ENABLE_BLUETOOTH && resultCode == RESULT_OK) {
            findDevices();
        } else {
            finish();
        }
    }

    private void findDevices() {
        bluetoothAdapter.startDiscovery();
    }

  private class BluetoothFoundReceiver extends BroadcastReceiver
  {
    @Override
    public void onReceive(Context context, Intent intent) {
      final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
      runOnUiThread(new Runnable()
      {
        @Override
        public void run()
        {
          if(knownSensorAdapter.knows(device.getAddress()))
          {
            return;
          }
          availableSensorAdapter.deviceFound(device);
          if(Constants.isDevMode())
          {
            Log.e(Constants.TAG, "", new Throwable());
          }
        }
      });
    }
  }
}
