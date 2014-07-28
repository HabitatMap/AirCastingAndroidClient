package pl.llp.aircasting.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.adapter.StreamAdapter;
import pl.llp.aircasting.activity.adapter.StreamAdapterFactory;
import pl.llp.aircasting.activity.listener.OnSensorDragListener;
import pl.llp.aircasting.api.RegressionDriver;
import pl.llp.aircasting.api.SessionDriver;
import pl.llp.aircasting.api.data.CreateRegressionResponse;
import pl.llp.aircasting.event.ui.DoubleTapEvent;
import pl.llp.aircasting.event.ui.LongClickEvent;
import pl.llp.aircasting.event.ui.TapEvent;
import pl.llp.aircasting.event.ui.ViewStreamEvent;
import pl.llp.aircasting.model.*;
import pl.llp.aircasting.storage.repository.RegressionRepository;
import pl.llp.aircasting.util.http.HttpResult;
import pl.llp.aircasting.util.http.Status;
import pl.llp.aircasting.view.SensorsGridView;
import roboguice.inject.InjectView;

import static pl.llp.aircasting.Intents.startSensors;
import static pl.llp.aircasting.Intents.stopSensors;
import static pl.llp.aircasting.Intents.triggerSync;

public class StreamsActivity extends ButtonsActivity {
    @Inject Context context;
    @Inject StreamAdapterFactory adapterFactory;
    @Inject SensorManager sensorManager;
    @Inject SessionManager sessionManager;
    @Inject RegressionDriver regressionDriver;
    @Inject RegressionRepository regressionRepository;

    @InjectView(R.id.sensors_grid) SensorsGridView gridView;
    @InjectView(R.id.heat_map_button_container) View mapContainer;
    @InjectView(R.id.heat_map_button) View mapButton;
    @InjectView(R.id.graph_button_container) View graphContainer;
    @InjectView(R.id.graph_button) View graphButton;

    StreamAdapter adapter;
    boolean selectingReference = false;
    boolean selectingTarget = false;
    Sensor reference = null;
    Sensor target = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intents.startDatabaseWriterService(context);

        setContentView(R.layout.streams);

        adapter = adapterFactory.getAdapter(this);
        gridView.setAdapter(adapter);

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                v.vibrate(50);
                gridView.enableDrag();
                return false;
            }
        });

        gridView.setOnItemDoubleClickListener(new SensorsGridView.OnItemDoubleClickListener() {
            @Override
            public void onItemDoubleClick(AdapterView<?> parent, View view, int position, long id) {
                if (sensorManager.isSessionBeingRecorded()) {
                    sensorManager.toggleSensor((Sensor) view.getTag());
                }
            }
        });

        gridView.setOnItemSingleTapListener(new SensorsGridView.OnItemSingleTapListener() {
            @Override
            public void onItemSingleTap(AdapterView<?> parent, View view, int position, long id) {
                if (selectingReference) {
                    reference = (Sensor) view.getTag();
                    selectingReference = false;
                    showDialog(R.string.calibration_select_target, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            selectingTarget = true;
                        }
                    });
                } else if (selectingTarget) {
                    target = (Sensor) view.getTag();
                    selectingTarget = false;
                    sessionManager.disableCalibration();
                    triggerCalibration();
                } else {
                    if (sensorManager.isSessionBeingViewed())
                        return;
                    adapter.toggleStatsVisibility((Sensor) view.getTag());
                }
            }
        });

        SensorsGridView.OnDragListener graphListener = new OnSensorDragListener(gridView, graphButton, graphContainer) {
            @Override
            public void onDrop(View view) {
                eventBus.post(new ViewStreamEvent((Sensor) view.getTag()));
                context.startActivity(new Intent(StreamsActivity.this, GraphActivity.class));
            }
        };

        SensorsGridView.OnDragListener mapListener = new OnSensorDragListener(gridView, mapButton, mapContainer) {
            @Override
            public void onDrop(View view) {
                if (sessionManager.isLocationless()) {
                    Toast.makeText(StreamsActivity.this, R.string.cant_map_without_gps, Toast.LENGTH_SHORT).show();
                } else {
                    eventBus.post(new ViewStreamEvent((Sensor) view.getTag()));
                    context.startActivity(new Intent(StreamsActivity.this, AirCastingMapActivity.class));
                }
            }
        };

        gridView.registerListenArea((ViewGroup) findViewById(R.id.buttons), R.id.graph_button_container, graphListener);
        gridView.registerListenArea((ViewGroup) findViewById(R.id.buttons), R.id.heat_map_button_container, mapListener);

        if (sessionManager.isCalibrating()) {
            showDialog(R.string.calibration_select_reference, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    selectingReference = true;
                }
            });
        }
    }

    private void showDialog(int messageId, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(this)
                .setMessage(messageId)
                .setPositiveButton(R.string.ok, listener)
                .setCancelable(false)
                .create()
                .show();
    }

    private void triggerCalibration() {
        new AlertDialog.Builder(this)
                .setMessage(target.getPackageName() + " - " + target.getSensorName() +
                            " will now be calibrated against " + reference.getPackageName() + " - " + reference.getSensorName())
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        HttpResult<CreateRegressionResponse> response = regressionDriver.createRegression(sessionManager.getSession(), target, reference);
                        if (response.getStatus() == Status.SUCCESS) {
                            Toast.makeText(StreamsActivity.this, R.string.calibration_successful, Toast.LENGTH_LONG).show();
                        }
                        triggerSync(StreamsActivity.this);
                    }
                })
                .setCancelable(false)
                .create()
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intents.startDatabaseWriterService(context);

        adapter.start();
        adapter.notifyDataSetChanged();

        startSensors(context);
    }

    @Override
    protected void onPause() {
        super.onPause();

        adapter.stop();
        stopSensors(context);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.graph_button:
                if (adapter.getCount() == 1) {
                    eventBus.post(new ViewStreamEvent((Sensor) gridView.getChildAt(0).getTag()));
                    startActivity(new Intent(this, GraphActivity.class));
                } else {
                    Toast.makeText(this, R.string.drag_to_graph_stream, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.heat_map_button:
                if (adapter.getCount() == 1) {
                    if (sessionManager.isLocationless()) {
                        Toast.makeText(StreamsActivity.this, R.string.cant_map_without_gps, Toast.LENGTH_SHORT).show();
                    } else {
                        eventBus.post(new ViewStreamEvent((Sensor) gridView.getChildAt(0).getTag()));
                        context.startActivity(new Intent(StreamsActivity.this, AirCastingMapActivity.class));
                    }
                } else {
                    Toast.makeText(context, R.string.drag_to_map_stream, Toast.LENGTH_LONG).show();
                }
                break;
        }
        super.onClick(view);
    }

    @Subscribe
    public void onEvent(MotionEvent event) {
        gridView.dispatchTouchEvent(event);
    }

    @Subscribe
    public void onEvent(LongClickEvent event) {
        gridView.dispatchLongPressEvent(event.getMotionEvent());
    }

    @Subscribe
    public void onEvent(DoubleTapEvent event) {
        gridView.dispatchDoubleClickEvent(event.getMotionEvent());
    }

    @Subscribe
    public void onEvent(TapEvent event) {
        gridView.dispatchSingleTapEvent(event.getMotionEvent());
    }
}
