package pl.llp.aircasting.activity.adapter;

import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.ButtonsActivity;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.event.ui.ViewStreamEvent;
import pl.llp.aircasting.helper.GaugeHelper;
import pl.llp.aircasting.helper.TopBarHelper;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.SensorManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.sort;

public class StreamAdapter extends SimpleAdapter implements View.OnClickListener
{
    public static final String TITLE = "title";
    public static final String VERY_LOW = "veryLow";
    public static final String LOW = "low";
    public static final String MID = "mid";
    public static final String HIGH = "high";
    public static final String VERY_HIGH = "veryHigh";

    private static final String[] FROM = new String[]{
            TITLE, VERY_LOW, LOW, MID, HIGH, VERY_HIGH
    };
    private static final int[] TO = new int[]{
            R.id.title, R.id.top_bar_very_low, R.id.top_bar_low, R.id.top_bar_mid, R.id.top_bar_high, R.id.top_bar_very_high
    };

    private static final Comparator<Map<String, Object>> titleComparator = new Comparator<Map<String, Object>>() {
        @Override
        public int compare(@Nullable Map<String, Object> left, @Nullable Map<String, Object> right) {
            String rightTitle = right.get(TITLE).toString();
            String leftTitle = left.get(TITLE).toString();
            return leftTitle.compareTo(rightTitle);
        }
    };
    public static final String SENSOR = "sensor";

    SensorManager sensorManager;
    TopBarHelper topBarHelper;
    GaugeHelper gaugeHelper;
    ButtonsActivity context;
    EventBus eventBus;

    private List<Map<String, Object>> data;
    private Map<String, Map<String, Object>> sensors = newHashMap();


    public StreamAdapter(ButtonsActivity context, List<Map<String, Object>> data, EventBus eventBus,
                         GaugeHelper gaugeHelper, TopBarHelper topBarHelper, SensorManager sensorManager) {
        super(context, data, R.layout.stream, FROM, TO);
        this.data = data;
        this.eventBus = eventBus;
        this.context = context;
        this.gaugeHelper = gaugeHelper;
        this.topBarHelper = topBarHelper;
        this.sensorManager = sensorManager;
    }

    /**
     * Start updating the adapter
     */
    public void start() {
        eventBus.register(this);
    }

    /**
     * Stop updating the adapter
     */
    public void stop() {
        eventBus.unregister(this);
    }

    @Subscribe
    public void onEvent(SensorEvent event) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                update();
            }
        });
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        Map<String, Object> state = data.get(position);
        Sensor sensor = (Sensor) state.get(SENSOR);

        gaugeHelper.updateGauges(sensor, view);
        topBarHelper.updateTopBar(sensor, view);

        initializeButtons(view, sensor);
        view.setClickable(true);
        view.setFocusable(true);

        return view;
    }

  private void initializeButtons(View view, Sensor sensor)
  {
    View recordButton = view.findViewById(R.id.record_stream);
    View deleteButton = view.findViewById(R.id.delete_stream);
    View viewButton = view.findViewById(R.id.view_stream);

    recordButton.setTag(sensor);
    viewButton.setTag(sensor);
    deleteButton.setTag(sensor);
    deleteButton.setOnClickListener(this);
    viewButton.setOnClickListener(this);
    recordButton.setOnClickListener(this);

    if (sensor.isEnabled())
    {
      recordButton.setBackgroundResource(R.drawable.rec_active);
    }
    else
    {
      recordButton.setBackgroundResource(R.drawable.rec_inactive);
    }

    if (sensorManager.hasRunningSession())
    {
      deleteButton.setVisibility(View.INVISIBLE);
      recordButton.setVisibility(View.VISIBLE);
    }
    else
    {
      deleteButton.setVisibility(View.VISIBLE);
      recordButton.setVisibility(View.INVISIBLE);
    }

    if (sensorManager.getVisibleSensor().equals(sensor))
    {
      viewButton.setBackgroundResource(R.drawable.viewing_active);
    }
    else
    {
      viewButton.setBackgroundResource(R.drawable.viewing_inactive);
    }

    View topBar = view.findViewById(R.id.top_bar);
    topBar.setTag(sensor);
    topBar.setOnClickListener(this);
  }

  private void update() {
        data.clear();

    List<Sensor> sensors = sensorManager.getSensors();
    for (Sensor sensor : sensors) {
            Map<String, Object> map = prepareItem(sensor);

            map.put(TITLE, sensor.toString());
            map.put(SENSOR, sensor);

            data.add(map);
        }

        sort(data, titleComparator);

        notifyDataSetChanged();
    }

    private Map<String, Object> prepareItem(Sensor sensor) {
        String name = sensor.getSensorName();
        if (!sensors.containsKey(name)) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            sensors.put(name, map);
            data.add(map);
        }
        return sensors.get(name);
    }

  @Override
  public void onClick(View view)
  {
    Sensor sensor = (Sensor) view.getTag();
    switch (view.getId())
    {
      case R.id.view_stream:
        eventBus.post(new ViewStreamEvent(sensor));
        break;
      case R.id.record_stream:
        sensorManager.toggleSensor(sensor);
        break;
      case R.id.delete_stream:
        confirm(context, sensor);
        break;
      case R.id.top_bar:
        Intents.thresholdsEditor(context, sensor);
        break;
    }

    context.suppressNextTap();
    notifyDataSetChanged();
  }

  private void confirm(ButtonsActivity context, final Sensor sensor)
  {
    AlertDialog.Builder b = new AlertDialog.Builder(context);
    b.setMessage("Are you sure?").
    setCancelable(true).
    setPositiveButton("Yes", new DialogInterface.OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int which)
      {
        sensorManager.deleteSensorFromCurrentSession(sensor);
        update();
      }
    }).setNegativeButton("No", new DialogInterface.OnClickListener()
    {
      @Override
      public void onClick(DialogInterface dialog, int which)
      {
        // be like me - do nothing!
      }
    });
    AlertDialog dialog = b.create();
    dialog.show();
  }
}
