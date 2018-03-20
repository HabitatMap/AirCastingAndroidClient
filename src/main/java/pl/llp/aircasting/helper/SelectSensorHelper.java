package pl.llp.aircasting.helper;

import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import pl.llp.aircasting.R;
import pl.llp.aircasting.model.Sensor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.jetbrains.annotations.Nullable;
import roboguice.inject.InjectResource;

import java.util.Comparator;
import java.util.List;

import static java.util.Collections.sort;

public class SelectSensorHelper {
  public static final int DIALOG_ID = 175483;

  @InjectResource(R.string.select_sensor) String title;
  @Inject VisibleSession visibleSession;
  @Inject SessionDataFactory sessionData;

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

    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
    LayoutInflater inflater = context.getLayoutInflater();
    Toolbar toolbar = (Toolbar) inflater.inflate(R.layout.dialog_toolbar, null);
    toolbar.setTitle(title);

    dialog.setCustomTitle(toolbar);

    return dialog.setSingleChoiceItems(listItems, selected, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        Sensor sensor = sensors.get(which);
        visibleSession.setSensor(sensor);

        context.removeDialog(DIALOG_ID);
      }
    }).setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        context.removeDialog(DIALOG_ID);
      }
    }).create();
  }

  private String[] listItems(List<Sensor> sensors) {
    return Lists.transform(sensors, new Function<Sensor, String>() {
      @Override
      public String apply(@Nullable Sensor input) {
        return input.toSensorString();
      }
    }).toArray(new String[]{});
  }

  private int selectedSensorIndex(List<Sensor> sensors)
  {
    return sensors.indexOf(visibleSession.getSensor());
  }

  private List<Sensor> sortedSensors() {
    final List<Sensor> sensors = sessionData.getSensorsList(visibleSession.getVisibleSessionId());
    sort(sensors, new Comparator<Sensor>() {
      @Override
      public int compare(Sensor sensor, Sensor sensor1) {
        return sensor.toString().compareTo(sensor1.toString());
      }
    });
    return sensors;
  }
}