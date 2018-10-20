package pl.llp.aircasting.sensor;

import pl.llp.aircasting.event.sensor.ThresholdSetEvent;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.internal.MeasurementLevel;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * Created by ags on 01/04/2013 at 13:22
 */
@Singleton
public class ThresholdsHolder
{
  @Inject SettingsHelper settings;
  @Inject EventBus eventBus;

  Map<Sensor, Map<MeasurementLevel, Integer>> thresholds = newHashMap();

  @Inject
  public void init()
  {
    eventBus.register(this);
  }

  @Subscribe
  public void onEvent(ThresholdSetEvent event)
  {
    Sensor sensor = event.getSensor();

    if (sensor != null) {
      Map<MeasurementLevel, Integer> levels = getValues(sensor);
      levels.put(event.getLevel(), event.getValue());
    }
  }

  private HashMap<MeasurementLevel, Integer> initLevels(Sensor sensor)
  {
    HashMap<MeasurementLevel, Integer> values = new HashMap<MeasurementLevel, Integer>();
    for (MeasurementLevel level : MeasurementLevel.OBTAINABLE_LEVELS) {
      int threshold = settings.getThreshold(sensor, level);
      values.put(level, threshold);
    }
    thresholds.put(sensor, values);
    return values;
  }

  public MeasurementLevel getLevel(Sensor sensor, double value)
  {
    if (sensor != null) {
      Map<MeasurementLevel, Integer> levels = getValues(sensor);

      for (MeasurementLevel level : MeasurementLevel.OBTAINABLE_LEVELS) {
        if (value >= levels.get(level)) {
          return level;
        }
      }
    }
    return MeasurementLevel.TOO_LOW;
  }

  private Map<MeasurementLevel, Integer> getValues(Sensor sensor)
  {
    Map<MeasurementLevel, Integer> levels = thresholds.get(sensor);
    if(levels == null)
    {
      levels = initLevels(sensor);
    }
    return levels;
  }

  public int getValue(Sensor sensor, MeasurementLevel level)
  {
    return getValues(sensor).get(level);
  }
}
