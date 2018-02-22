package pl.llp.aircasting.activity.extsens;

import android.os.Handler;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.ChooseSessionTypeActivity;
import pl.llp.aircasting.activity.ChooseStreamingMethodActivity;
import pl.llp.aircasting.activity.DialogActivity;
import pl.llp.aircasting.event.ConnectionUnsuccessfulEvent;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.helper.ToastHelper;
import pl.llp.aircasting.sensor.ExternalSensorDescriptor;
import pl.llp.aircasting.sensor.airbeam.Airbeam2Configurator;
import pl.llp.aircasting.sensor.external.ExternalSensors;
import pl.llp.aircasting.util.Constants;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import roboguice.inject.InjectView;

import java.util.UUID;

import static pl.llp.aircasting.Intents.CONTINUE_STREAMING;

public class ExternalSensorActivity extends DialogActivity {
    @Inject Context context;
    @Inject SensorAdapterFactory adapterFactory;
    @Inject SettingsHelper settingsHelper;
    @Inject ExternalSensors externalSensors;
    @Inject EventBus eventBus;
    @Inject Airbeam2Configurator airbeam2Configurator;

    @InjectView(R.id.paired_sensor_list) ListView pairedSensorList;
    @InjectView(R.id.connected_sensors_list) ListView connectedSensorList;
    @InjectView((R.id.pair_with_new_sensors_button)) Button openBluetoothButton;

    BluetoothAdapter bluetoothAdapter;
    PairedSensorAdapter pairedSensorAdapter;
    ConnectedSensorAdapter connectedSensorAdapter;

    AdapterInteractor sensorLists;
    IOIOInteractor ioio = new IOIOInteractor();

    private long bluetoothRequestTimestamp;
    private boolean configurationRequired;
    private boolean continueStreaming;
    private long sleepTime = 4000;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.external_sensors_list);
        eventBus.register(this);
        configurationRequired = getIntent().getBooleanExtra(Intents.CONFIGURATION_REQUIRED, false);
        continueStreaming = getIntent().getBooleanExtra(CONTINUE_STREAMING, false);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        pairedSensorAdapter = adapterFactory.getPairedSensorAdapter(this);
        connectedSensorAdapter = adapterFactory.getConnectedSensorAdapter(this, settingsHelper);

        pairedSensorList.setAdapter(pairedSensorAdapter);
        pairedSensorList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final ExternalSensorDescriptor connected = sensorLists.connectToActive(position);

                if (continueStreaming) {
                    ToastHelper.show(context, R.string.configuring_airbeam, (int) sleepTime);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            String uuid = getIntent().getStringExtra("uuid");
                            airbeam2Configurator.sendUUIDAndAuthToken(UUID.fromString(uuid), settingsHelper.getAuthToken());;
                            Intent intent = new Intent(context, ChooseStreamingMethodActivity.class);
                            intent.putExtra(CONTINUE_STREAMING, true);

                            startActivity(intent);
                        }
                    }, sleepTime);

                    finish();
                } else if (configurationRequired && connected.getName().startsWith(ExternalSensors.AIRBEAM)) {
                    ToastHelper.show(context, R.string.configuring_airbeam, (int) sleepTime);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(context, ChooseSessionTypeActivity.class));
                        }
                    }, sleepTime);

                    finish();
                } else {
                    ToastHelper.show(context, R.string.connecting_external_device, Toast.LENGTH_SHORT);

                    ioio.startIfNecessary(connected, context);
                    Intents.restartSensors(context);

                    Intents.startDashboardActivity(ExternalSensorActivity.this, true);
                }
            }
        });

        connectedSensorList.setAdapter(connectedSensorAdapter);
        connectedSensorList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                disconnectFrom(sensorLists.disconnect(position));
            }
        });

        sensorLists = new AdapterInteractor(this, pairedSensorAdapter, connectedSensorAdapter, settingsHelper);
    }

    private void disconnectFrom(final ExternalSensorDescriptor disconnected) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int pos = connectedSensorAdapter.findPosition(disconnected);
                if (pos > -1) {
                    sensorLists.disconnect(pos);
                }
                externalSensors.disconnect(disconnected.getAddress());

                Intents.restartSensors(context);
                ioio.stopIfNecessary(disconnected, context);
            }
        });
    }

    @Subscribe
    public void onEvent(ConnectionUnsuccessfulEvent event) {
        disconnectFrom(new ExternalSensorDescriptor(event.getDevice()));
    }

    @Override
    protected void onResume() {
        super.onResume();

        eventBus.register(this);

        if (bluetoothAdapter == null) {
            ToastHelper.show(context, R.string.bluetooth_not_supported, Toast.LENGTH_LONG);
            finish();
            return;
        }

        sensorLists.updateKnownSensorListVisibility();
        ioio.startPreviouslyConnectedIOIO(settingsHelper, context);

        openBluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.startDiscovery();
                }
                Intent intent = new Intent();
                intent.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                context.startActivity(intent);
            }
        });

        if (!bluetoothAdapter.isEnabled()) {
            long now = System.currentTimeMillis();
            if (now - bluetoothRequestTimestamp > Constants.ONE_SECOND) {
                Intents.requestEnableBluetooth(this);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Intents.REQUEST_ENABLE_BLUETOOTH) {
            bluetoothRequestTimestamp = System.currentTimeMillis();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        openBluetoothButton.setOnClickListener(null);
        eventBus.unregister(this);
    }
}

