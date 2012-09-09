package android.bluetooth;

import android.bluetooth.IBluetoothCallback;
import android.os.ParcelUuid;

/**
  * System private API for talking with the Bluetooth service.
  *
  * {@hide}
  */
 interface IBluetooth
 {
   boolean isEnabled();
   int getBluetoothState();
   boolean enable();
   boolean disable(boolean persistSetting);

   String getAddress();
   String getName();
   boolean setName(in String name);

   int getScanMode();
   boolean setScanMode(int mode, int duration);

   int getDiscoverableTimeout();
   boolean setDiscoverableTimeout(int timeout);

   boolean startDiscovery();
   boolean cancelDiscovery();
   boolean isDiscovering();

   boolean createBond(in String address);
   boolean cancelBondProcess(in String address);
   boolean removeBond(in String address);
   String[] listBonds();
   int getBondState(in String address);

   String getRemoteName(in String address);
   int getRemoteClass(in String address);
   ParcelUuid[] getRemoteUuids(in String address);
   boolean fetchRemoteUuids(in String address, in ParcelUuid uuid, in IBluetoothCallback callback);
   int getRemoteServiceChannel(in String address, in ParcelUuid uuid);

   boolean setPin(in String address, in byte[] pin);
   boolean setPasskey(in String address, int passkey);
   boolean setPairingConfirmation(in String address, boolean confirm);
   boolean cancelPairingUserInput(in String address);

   boolean setTrust(in String address, in boolean value);
   boolean getTrustState(in String address);

   int addRfcommServiceRecord(in String serviceName, in ParcelUuid uuid, int channel, IBinder b);
   void removeServiceRecord(int handle);
}