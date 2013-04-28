/**
 AirCasting - Share your Air!
 Copyright (C) 2011-2012 HabitatMap, Inc.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 You can contact the authors by email at <info@habitatmap.org>
 */
package pl.llp.aircasting.view.presenter;

import pl.llp.aircasting.activity.ApplicationState;
import pl.llp.aircasting.activity.events.SessionChangeEvent;
import pl.llp.aircasting.event.ui.ViewStreamEvent;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.SensorManager;
import pl.llp.aircasting.model.SessionManager;
import pl.llp.aircasting.model.events.MeasurementEvent;
import pl.llp.aircasting.util.Constants;

import android.content.SharedPreferences;
import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newCopyOnWriteArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Multimaps.index;
import static java.util.Collections.sort;

@Singleton
public class MeasurementPresenter implements SharedPreferences.OnSharedPreferenceChangeListener
{
  private static final long MIN_ZOOM = 30000;

  @Inject SessionManager sessionManager;
  @Inject SettingsHelper settingsHelper;
  @Inject SharedPreferences preferences;
  @Inject EventBus eventBus;
  @Inject SensorManager sensorManager;
  @Inject MeasurementAggregator aggregator;

  private CopyOnWriteArrayList<Measurement> fullView = null;
  private int measurementsSize;

  private int anchor;
  private long visibleMilliseconds = MIN_ZOOM;

  private final CopyOnWriteArrayList<Measurement> timelineView = new CopyOnWriteArrayList<Measurement>();
  private List<Listener> listeners = newArrayList();

  @Inject private ApplicationState state;

  @Inject
  public void init()
  {
    preferences.registerOnSharedPreferenceChangeListener(this);
    eventBus.register(this);
  }

  @Subscribe
  public synchronized void onEvent(MeasurementEvent event)
  {
    onMeasurement(event);
  }

  private void onMeasurement(MeasurementEvent event)
  {
    if (!state.recording().isRecording()) return;
    if (!event.getSensor().equals(sensorManager.getVisibleSensor())) return;

    Measurement measurement = event.getMeasurement();

    prepareFullView();
    updateFullView(measurement);

    if (timelineView != null && anchor == 0)
    {
      updateTimelineView();
    }

    notifyListeners();
  }


  private synchronized void updateTimelineView()
  {
    Stopwatch stopwatch = new Stopwatch().start();

    Measurement measurement = aggregator.getAverage();

    if (aggregator.isComposite())
    {
      if (!timelineView.isEmpty())
      {
        timelineView.remove(timelineView.size() - 1);
      }
      timelineView.add(measurement);
      Constants.logGraphPerformance("updateTimelineView step 0 took " + stopwatch.elapsedMillis());
    }
    else
    {
      long firstToDisplay = measurement.getTime().getTime() - visibleMilliseconds;
      while (!timelineView.isEmpty() &&
          firstToDisplay >=  timelineView.get(0).getTime().getTime())
      {
        timelineView.remove(0);
      }
      Constants.logGraphPerformance("updateTimelineView step 1 took " + stopwatch.elapsedMillis());
      measurementsSize += 1;
      timelineView.add(measurement);
    }

    Constants.logGraphPerformance("updateTimelineView step n took " + stopwatch.elapsedMillis());
  }

  private void updateFullView(Measurement measurement)
  {
    boolean newBucket = isNewBucket(measurement);

    if (newBucket)
    {
      if (!aggregator.isEmpty())
      {
        for (Listener listener : listeners)
        {
          listener.onAveragedMeasurement(aggregator.getAverage());
        }
      }
      aggregator.reset();
    }
    else
    {
      fullView.remove(fullView.size() - 1);
    }

    aggregator.add(measurement);
    fullView.add(aggregator.getAverage());
  }

  private boolean isNewBucket(Measurement measurement)
  {
    if (fullView.isEmpty()) return true;

    Measurement last = getLast(fullView);

    long b1 = bucketBySecond(last);
    long b2 = bucketBySecond(measurement);
    return b1 != b2;
  }

  @Subscribe
  public synchronized void onEvent(SessionChangeEvent event)
  {
    reset();
  }

  @Subscribe
  public synchronized void onEvent(ViewStreamEvent event)
  {
    reset();
  }

  private synchronized void reset()
  {
    fullView = null;
    timelineView.clear();
    anchor = 0;

    notifyListeners();
  }

