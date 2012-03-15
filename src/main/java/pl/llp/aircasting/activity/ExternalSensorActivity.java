package pl.llp.aircasting.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;
import com.google.inject.Inject;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.adapter.SensorAdapter;
import pl.llp.aircasting.activity.adapter.SensorAdapterFactory;
import roboguice.inject.InjectView;

public class ExternalSensorActivity extends DialogActivity {
    @Inject Context context;
    @Inject SensorAdapterFactory adapterFactory;

    @InjectView(R.id.sensor_list) ListView sensorList;

    BluetoothAdapter bluetoothAdapter;
    BroadcastReceiver receiver = new BluetoothFoundReceiver();
    SensorAdapter sensorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.external_sensor_list);

        sensorAdapter = adapterFactory.getAdapter(this);
        sensorList.setAdapter(sensorAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(context, R.string.bluetooth_not_supported, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        if (!bluetoothAdapter.isEnabled()) {
            Intents.requestEnableBluetooth(this);
        } else {
            findDevices();
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

    private class BluetoothFoundReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            sensorAdapter.deviceFound(device);
        }
    }
}
