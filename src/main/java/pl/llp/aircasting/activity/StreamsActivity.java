package pl.llp.aircasting.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
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
            @Override
            public void onEnter(View view) {
                findViewById(R.id.graph_button).setBackgroundColor(getResources().getColor(R.color.transparent));
                gridView.toggleDisplaySize();
            }

            @Override
            public void onLeave(View view) {
                findViewById(R.id.graph_button).setBackgroundColor(getResources().getColor(R.color.bar_blue));
                gridView.toggleDisplaySize();
            }

            @Override
            public void onDrop(View view) {
                eventBus.post(new ViewStreamEvent((Sensor) view.getTag()));
                startActivity(new Intent(StreamsActivity.this, GraphActivity.class));
            }
        };

        SensorsGridView.OnDragListener mapListener = new SensorsGridView.OnDragListener() {
            @Override
            public void onEnter(View view) {
                findViewById(R.id.heat_map_button).setBackgroundColor(getResources().getColor(R.color.transparent));
                gridView.toggleDisplaySize();
            }

            @Override
            public void onLeave(View view) {
                findViewById(R.id.heat_map_button).setBackgroundColor(getResources().getColor(R.color.bar_blue));
                gridView.toggleDisplaySize();
            }

            @Override
            public void onDrop(View view) {
                eventBus.post(new ViewStreamEvent((Sensor) view.getTag()));
                startActivity(new Intent(StreamsActivity.this, AirCastingMapActivity.class));
            }
        };

        gridView.registerListenArea(findViewById(R.id.graph_button), graphListener);
        gridView.registerListenArea(findViewById(R.id.heat_map_button), mapListener);
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