  private CopyOnWriteArrayList<Measurement> prepareFullView()
  {
    if (fullView != null) return fullView;

    Stopwatch stopwatch = new Stopwatch().start();

    String visibleSensor = sensorManager.getVisibleSensor().getSensorName();
    MeasurementStream stream = sessionManager.getMeasurementStream(visibleSensor);
    Iterable<Measurement> measurements;
    if (stream == null)
    {
      measurements = newArrayList();
    }
    else
    {
      measurements = stream.getMeasurements();
    }

    ImmutableListMultimap<Long, Measurement> forAveraging =
        index(measurements, new Function<Measurement, Long>()
        {
          @Override
          public Long apply(Measurement measurement)
          {
            return measurement.getSecond() / settingsHelper.getAveragingTime();
          }
        });

    Constants.logGraphPerformance("prepareFullView step 1 took " + stopwatch.elapsedMillis());

    ArrayList<Long> times = newArrayList(forAveraging.keySet());
    sort(times);

    Constants.logGraphPerformance("prepareFullView step 2 took " + stopwatch.elapsedMillis());
    List<Measurement> timeboxedMeasurements = newLinkedList();
    for (Long time : times)
    {
      ImmutableList<Measurement> chunk = forAveraging.get(time);
      timeboxedMeasurements.add(average(chunk));
    }

    Constants.logGraphPerformance("prepareFullView step 3 took " + stopwatch.elapsedMillis());
    CopyOnWriteArrayList<Measurement> result = Lists.newCopyOnWriteArrayList(timeboxedMeasurements);

    Constants.logGraphPerformance("prepareFullView step n took " + stopwatch.elapsedMillis());
    fullView = result;
    return result;
  }

  private long bucketBySecond(Measurement measurement)
  {
    return measurement.getSecond() / settingsHelper.getAveragingTime();
  }

  private Measurement average(ImmutableList<Measurement> measurements)
  {
    aggregator.reset();

    for (Measurement measurement : measurements)
    {
      aggregator.add(measurement);
    }

    return aggregator.getAverage();
  }

  public synchronized List<Measurement> getTimelineView()
  {
    if (state.recording().isShowingASession())
    {
      prepareTimelineView();
      return timelineView;
    }
    else
    {
      return newArrayList();
    }
  }

  protected synchronized void prepareTimelineView()
  {
    if (!timelineView.isEmpty())
    {
      return;
    }
    Stopwatch stopwatch = new Stopwatch().start();

    final List<Measurement> measurements = getFullView();
    int position = measurements.size() - 1 - anchor;
    final long lastMeasurementTime = measurements.isEmpty() ? 0 : measurements.get(position).getTime().getTime();

    timelineView.clear();
    TreeMap<Long, Measurement> measurementsMap = new TreeMap<Long, Measurement>();
    for (Measurement m : measurements)
    {
      measurementsMap.put(m.getTime().getTime(), m);
    }

//    +1 because subMap parameters are (inclusive, exclusive)
    timelineView.addAll(measurementsMap.subMap(lastMeasurementTime - visibleMilliseconds, lastMeasurementTime + 1).values());
    measurementsSize = measurements.size();

    Constants.logGraphPerformance("prepareTimelineView for [" + timelineView.size() + "] took " + stopwatch.elapsedMillis());
  }

  public void registerListener(Listener listener)
  {
    listeners.add(listener);
  }

  public void unregisterListener(Listener listener)
  {
    listeners.remove(listener);
  }

  public boolean canZoomIn()
  {
    return visibleMilliseconds > MIN_ZOOM;
  }

  public synchronized boolean canZoomOut()
  {
    prepareTimelineView();
    return timelineView.size() < measurementsSize;
  }

  public void zoomIn()
  {
    if (canZoomIn())
    {
      anchor += timelineView.size() / 4;
      fixAnchor();
      setZoom(visibleMilliseconds / 2);
    }
  }

  public synchronized void zoomOut()
  {
    if (canZoomOut())
    {
      anchor -= timelineView.size() / 2;
      fixAnchor();
      setZoom(visibleMilliseconds * 2);
    }
  }

  synchronized void setZoom(long zoom)
  {
    this.visibleMilliseconds = zoom;
    timelineView.clear();
    notifyListeners();
  }

  private synchronized void fixAnchor()
  {
    if (anchor > measurementsSize - timelineView.size())
    {
      anchor = measurementsSize - timelineView.size();
    }
    if (anchor < 0) {
      anchor = 0;
    }
  }

  public synchronized void scroll(double scrollAmount)
  {
    prepareTimelineView();

    anchor -= scrollAmount * timelineView.size();
    fixAnchor();
    timelineView.clear();

    notifyListeners();
  }

  public List<Measurement> getFullView()
  {
    if (!state.recording().isJustShowingCurrentValues())
    {
      fullView = prepareFullView();
    }
    else
    {
      fullView = newCopyOnWriteArrayList();
    }
    return fullView;
  }

  public synchronized boolean canScrollRight()
  {
    return anchor != 0;
  }

  public synchronized boolean canScrollLeft()
  {
    prepareTimelineView();
    return anchor < measurementsSize - timelineView.size();
  }

  @Override
  public synchronized void onSharedPreferenceChanged(SharedPreferences preferences, String key)
  {
    if (key.equals(SettingsHelper.AVERAGING_TIME))
    {
      reset();
    }
  }

  public interface Listener
  {
    void onViewUpdated();

    void onAveragedMeasurement(Measurement measurement);
  }

  private void notifyListeners()
  {
    for (Listener listener : listeners)
    {
      listener.onViewUpdated();
    }
  }
}
