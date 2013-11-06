package pl.llp.aircasting.activity;

import android.content.Context;
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
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.adapter.StreamAdapter;
import pl.llp.aircasting.activity.adapter.StreamAdapterFactory;
import pl.llp.aircasting.activity.listener.OnSensorDragListener;
import pl.llp.aircasting.event.ui.DoubleTapEvent;
import pl.llp.aircasting.event.ui.LongClickEvent;
import pl.llp.aircasting.event.ui.TapEvent;
import pl.llp.aircasting.event.ui.ViewStreamEvent;
import pl.llp.aircasting.helper.StreamViewHelper;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.SensorManager;
import pl.llp.aircasting.view.SensorsGridView;
import roboguice.inject.InjectView;

import static pl.llp.aircasting.Intents.startSensors;
import static pl.llp.aircasting.Intents.stopSensors;

public class StreamsActivity extends ButtonsActivity {
    @Inject Context context;
    @Inject StreamAdapterFactory adapterFactory;
    @Inject SensorManager sensorManager;

    @InjectView(R.id.sensors_grid) SensorsGridView gridView;
    @InjectView(R.id.heat_map_button_container) View mapContainer;
    @InjectView(R.id.heat_map_button) View mapButton;
    @InjectView(R.id.graph_button_container) View graphContainer;
    @InjectView(R.id.graph_button) View graphButton;

    StreamAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                if (sensorManager.isSessionBeingViewed())
                    return;

                adapter.toggleStatsVisibility((Sensor) view.getTag());
            }
        });

        SensorsGridView.OnDragListener graphListener = new OnSensorDragListener(eventBus, this, gridView, graphButton, graphContainer,
                new Intent(this, GraphActivity.class));

        SensorsGridView.OnDragListener mapListener = new OnSensorDragListener(eventBus, this, gridView, mapButton, mapContainer,
                new Intent(this, AirCastingMapActivity.class));

        gridView.registerListenArea((ViewGroup) findViewById(R.id.buttons), R.id.graph_button_container, graphListener);
        gridView.registerListenArea((ViewGroup) findViewById(R.id.buttons), R.id.heat_map_button_container, mapListener);
    }

    @Override
    protected void onResume() {
        super.onResume();

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
                    eventBus.post(new ViewStreamEvent((Sensor) gridView.getChildAt(0).getTag()));
                    startActivity(new Intent(this, AirCastingMapActivity.class));
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
