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

import pl.llp.aircasting.event.sensor.MeasurementEvent;
import pl.llp.aircasting.event.session.SessionChangeEvent;
import pl.llp.aircasting.event.ui.ViewStreamEvent;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.SensorManager;
import pl.llp.aircasting.model.SessionManager;
import pl.llp.aircasting.util.Constants;

import android.content.SharedPreferences;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newCopyOnWriteArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Multimaps.index;
import static java.util.Collections.sort;

@Singleton
public class MeasurementPresenter implements SharedPreferences.OnSharedPreferenceChangeListener
{
  public static final long MILLIS_IN_SECOND = 1000;
  private static final int SAMPLE_LIMIT = 1000;
  private static final long MIN_ZOOM = 30000;

  @Inject SessionManager sessionManager;
  @Inject SettingsHelper settingsHelper;
  @Inject SharedPreferences preferences;
  @Inject EventBus eventBus;
  @Inject SensorManager sensorManager;
  @Inject MeasurementAggregator aggregator;

  private double anchor = 0;

  private List<Measurement> fullView = null;
  private int measurementsSize;

  private long zoom = MIN_ZOOM;

  private final CopyOnWriteArrayList<Measurement> timelineView = new CopyOnWriteArrayList<Measurement>();
  private List<Listener> listeners = newArrayList();

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
    if (sessionManager.isSessionSaved()) return;
        if (!event.getSensor().equals(sensorManager.getVisibleSensor())) return;

        Measurement measurement = event.getMeasurement();

        prepareFullView();
        updateFullView(measurement);

        if (timelineView != null && (int) anchor == 0)
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
    }
    else
    {
      while (timelineView.size() > 0 &&
          measurement.getTime().getTime() - timelineView.get(0).getTime().getTime() >= zoom)
      {
        timelineView.remove(0);
      }
      measurementsSize += 1;
      timelineView.add(measurement);
    }

    Constants.logGraphPerformance("prepareTimelineView took " + stopwatch.elapsedMillis());
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

    return bucket(last) != bucket(measurement);
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

  private List<Measurement> prepareFullView()
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
            return bucket(measurement);
          }
        });

    ArrayList<Long> times = newArrayList(forAveraging.keySet());
    sort(times);

    List<Measurement> timeboxedMeasurements = newLinkedList();
    for (Long time : times)
    {
      ImmutableList<Measurement> chunk = forAveraging.get(time);
      timeboxedMeasurements.add(average(chunk));
    }

    List<Measurement> result = Lists.newCopyOnWriteArrayList(timeboxedMeasurements);

    Constants.logGraphPerformance("prepareFullView took " + stopwatch.elapsedMillis());
    fullView = result;
    return result;
  }

  private long bucket(Measurement measurement)
  {
    return measurement.getTime().getTime() / (settingsHelper.getAveragingTime() * MILLIS_IN_SECOND);
  }

  private Measurement average(Collection<Measurement> measurements)
  {
    aggregator.reset();

    for (Measurement measurement : measurements)
    {
      aggregator.add(measurement);
    }

    return aggregator.getAverage();
  }

  synchronized void setZoom(long zoom)
  {
    this.zoom = zoom;
    timelineView.clear();
    notifyListeners();
  }

  public synchronized List<Measurement> getTimelineView()
  {
    if (sessionManager.isSessionSaved() || sessionManager.isSessionStarted())
    {
      prepareTimelineView();
      return timelineView;
    }
    else
    {
      return new ArrayList<Measurement>();
    }
  }

  protected synchronized CopyOnWriteArrayList<Measurement> prepareTimelineView()
  {
    if (!timelineView.isEmpty()) return null;
    Stopwatch stopwatch = new Stopwatch().start();

    final List<Measurement> measurements = getFullView();
    int position = measurements.size() - 1 - (int) anchor;
    final long lastMeasurementTime = measurements.isEmpty()
        ? 0 : measurements.get(position).getTime().getTime();


    timelineView.clear();
    Iterables.addAll(timelineView, filter(measurements, fitsInTimeline(lastMeasurementTime)));

    measurementsSize = measurements.size();

    Constants.logGraphPerformance("prepareTimelineView took " + stopwatch.elapsedMillis());
    return timelineView;
  }

  private Predicate<Measurement> fitsInTimeline(final long lastMeasurementTime)
  {
    return new Predicate<Measurement>()
    {
      @Override
      public boolean apply(@Nullable Measurement measurement)
      {
        if (measurement == null)
        {
          return false;
        }

        return measurement.getTime().getTime() <= lastMeasurementTime &&
            lastMeasurementTime - measurement.getTime().getTime() <= zoom;
      }
    };
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
    return zoom > MIN_ZOOM;
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
      setZoom(zoom / 2);
    }
  }

  public synchronized void zoomOut()
  {
    if (canZoomOut())
    {
      anchor -= timelineView.size();
      fixAnchor();
      setZoom(zoom * 2);
    }
  }

  public List<Measurement> getFullView()
  {
    if (sessionManager.isSessionSaved() || sessionManager.isSessionStarted())
    {
      fullView = prepareFullView();
    }
    else
    {
      fullView = newCopyOnWriteArrayList();
    }
    return fullView;
  }

  public synchronized void scroll(double scrollAmount)
  {
    prepareTimelineView();

    anchor -= scrollAmount * timelineView.size();
    fixAnchor();
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
