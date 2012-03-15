package pl.llp.aircasting.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;
import com.google.inject.Inject;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;

import javax.annotation.Nullable;

public class ExternalSensorActivity extends DialogActivity {
    @Nullable @Inject BluetoothAdapter bluetoothAdapter;
    @Inject Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(bluetoothAdapter == null){
            Toast.makeText(context, R.string.bluetooth_not_supported, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if(!bluetoothAdapter.isEnabled()){
            Intents.requestEnableBluetooth(this);
        }
    }
}
