package pl.llp.aircasting.helper;

import pl.llp.aircasting.MarkerSize;
import pl.llp.aircasting.MeasurementLevel;
import pl.llp.aircasting.R;
import pl.llp.aircasting.model.MeasurementLevelEvent;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.SensorManager;
import pl.llp.aircasting.model.SessionManager;

import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import roboguice.inject.InjectResource;

import static java.lang.String.valueOf;

@Singleton
public class GaugeHelper
{
  public static final int MARGIN = 2;

  @Inject ResourceHelper resourceHelper;
  @Inject SettingsHelper settingsHelper;
  @Inject SessionManager sessionManager;
  @Inject SensorManager sensorManager;

  @Inject EventBus eventBus;

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
    updateExternalGauges(sensor);

    int now = (int) sessionManager.getNow(sensor);
    updateGauge(view.findViewById(R.id.now_gauge), sensor, MarkerSize.BIG, now);

    String nowText = String.format(nowLabel, sensor.getShortType());
    String avgText = String.format(avgLabel, sensor.getShortType());
    String peakText = String.format(peakLabel, sensor.getShortType());

    TextView nowTextView = (TextView) view.findViewById(R.id.now_label);
    TextView avgTextView = (TextView) view.findViewById(R.id.avg_label);
    TextView peakTextView = (TextView) view.findViewById(R.id.peak_label);

    float avgSize = findMinimumVisibleSize(avgTextView, avgText);
    float peakSize = findMinimumVisibleSize(peakTextView, peakText);
    float nowSize = findMinimumVisibleSize(nowTextView, nowText);

    avgSize = peakSize = Math.min(avgSize, peakSize);

    updateLabel(nowTextView, nowText, nowSize);
    updateLabel(avgTextView, avgText, avgSize);
    updateLabel(peakTextView, peakText, peakSize);

    boolean hasStats = sessionManager.isSessionStarted() || sessionManager.isSessionSaved();
    if (hasStats && sensor.isEnabled())
    {
      int avg = (int) sessionManager.getAvg(sensor);
      int peak = (int) sessionManager.getPeak(sensor);

      updateGauge(view.findViewById(R.id.avg_gauge), sensor, MarkerSize.SMALL, avg);
      updateGauge(view.findViewById(R.id.peak_gauge), sensor, MarkerSize.SMALL, peak);
    }
    else
    {
      displayInactiveGauge(view.findViewById(R.id.avg_gauge), MarkerSize.SMALL);
      displayInactiveGauge(view.findViewById(R.id.peak_gauge), MarkerSize.SMALL);
    }
  }

  private void updateExternalGauges(Sensor sensor)
  {
    Sensor visibleSensor = sensorManager.getVisibleSensor();
    if (visibleSensor.matches(sensor))
    {
      MeasurementLevel level = null;
      if (sessionManager.isSessionSaved())
      {
        level = MeasurementLevel.TOO_LOW;
      }
      else
      {
        int now = (int) sessionManager.getNow(visibleSensor);
        level = resourceHelper.getLevel(visibleSensor, now);
      }
      eventBus.post(new MeasurementLevelEvent(visibleSensor, level));
    }
  }

  private void updateVisibility(View view)
  {
    View nowContainer = view.findViewById(R.id.now_container);

    nowContainer.setVisibility(sessionManager.isSessionSaved() ? View.GONE : View.VISIBLE);
  }

  private void updateLabel(TextView view, String label, float size)
  {
    view.getPaint().setTextSize(size);
    view.setText(label);
  }

  private float findMinimumVisibleSize(TextView textView, String message)
  {
    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
    long viewWidth = textView.getWidth();
    if(viewWidth < 1)
    {
      textView.measure(0, 0);
      viewWidth = textView.getMeasuredWidth();

      if(viewWidth < 1) return textView.getTextSize();
    }

    TextPaint textPaint = textView.getPaint();
    float textSize = textView.getTextSize();
    float paintWidth = textPaint.measureText(message) + 2*MARGIN;
    while(paintWidth > viewWidth)
    {
      textSize--;
      textPaint.setTextSize(textSize);
      paintWidth = textPaint.measureText(message) + 2*MARGIN;
    }
    return textSize;
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
