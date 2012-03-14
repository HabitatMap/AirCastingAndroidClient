package pl.llp.aircasting.guice;

import android.bluetooth.BluetoothAdapter;
import com.google.inject.Provider;

public class BluetoothAdapterProvider implements Provider<BluetoothAdapter> {
    @Override
    public BluetoothAdapter get() {
        return BluetoothAdapter.getDefaultAdapter();
    }
}
