package pl.llp.aircasting.helper;

import pl.llp.aircasting.MarkerSize;
import pl.llp.aircasting.R;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.SessionManager;

import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import roboguice.inject.InjectResource;

import static java.lang.String.valueOf;

@Singleton
public class GaugeHelper
{
  @Inject ResourceHelper resourceHelper;
  @Inject SettingsHelper settingsHelper;
  @Inject SessionManager sessionManager;

  @InjectResource(R.string.avg_label_template) String avgLabel;
  @InjectResource(R.string.now_label_template) String nowLabel;
  @InjectResource(R.string.peak_label_template) String peakLabel;

  /**
   * Update a set of now/avg/peak gauges
   *
   * @param sensor The Sensor from which the readings are taken
   * @param view   The view containing the three gauges
   */
  public void updateGauges(Sensor sensor, View view)
  {
    updateVisibility(view);

    int now = (int) sessionManager.getNow(sensor);
    updateGauge(view.findViewById(R.id.now_gauge), sensor, MarkerSize.BIG, now);
    updateLabel(sensor, view.findViewById(R.id.now_label), nowLabel);

    boolean hasStats = sessionManager.isSessionStarted() || sessionManager.isSessionSaved();
    if (hasStats && sensor.isEnabled())
    {
      int avg = (int) sessionManager.getAvg(sensor);
      int peak = (int) sessionManager.getPeak(sensor);

      updateGauge(view.findViewById(R.id.avg_gauge), sensor, MarkerSize.SMALL, avg);
      updateGauge(view.findViewById(R.id.peak_gauge), sensor, MarkerSize.SMALL, peak);

      updateLabel(sensor, view.findViewById(R.id.avg_label), avgLabel);
      updateLabel(sensor, view.findViewById(R.id.peak_label), peakLabel);
    }
    else
    {
      displayInactiveGauge(view.findViewById(R.id.avg_gauge), MarkerSize.SMALL);
      displayInactiveGauge(view.findViewById(R.id.peak_gauge), MarkerSize.SMALL);
    }
  }

  private void updateVisibility(View view)
  {
    View nowContainer = view.findViewById(R.id.now_container);

    nowContainer.setVisibility(sessionManager.isSessionSaved() ? View.GONE : View.VISIBLE);
  }

  private void updateLabel(Sensor sensor, View view, String label)
  {
    TextView textView = (TextView) view;

    String formatted = String.format(label, sensor.getShortType());
    textView.setText(formatted);
  }

  private void updateGauge(View view, Sensor sensor, MarkerSize size, int value)
  {
    TextView textView = (TextView) view;

    textView.setText(valueOf(value));
    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, resourceHelper.getTextSize(value, size));
    textView.setBackgroundDrawable(resourceHelper.getGauge(sensor, size, value));
  }

  private void displayInactiveGauge(View view, MarkerSize size)
  {
    TextView textView = (TextView) view;

    textView.setText("--");
    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, ResourceHelper.SMALL_GAUGE_SMALL_TEXT);
    textView.setBackgroundDrawable(resourceHelper.getDisabledGauge(size));
  }
}
