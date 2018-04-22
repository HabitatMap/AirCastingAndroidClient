/**
 * AirCasting - Share your Air!
 * Copyright (C) 2011-2012 HabitatMap, Inc.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * You can contact the authors by email at <info@habitatmap.org>
 */
package pl.llp.aircasting.view.presenter;

import pl.llp.aircasting.activity.ApplicationState;
import pl.llp.aircasting.activity.events.VisibleSessionUpdatedEvent;
import pl.llp.aircasting.android.Logger;
import pl.llp.aircasting.event.ui.VisibleStreamUpdatedEvent;
import pl.llp.aircasting.helper.VisibleSession;
import pl.llp.aircasting.helper.SessionDataFactory;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.*;
import pl.llp.aircasting.model.CurrentSessionManager;
import pl.llp.aircasting.model.events.MeasurementEvent;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;

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
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newCopyOnWriteArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Multimaps.index;
import static java.util.Collections.sort;

@Singleton
public class MeasurementPresenter implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final long MIN_ZOOM = 120000;
    private static final long SCROLL_TIMEOUT = 1000;
    private static final int MAX_FIXED_MEASUREMENTS = 1440;
    private static final int MAX_MOBILE_MEASUREMENTS = 86400;

    @Inject CurrentSessionManager currentSessionManager;
    @Inject SettingsHelper settingsHelper;
    @Inject SharedPreferences preferences;
    @Inject EventBus eventBus;
    @Inject VisibleSession visibleSession;
    @Inject MeasurementAggregator aggregator;
    @Inject SessionDataFactory sessionData;

    private CopyOnWriteArrayList<Measurement> fullView = null;
    private int measurementsSize;

    private int anchor;
    private long visibleMilliseconds = 3600000;
    private long lastScrolled;
    private Date timeAnchor;

    private final CopyOnWriteArrayList<Measurement> timelineView = new CopyOnWriteArrayList<Measurement>();
    private List<Listener> listeners = newArrayList();

    @Inject
    private ApplicationState state;

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    private Sensor sensor = SimpleAudioReader.getSensor();

    @Inject
    public void init() {
        preferences.registerOnSharedPreferenceChangeListener(this);
        eventBus.register(this);
    }

    @Subscribe
    public synchronized void onEvent(MeasurementEvent event) {
        onMeasurement(event);
    }

    private void onMeasurement(MeasurementEvent event) {
        if (timelineShouldUpdate(event)) {

            Measurement measurement = event.getMeasurement();

            prepareFullView();
            updateFullView(measurement);

            if (anchor == 0) {
                updateTimelineView();
            }

            notifyListeners();
        }
    }

    private boolean timelineShouldUpdate(MeasurementEvent event) {
        boolean sessionUpdatable = state.recording().isRecording() || visibleSession.getSession().isFixed();
        if (!sessionUpdatable) { return false; }

        String visibleSensorName = visibleSession.getStream().getSensorName();
        long visibleSessionId = visibleSession.getVisibleSessionId();

        return event.getSessionId() == visibleSessionId &&
                event.getSensor().getSensorName() == visibleSensorName;
    }

    private synchronized void updateTimelineView() {
        Stopwatch stopwatch = new Stopwatch().start();

        Measurement measurement = aggregator.getAverage();

        if (aggregator.isComposite()) {
            if (!timelineView.isEmpty()) {
                timelineView.remove(timelineView.size() - 1);
            }
            timelineView.add(measurement);
            Logger.logGraphPerformance("updateTimelineView step 0 took " + stopwatch.elapsed(TimeUnit.MILLISECONDS));
        } else {
            long firstToDisplay = measurement.getTime().getTime() - visibleMilliseconds;
            while (!timelineView.isEmpty() &&
                    firstToDisplay >= timelineView.get(0).getTime().getTime()) {
                timelineView.remove(0);
            }
            Logger.logGraphPerformance("updateTimelineView step 1 took " + stopwatch.elapsed(TimeUnit.MILLISECONDS));
            measurementsSize += 1;
            timelineView.add(measurement);
        }

        Logger.logGraphPerformance("updateTimelineView step n took " + stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    private void updateFullView(Measurement measurement) {
        boolean newBucket = isNewBucket(measurement);

        if (newBucket) {
            if (!aggregator.isEmpty()) {
                for (Listener listener : listeners) {
                    listener.onAveragedMeasurement(aggregator.getAverage());
                }
            }
            aggregator.reset();
        } else {
            fullView.remove(fullView.size() - 1);
        }

        aggregator.add(measurement);
        fullView.add(aggregator.getAverage());
    }

    private boolean isNewBucket(Measurement measurement) {
        if (fullView.isEmpty()) return true;

        Measurement last = getLast(fullView);

        long b1 = bucketBySecond(last);
        long b2 = bucketBySecond(measurement);
        return b1 != b2;
    }

    @Subscribe
    public void onEvent(VisibleSessionUpdatedEvent event) {
        this.sensor = visibleSession.getSensor();
        reset();
//        anchor = 0;
        fixAnchor();
    }

    @Subscribe
    public synchronized void onEvent(VisibleStreamUpdatedEvent event) {
        this.sensor = event.getSensor();
        reset();
    }

    private synchronized void reset() {
        fullView = null;
        timelineView.clear();

        notifyListeners();
    }

    private CopyOnWriteArrayList<Measurement> prepareFullView() {
        if (fullView != null) return fullView;

        Stopwatch stopwatch = new Stopwatch().start();

        String sensorName = sensor.getSensorName();
        MeasurementStream stream = sessionData.getStream(sensorName, visibleSession.getVisibleSessionId());
        Iterable<Measurement> measurements;
        if (stream == null) {
            measurements = newArrayList();
        } else {
            int amount = visibleSession.isVisibleSessionFixed() ? MAX_FIXED_MEASUREMENTS : MAX_MOBILE_MEASUREMENTS;
            measurements = stream.getLastMeasurements(amount, 0);
        }

        ImmutableListMultimap<Long, Measurement> forAveraging =
                index(measurements, new Function<Measurement, Long>() {
                    @Override
                    public Long apply(Measurement measurement) {
                        return measurement.getSecond() / settingsHelper.getAveragingTime();
                    }
                });

        Logger.logGraphPerformance("prepareFullView step 1 took " + stopwatch.elapsed(TimeUnit.MILLISECONDS));

        ArrayList<Long> times = newArrayList(forAveraging.keySet());
        sort(times);

        Logger.logGraphPerformance("prepareFullView step 2 took " + stopwatch.elapsed(TimeUnit.MILLISECONDS));
        List<Measurement> mobileMeasurements = newLinkedList();
        for (Long time : times) {
            ImmutableList<Measurement> chunk = forAveraging.get(time);
            mobileMeasurements.add(average(chunk));
        }

        Logger.logGraphPerformance("prepareFullView step 3 took " + stopwatch.elapsed(TimeUnit.MILLISECONDS));
        CopyOnWriteArrayList<Measurement> result = Lists.newCopyOnWriteArrayList(mobileMeasurements);

        Logger.logGraphPerformance("prepareFullView step n took " + stopwatch.elapsed(TimeUnit.MILLISECONDS));
        fullView = result;
        return result;
    }

    private long bucketBySecond(Measurement measurement) {
        return measurement.getSecond() / settingsHelper.getAveragingTime();
    }

    private Measurement average(ImmutableList<Measurement> measurements) {
        aggregator.reset();

        for (Measurement measurement : measurements) {
            aggregator.add(measurement);
        }

        return aggregator.getAverage();
    }

    public synchronized List<Measurement> getTimelineView() {
        prepareTimelineView();
        return timelineView;
    }

    private int timeToAnchor(Date d, List<Measurement> measurements) {
        if (measurements.isEmpty()) return 0;
        int position = 0;
        for (int i = 1; i < measurements.size(); i++) {
            if (Math.abs(d.getTime() - measurements.get(i).getTime().getTime()) <
                    Math.abs(d.getTime() - measurements.get(position).getTime().getTime())) {
                position = i;
            }
        }
        return measurements.size() - 1 - position;
    }

    protected synchronized void prepareTimelineView() {
        if (!timelineView.isEmpty()) {
            return;
        }

        Stopwatch stopwatch = new Stopwatch().start();

        final List<Measurement> measurements = getFullView();

        if (anchor != 0 && new Date().getTime() - lastScrolled > SCROLL_TIMEOUT) {
            anchor = timeToAnchor(timeAnchor, measurements);
        }

        int position = measurements.size() - 1 - anchor;
        final long lastMeasurementTime = measurements.isEmpty() ? 0 : measurements.get(position).getTime().getTime();

        timelineView.clear();
        TreeMap<Long, Measurement> measurementsMap = new TreeMap<Long, Measurement>();
        for (Measurement m : measurements) {
            measurementsMap.put(m.getTime().getTime(), m);
        }

//    +1 because subMap parameters are (inclusive, exclusive)
        timelineView.addAll(measurementsMap.subMap(lastMeasurementTime - visibleMilliseconds, lastMeasurementTime + 1).values());
        measurementsSize = measurements.size();

        Logger.logGraphPerformance("prepareTimelineView for [" + timelineView.size() + "] took " + stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    public void registerListener(Listener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(Listener listener) {
        listeners.remove(listener);
    }

    public boolean canZoomIn() {
        return visibleMilliseconds > MIN_ZOOM;
    }

    public synchronized boolean canZoomOut() {
        prepareTimelineView();
        return timelineView.size() < measurementsSize;
    }

    public void zoomIn() {
        if (canZoomIn()) {
            anchor += timelineView.size() / 4;
            fixAnchor();
            setZoom(visibleMilliseconds / 2);
        }
    }

    public synchronized void zoomOut() {
        if (canZoomOut()) {
            anchor -= timelineView.size() / 2;
            fixAnchor();
            setZoom(visibleMilliseconds * 2);
        }
    }

    synchronized void setZoom(long zoom) {
        this.visibleMilliseconds = zoom;
        timelineView.clear();
        notifyListeners();
    }

    private synchronized void fixAnchor() {
        if (anchor > measurementsSize - timelineView.size()) {
            anchor = measurementsSize - timelineView.size();
        }
        if (anchor < 0) {
            anchor = 0;
        }
        int position = fullView.size() - 1 - anchor;
        timeAnchor = fullView.isEmpty() ? new Date() : fullView.get(position).getTime();
    }

    public synchronized void scroll(double scrollAmount) {

        lastScrolled = new Date().getTime();
        prepareTimelineView();

        anchor -= scrollAmount * timelineView.size();
        fixAnchor();
        timelineView.clear();

        notifyListeners();
    }

    public List<Measurement> getFullView() {
        fullView = prepareFullView();
        return fullView;
    }

    public synchronized boolean canScrollRight() {
        return anchor != 0;
    }

    public synchronized boolean canScrollLeft() {
        prepareTimelineView();
        return anchor < measurementsSize - timelineView.size();
    }

    @Override
    public synchronized void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (key.equals(SettingsHelper.AVERAGING_TIME)) {
            reset();
        }
    }

    public interface Listener {
        void onViewUpdated();

        void onAveragedMeasurement(Measurement measurement);
    }

    private void notifyListeners() {
        for (Listener listener : listeners) {
            listener.onViewUpdated();
        }
    }
}
