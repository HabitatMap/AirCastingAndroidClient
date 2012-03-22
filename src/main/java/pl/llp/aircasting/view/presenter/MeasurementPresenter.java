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

import android.content.SharedPreferences;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.SessionManager;

import java.util.*;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Multimaps.index;
import static java.util.Collections.sort;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/4/11
 * Time: 1:47 PM
 */
@Singleton
public class MeasurementPresenter implements SessionManager.Listener, SharedPreferences.OnSharedPreferenceChangeListener {
    public static final long MILLIS_IN_SECOND = 1000;

    @Inject SessionManager sessionManager;
    @Inject SettingsHelper settingsHelper;
    @Inject SharedPreferences preferences;

    @Inject
    public void init() {
        sessionManager.registerListener(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    private double anchor = 0;

    private LinkedList<Measurement> fullView = null;
    private int measurementsSize;

    private MeasurementAggregator aggregator = new MeasurementAggregator();

    private static final long MIN_ZOOM = 30000;
    private long zoom = MIN_ZOOM;

    private LinkedList<Measurement> timelineView;
    private List<Listener> listeners = newArrayList();

    @Override
    public void onNewMeasurement(Measurement measurement) {
        if (!sessionManager.isSessionSaved()) {
            prepareFullView();
            updateFullView(measurement);

            if (timelineView != null && (int) anchor == 0) {
                updateTimelineView();
            }
        }

        notifyListeners();
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
            fullView.removeLast();
        }

        aggregator.add(measurement);
        fullView.add(aggregator.getAverage());
    }

    private boolean isNewBucket(Measurement measurement) {
        if (fullView.isEmpty()) return true;

        Measurement last = getLast(fullView);

        return bucket(last) != bucket(measurement);
    }

    @Override
    public synchronized void onNewSession() {
        reset();

        notifyListeners();
    }

    private void reset() {
        fullView = null;
        timelineView = null;
        anchor = 0;
    }

    private void prepareFullView() {
        if (fullView != null) return;

        ImmutableListMultimap<Long, Measurement> forAveraging =
                index(sessionManager.getSoundMeasurements(), new Function<Measurement, Long>() {
                    @Override
                    public Long apply(Measurement measurement) {
                        return bucket(measurement);
                    }
                });

        ArrayList<Long> keys = newArrayList(forAveraging.keySet());
        sort(keys);
        fullView = newLinkedList();

        for (Long key : keys) {
            ImmutableList<Measurement> measurements = forAveraging.get(key);
            fullView.add(average(measurements));
        }
    }

    private long bucket(Measurement measurement) {
        return measurement.getTime().getTime() / (settingsHelper.getAveragingTime() * MILLIS_IN_SECOND);
    }

    private Measurement average(Collection<Measurement> measurements) {
        aggregator.reset();

        for (Measurement measurement : measurements) {
            aggregator.add(measurement);
        }

        return aggregator.getAverage();
    }

    @Override
    public void onNewNote(Note eq) {
    }

    @Override
    public void onError() {
    }

    private void notifyListeners() {
        for (Listener listener : listeners) {
            listener.onViewUpdated();
        }
    }

    void setZoom(long zoom) {
        this.zoom = zoom;
        timelineView = null;
        notifyListeners();
    }

    public List<Measurement> getTimelineView() {
        if (sessionManager.isSessionSaved() || sessionManager.isSessionStarted()) {
            prepareTimelineView();
            return timelineView;
        } else {
            return new ArrayList<Measurement>();
        }
    }

    protected synchronized void prepareTimelineView() {
        if (timelineView != null) return;

        final List<Measurement> measurements = getFullView();
        int position = measurements.size() - 1 - (int) anchor;
        final long last = measurements.isEmpty()
                ? 0 : measurements.get(position).getTime().getTime();

        timelineView = newLinkedList(filter(measurements, new Predicate<Measurement>() {
            @Override
            public boolean apply(Measurement measurement) {
                return measurement.getTime().getTime() <= last &&
                        last - measurement.getTime().getTime() <= zoom;
            }
        }));

        measurementsSize = measurements.size();
    }

    private void updateTimelineView() {
        Measurement measurement = aggregator.getAverage();

        if (aggregator.isComposite()) {
            if (!timelineView.isEmpty()) timelineView.removeLast();
            timelineView.add(measurement);
        } else {
            while (timelineView.size() > 0 &&
                    measurement.getTime().getTime() - timelineView.get(0).getTime().getTime() >= zoom) {
                timelineView.removeFirst();
            }
            measurementsSize += 1;
            timelineView.add(measurement);
        }
    }

    public void registerListener(Listener listener) {
        listeners.add(listener);
    }

    public void unregisterListener(Listener listener) {
        listeners.remove(listener);
    }

    public boolean canZoomIn() {
        return zoom > MIN_ZOOM;
    }

    public boolean canZoomOut() {
        prepareTimelineView();
        return timelineView.size() < measurementsSize;
    }

    public void zoomIn() {
        if (canZoomIn()) {
            setZoom(zoom / 2);
        }
    }

    public void zoomOut() {
        if (canZoomOut()) {
            anchor -= timelineView.size();
            fixAnchor();
            setZoom(zoom * 2);
        }
    }

    public List<Measurement> getFullView() {
        if (sessionManager.isSessionSaved() || sessionManager.isSessionStarted()) {
            prepareFullView();
            return fullView;
        } else {
            return new ArrayList<Measurement>();
        }
    }

    public void scroll(double scroll) {
        prepareTimelineView();

        anchor -= scroll * timelineView.size();
        fixAnchor();
        timelineView = null;

        notifyListeners();
    }

    private void fixAnchor() {
        if (anchor > measurementsSize - timelineView.size()) {
            anchor = measurementsSize - timelineView.size();
        }
        if (anchor < 0) {
            anchor = 0;
        }
    }

    public boolean canScrollRight() {
        return anchor != 0;
    }

    public boolean canScrollLeft() {
        prepareTimelineView();
        return anchor < measurementsSize - timelineView.size();
    }

    @Override
    public synchronized void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (key.equals(SettingsHelper.AVERAGING_TIME)) {
            reset();
            notifyListeners();
        }
    }

    public interface Listener {
        void onViewUpdated();

        void onAveragedMeasurement(Measurement measurement);
    }

    private static class MeasurementAggregator {
        private double longitude = 0;
        private double latitude = 0;
        private double value = 0;
        private long time = 0;
        private int count = 0;

        public void add(Measurement measurement) {
            longitude += measurement.getLongitude();
            latitude += measurement.getLatitude();
            value += measurement.getValue();
            time += measurement.getTime().getTime();
            count += 1;
        }

        public void reset() {
            longitude = latitude = value = time = count = 0;
        }

        public Measurement getAverage() {
            return new Measurement(latitude / count, longitude / count, value / count, new Date(time / count));
        }

        public boolean isComposite() {
            return count > 1;
        }

        public boolean isEmpty() {
            return count == 0;
        }
    }
}
