package pl.llp.aircasting.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import pl.llp.aircasting.R;
import pl.llp.aircasting.event.ui.ViewStreamEvent;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.SensorManager;
import roboguice.inject.InjectResource;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

import static com.google.common.collect.Iterables.indexOf;
import static java.util.Collections.sort;

public class SelectSensorHelper {
  public static final int DIALOG_ID = 175483;

  @InjectResource(R.string.select_sensor) String title;
  @Inject SensorManager sensorManager;
  @Inject EventBus eventBus;

  /**
   * Prepare a dialog displaying a list of sensors, allowing the user to select
   * one to view
   *
   * @param context The activity in
   */
  public Dialog chooseSensor(final Activity context) {
    final List<Sensor> sensors = sortedSensors();
    int selected = selectedSensorIndex(sensors);
    String[] listItems = listItems(sensors);

    return new AlertDialog.Builder(context)
        .setTitle(title)
        .setSingleChoiceItems(listItems, selected, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            Sensor sensor = sensors.get(which);
            ViewStreamEvent event = new ViewStreamEvent(sensor);

            eventBus.post(event);

            context.removeDialog(DIALOG_ID);
          }
        })
        .setOnCancelListener(new DialogInterface.OnCancelListener() {
          @Override
          public void onCancel(DialogInterface dialog) {
            context.removeDialog(DIALOG_ID);
          }
        })
        .create();
  }

  private String[] listItems(List<Sensor> sensors) {
    return Lists.transform(sensors, new Function<Sensor, String>() {
      @Override
      public String apply(@Nullable Sensor input) {
        return input.toString();
      }
    }).toArray(new String[]{});
  }

  private int selectedSensorIndex(List<Sensor> sensors) {
    return indexOf(sensors, new Predicate<Sensor>() {
      @Override
      public boolean apply(@Nullable Sensor input) {
        return input.equals(sensorManager.getVisibleSensor());
      }
    });
  }

  private List<Sensor> sortedSensors() {
    final List<Sensor> sensors = sensorManager.getSensors();
    sort(sensors, new Comparator<Sensor>() {
      @Override
      public int compare(Sensor sensor, Sensor sensor1) {
        return sensor.toString().compareTo(sensor1.toString());
      }
    });
    return sensors;
  }
}