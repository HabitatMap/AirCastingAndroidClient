package android.bluetooth;

/**
 * System private API for Bluetooth service callbacks.
 *
 * {@hide}
 */
interface IBluetoothCallback
{
    void onRfcommChannelFound(int channel);
}