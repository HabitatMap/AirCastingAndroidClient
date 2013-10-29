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
import pl.llp.aircasting.event.ui.LongClickEvent;
import pl.llp.aircasting.event.ui.ViewStreamEvent;
import pl.llp.aircasting.helper.StreamViewHelper;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.view.SensorsGridView;
import roboguice.inject.InjectView;

import static pl.llp.aircasting.Intents.startSensors;
import static pl.llp.aircasting.Intents.stopSensors;

public class StreamsActivity extends ButtonsActivity {
    @Inject Context context;
    @Inject StreamAdapterFactory adapterFactory;

    @InjectView(R.id.sensors_grid) SensorsGridView gridView;

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

        SensorsGridView.OnDragListener graphListener = new SensorsGridView.OnDragListener() {
            private int origHeight;

            @Override
            public void onEnter(View view) {
                View area = findViewById(R.id.graph_button);
                View container = findViewById(R.id.graph_button_container);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) container.getLayoutParams();
                params.weight = 2f;
                container.setLayoutParams(params);
                ViewGroup.LayoutParams areaParams = area.getLayoutParams();
                origHeight = areaParams.height;
                areaParams.height *= 1.5f;
                area.setLayoutParams(areaParams);
                gridView.setPadding(gridView.getPaddingLeft(), areaParams.height + 4, gridView.getPaddingRight(), gridView.getPaddingBottom());
            }

            @Override
            public void onLeave(View view) {
                View area = findViewById(R.id.graph_button);
                View container = findViewById(R.id.graph_button_container);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) container.getLayoutParams();
                params.weight = 1f;
                container.setLayoutParams(params);
                ViewGroup.LayoutParams areaParams = area.getLayoutParams();
                areaParams.height = origHeight;
                area.setLayoutParams(areaParams);
                if (!gridView.isInListenArea())
                    gridView.setPadding(gridView.getPaddingLeft(), areaParams.height, gridView.getPaddingRight(), gridView.getPaddingBottom());
            }

            @Override
            public void onDrop(View view) {
                eventBus.post(new ViewStreamEvent((Sensor) view.getTag()));
                startActivity(new Intent(StreamsActivity.this, GraphActivity.class));
            }
        };

        SensorsGridView.OnDragListener mapListener = new SensorsGridView.OnDragListener() {
            private int origHeight;

            @Override
            public void onEnter(View view) {
                View area = findViewById(R.id.heat_map_button);
                View container = findViewById(R.id.heat_map_button_container);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) container.getLayoutParams();
                params.weight = 2f;
                container.setLayoutParams(params);
                ViewGroup.LayoutParams areaParams = area.getLayoutParams();
                origHeight = areaParams.height;
                areaParams.height *= 1.5f;
                area.setLayoutParams(areaParams);
                gridView.setPadding(gridView.getPaddingLeft(), areaParams.height + 4, gridView.getPaddingRight(), gridView.getPaddingBottom());

            }

            @Override
            public void onLeave(View view) {
                View area = findViewById(R.id.heat_map_button);
                View container = findViewById(R.id.heat_map_button_container);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) container.getLayoutParams();
                params.weight = 1f;
                container.setLayoutParams(params);
                ViewGroup.LayoutParams areaParams = area.getLayoutParams();
                areaParams.height = origHeight;
                area.setLayoutParams(areaParams);
                if (!gridView.isInListenArea())
                    gridView.setPadding(gridView.getPaddingLeft(), areaParams.height, gridView.getPaddingRight(), gridView.getPaddingBottom());

            }

            @Override
            public void onDrop(View view) {
                eventBus.post(new ViewStreamEvent((Sensor) view.getTag()));
                startActivity(new Intent(StreamsActivity.this, AirCastingMapActivity.class));
            }
        };

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

    @Subscribe
    public void onEvent(MotionEvent event) {
        gridView.dispatchTouchEvent(event);
    }

    @Subscribe
    public void onEvent(LongClickEvent event) {
        gridView.dispatchLongPressEvent(event.getMotionEvent());
    }
}
