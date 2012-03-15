package pl.llp.aircasting.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import com.google.inject.Inject;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import roboguice.inject.InjectView;

public class ExternalSensorActivity extends DialogActivity {
    @Inject Context context;
    @InjectView(R.id.info) TextView info;

    BluetoothAdapter bluetoothAdapter;
    BroadcastReceiver receiver = new BluetoothFoundReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor);
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

    private void deviceFound(BluetoothDevice device) {
        info.setText(info.getText() + "\n" + device.getAddress());
    }

    private class BluetoothFoundReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            deviceFound(device);
        }
    }
}
