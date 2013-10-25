package pl.llp.aircasting.activity;

import android.content.Context;
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

        ViewGroup container = (ViewGroup) findViewById(R.id.streams_container);
        View area = layoutInflater.inflate(R.layout.streams_top_bar, container, false);
        container.addView(area);
        gridView.addSpecialArea(area);

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
