package pl.llp.aircasting.activity.extsens;

import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.DialogActivity;
import pl.llp.aircasting.helper.NoOp;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.ExternalSensorDescriptor;
import pl.llp.aircasting.sensor.external.ExternalSensors;
import pl.llp.aircasting.util.Constants;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.IBluetooth;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.google.inject.Inject;
import roboguice.inject.InjectView;

import java.lang.reflect.Method;

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

    AdapterInteractor sensorLists;
    IOIOInteractor ioio = new IOIOInteractor();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.external_sensors_list);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        knownSensorAdapter = adapterFactory.getKnownSensorAdapter(this, settingsHelper);
        availableSensorAdapter = adapterFactory.getAvailabelSensorAdapter(this);

        availableSensorList.setAdapter(availableSensorAdapter);
        availableSensorList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id)
          {
            ExternalSensorDescriptor connected = sensorLists.connectToActive(position);
            String address = connected.getAddress();
            try
            {
              IBluetooth iBluetooth = getIBluetooth();
              if(bluetoothAdapter.getBondedDevices().contains(address))
              {

              }
              else
              {
                iBluetooth.createBond(address);
              }
            }
            catch (Exception e)
            {
                Log.e(Constants.TAG, "Failed to request pairing!", e);
            }
            ioio.startIfNecessary(connected, context);

            Intents.restartSensors(context);
            Intents.startStreamsActivity(ExternalSensorActivity.this);
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
                    ExternalSensorDescriptor disconnected = sensorLists.disconnect(position);
                    externalSensors.disconnect(disconnected.getAddress());

                    Intents.restartSensors(context);
                    ioio.stopIfNecessary(disconnected, context);
                  }
                }).setNegativeButton("No", NoOp.dialogOnClick());
            AlertDialog dialog = builder.create();
            dialog.show();
          }
        });

      sensorLists = new AdapterInteractor(this, knownSensorAdapter, availableSensorAdapter, settingsHelper);
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
        registerReceiver(receiver, filter);

        sensorLists.updateKnownSensorListVisibility();
        ioio.startPreviouslyConnectedIOIO(settingsHelper, context);

      if (bluetoothAdapter.isEnabled())
      {
          findDevices();
      }
      else
      {
          Intents.requestEnableBluetooth(this);
      }
    }

  private IBluetooth getIBluetooth()
  {
    IBluetooth ibt = null;

    try
    {
      Class c2 = Class.forName("android.os.ServiceManager");

      Method m2 = c2.getDeclaredMethod("getService", String.class);
      IBinder b = (IBinder) m2.invoke(null, "bluetooth");

      Class c3 = Class.forName("android.bluetooth.IBluetooth");

      Class[] s2 = c3.getDeclaredClasses();

      Class c = s2[0];
      Method m = c.getDeclaredMethod("asInterface", IBinder.class);
      m.setAccessible(true);
      ibt = (IBluetooth) m.invoke(null, b);
    }
    catch (Exception e)
    {
      Log.e("flowlab", "Erroraco!!! " + e.getMessage());
    }

    return ibt;
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

    private void findDevices()
    {
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
          if(!sensorLists.knows(device))
          {
            availableSensorAdapter.deviceFound(device);
          }
        }
      });
    }
  }
}

