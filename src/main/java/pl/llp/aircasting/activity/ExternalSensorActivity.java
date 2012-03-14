package pl.llp.aircasting.activity;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import com.google.inject.Inject;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;

public class ExternalSensorActivity extends DialogActivity {
    @Inject BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor);
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        if(!bluetoothAdapter.isEnabled()){
            Intents.requestEnableBluetooth(this);
        }
    }
}
