package pl.llp.aircasting.activity.adapter;

import android.util.Log;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.ButtonsActivity;
import pl.llp.aircasting.event.ui.ViewStreamEvent;
import pl.llp.aircasting.helper.GaugeHelper;
import pl.llp.aircasting.helper.NoOp;
import pl.llp.aircasting.helper.StreamViewHelper;
import pl.llp.aircasting.helper.TopBarHelper;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.SensorManager;
import pl.llp.aircasting.model.SessionManager;
import pl.llp.aircasting.model.events.SensorEvent;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.sort;

public class StreamAdapter extends SimpleAdapter implements View.OnClickListener
{
  public static final String QUANTITY = "quantity";
  public static final String SENSOR_NAME = "sensorName";
  public static final String VERY_LOW = "veryLow";
  public static final String LOW = "low";
  public static final String MID = "mid";
  public static final String HIGH = "high";
  public static final String VERY_HIGH = "veryHigh";

  private static final String[] FROM = new String[]{
      QUANTITY, SENSOR_NAME,VERY_LOW, LOW, MID, HIGH, VERY_HIGH
  };
  private static final int[] TO = new int[]{
      R.id.quantity, R.id.sensor_name, R.id.top_bar_very_low, R.id.top_bar_low, R.id.top_bar_mid, R.id.top_bar_high, R.id.top_bar_very_high
  };

  private static final Comparator<Map<String, Object>> titleComparator = new Comparator<Map<String, Object>>() {
    @Override
    public int compare(@Nullable Map<String, Object> left, @Nullable Map<String, Object> right) {
      String rightTitle = right.get(QUANTITY).toString();
      String leftTitle = left.get(QUANTITY).toString();
      return leftTitle.compareTo(rightTitle);
    }
  };
  public static final String SENSOR = "sensor";

  SessionManager sessionManager;
  SensorManager sensorManager;
  StreamViewHelper streamViewHelper;

  ButtonsActivity context;
  EventBus eventBus;

  private List<Map<String, Object>> data;
  private Map<String, Map<String, Object>> sensors = newHashMap();


  public StreamAdapter(ButtonsActivity context, List<Map<String, Object>> data, EventBus eventBus,
                       StreamViewHelper streamViewHelper, SensorManager sensorManager, SessionManager sessionManager) {
    super(context, data, R.layout.stream, FROM, TO);
    this.data = data;
    this.eventBus = eventBus;
    this.context = context;
    this.sensorManager = sensorManager;
    this.sessionManager = sessionManager;
    this.streamViewHelper = streamViewHelper;
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

    streamViewHelper.updateMeasurements(sensor, view);
    initializeButtons(view, sensor);

    view.setClickable(true);
    view.setFocusable(true);

    return view;
  }

  private void initializeButtons(View view, Sensor sensor)
  {
    View deleteButton = view.findViewById(R.id.delete_stream);
    View viewButton = view.findViewById(R.id.view_stream);

    viewButton.setTag(sensor);
    deleteButton.setTag(sensor);
    deleteButton.setOnClickListener(this);
    viewButton.setOnClickListener(this);

    if (sensorManager.isSessionBeingRecorded())
    {
      deleteButton.setVisibility(View.GONE);
    }
    else if(sensorManager.isSessionBeingViewed())
    {
      deleteButton.setVisibility(View.VISIBLE);
    }
    else
    {
      deleteButton.setVisibility(View.GONE);
    }

    Sensor visibleSensor = sensorManager.getVisibleSensor();
    if (visibleSensor.equals(sensor))
    {
      viewButton.setBackgroundResource(R.drawable.viewing_active);
    }
    else
    {
      viewButton.setBackgroundResource(R.drawable.viewing_inactive);
    }

  }

  private void update() {
    data.clear();

    List<Sensor> sensors = sensorManager.getSensors();
    for (Sensor sensor : sensors) {
      Map<String, Object> map = prepareItem(sensor);

      map.put(QUANTITY, sensor.getMeasurementType() + " - " + sensor.getSymbol());
      map.put(SENSOR_NAME, sensor.getSensorName());
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
        deleteStream(context, sensor);
        break;
      case R.id.top_bar:
        Intents.thresholdsEditor(context, sensor);
        break;
    }

    context.suppressNextTap();
    notifyDataSetChanged();
  }

  private void deleteStream(ButtonsActivity context, Sensor sensor)
  {
    if(sessionManager.getSession().getActiveMeasurementStreams().size() > 1)
    {
      confirmDeletingStream(context, sensor);
    }
    else
    {
      confirmDeletingSession(context);
    }

  }

  private void confirmDeletingSession(final ButtonsActivity context)
  {
    AlertDialog.Builder b = new AlertDialog.Builder(context);
    b.setMessage("This is the only stream, delete session?").
        setCancelable(true).
        setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
          @Override
          public void onClick(DialogInterface dialog, int which)
          {
            sessionManager.deleteSession();
            Intents.triggerSync(context);
            Intents.sessions(context, context);
          }
        }).setNegativeButton("No", NoOp.dialogOnClick());
    AlertDialog dialog = b.create();
    dialog.show();

  }

  private void confirmDeletingStream(final ButtonsActivity context, final Sensor sensor)
  {
    AlertDialog.Builder b = new AlertDialog.Builder(context);
    b.setMessage("Delete stream?").
        setCancelable(true).
        setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
          @Override
          public void onClick(DialogInterface dialog, int which)
          {
            sensorManager.deleteSensorFromCurrentSession(sensor);
            update();
            Intents.triggerSync(context);
          }
        }).setNegativeButton("No", NoOp.dialogOnClick());
    AlertDialog dialog = b.create();
    dialog.show();
  }
}
