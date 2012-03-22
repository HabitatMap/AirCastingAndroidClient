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

import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.event.sensor.MeasurementEvent;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.SessionManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.IsCollectionContaining.hasItem;
import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/4/11
 * Time: 1:48 PM
 */
@RunWith(InjectedTestRunner.class)
public class MeasurementPresenterTest {
    @Inject MeasurementPresenter presenter;
    List<Measurement> measurements;
    private Measurement measurement1;
    private MeasurementPresenter.Listener listener;
    private Measurement measurement2;

    @Before
    public void setup() {
        measurements = new ArrayList<Measurement>();
        for (int i = 0; i < 4; i++) {
            measurements.add(new Measurement(i, i, i, new Date(0, 0, 0, 0, 1, 2 * i)));
        }
        presenter.sessionManager = mockSessionManager();
        when(presenter.sessionManager.getSoundMeasurements()).thenReturn(measurements);
        when(presenter.sessionManager.isSessionStarted()).thenReturn(true);

        measurement1 = new Measurement(4, 4, 4, new Date(0, 0, 0, 0, 1, 8));
        measurement2 = new Measurement(5, 6, 5, new Date(0, 0, 0, 0, 1, 10));

        listener = mock(MeasurementPresenter.Listener.class);
        presenter.registerListener(listener);

        presenter.settingsHelper = mock(SettingsHelper.class);
        when(presenter.settingsHelper.getAveragingTime()).thenReturn(1);
    }

    private SessionManager mockSessionManager() {
        SessionManager result = mock(SessionManager.class);
        when(result.isSessionStarted()).thenReturn(true);
        return result;
    }

    private void triggerMeasurement(Measurement measurement){
        presenter.onEvent(new MeasurementEvent(measurement));
    }

    @Test
    public void shouldShowLastNMillis() {
        presenter.setZoom(100);

        List<Measurement> result = presenter.getTimelineView();

        assertThat(result, hasItem(equalTo(measurements.get(3))));
        assertThat(result, not(hasItem(equalTo(measurements.get(2)))));
    }

    @Test
    public void shouldNotCluster() {
        presenter.setZoom(4000);

        assertThat(presenter.getTimelineView(), hasItem(equalTo(measurements.get(3))));
        assertThat(presenter.getTimelineView(), hasItem(equalTo(measurements.get(2))));
    }

    @Test
    public void shouldHandleEmptyList() {
        when(presenter.sessionManager.getSoundMeasurements()).thenReturn(new ArrayList<Measurement>());

        assertThat(presenter.getTimelineView().isEmpty(), equalTo(true));
    }

    @Test
    public void shouldAppendWithoutRecalculating() {
        presenter.setZoom(4000);
        presenter.getTimelineView();
        presenter.sessionManager = mockSessionManager();

        triggerMeasurement(measurement1);
        List<Measurement> result = presenter.getTimelineView();

        verify(presenter.sessionManager, never()).getSoundMeasurements();
        assertThat(result, hasItem(equalTo(measurement1)));
        assertThat(result, hasItem(equalTo(measurements.get(3))));
        assertThat(result, not(hasItem(equalTo(measurements.get(2)))));
    }

    @Test
    public void shouldNotifyListeners() {
        triggerMeasurement(measurement1);

        verify(listener).onViewUpdated();
    }

    @Test
    public void shouldUnregisterListeners() {
        presenter.unregisterListener(listener);

        triggerMeasurement(measurement1);

        verifyZeroInteractions(listener);
    }

    @Test
    public void shouldAllowToZoomOut() {
        presenter.setZoom(2000);
        assertThat(presenter.canZoomOut(), equalTo(true));
    }

    @Test
    public void shouldNotAllowToZoomOutTooMuch() {
        presenter.setZoom(100000000);
        assertThat(presenter.canZoomOut(), equalTo(false));
    }

    @Test
    public void shouldAllowToZoomOutAfterMoreDataArrives() {
        presenter.setZoom(8000);
        presenter.getTimelineView();
        triggerMeasurement(measurement1);
        assertThat(presenter.canZoomOut(), equalTo(true));
    }

    @Test
    public void shouldUpdateTheFullView() {
        presenter.getFullView();
        presenter.sessionManager = mockSessionManager();

        triggerMeasurement(measurement1);
        List<Measurement> result = presenter.getFullView();

        verify(presenter.sessionManager, never()).getSoundMeasurements();
        assertThat(result, hasItem(equalTo(measurement1)));
    }

    @Test
    public void shouldNotUpdateWhenViewingASession() {
        presenter.getFullView();
        presenter.sessionManager = mockSessionManager();
        when(presenter.sessionManager.isSessionStarted()).thenReturn(false);
        when(presenter.sessionManager.isSessionSaved()).thenReturn(true);

        presenter.onNewSession();
        triggerMeasurement(measurement1);

        assertThat(presenter.getFullView().isEmpty(), equalTo(true));
    }

    @Test
    public void fullViewShouldBeEmptyWithoutASession() {
        when(presenter.sessionManager.isSessionSaved()).thenReturn(false);
        when(presenter.sessionManager.isSessionStarted()).thenReturn(false);

        triggerMeasurement(measurement1);

        assertThat(presenter.getFullView().isEmpty(), equalTo(true));
    }

    @Test
    public void timelineViewShouldBeEmptyWithoutASession() {
        when(presenter.sessionManager.isSessionSaved()).thenReturn(false);
        when(presenter.sessionManager.isSessionStarted()).thenReturn(false);

        assertThat(presenter.getTimelineView().isEmpty(), equalTo(true));
    }

    @Test
    public void shouldScrollLeft() {
        presenter.setZoom(4000);

        presenter.scroll(-0.5);

        assertThat(presenter.getTimelineView(), hasItem(equalTo(measurements.get(2))));
        assertThat(presenter.getTimelineView(), hasItem(equalTo(measurements.get(1))));
    }

    @Test
    public void shouldScrollRight() {
        presenter.setZoom(4000);

        presenter.scroll(-1);
        presenter.scroll(0.5);

        assertThat(presenter.getTimelineView(), hasItem(equalTo(measurements.get(2))));
        assertThat(presenter.getTimelineView(), hasItem(equalTo(measurements.get(1))));
    }

    @Test
    public void shouldNotScrollTooMuchRight() {
        presenter.setZoom(4000);

        presenter.scroll(2);

        assertThat(presenter.getTimelineView(), hasItem(equalTo(measurements.get(3))));
        assertThat(presenter.getTimelineView(), hasItem(equalTo(measurements.get(2))));
    }

    @Test
    public void shouldNotScrollTooMuchLeft() {
        presenter.setZoom(4000);

        presenter.scroll(-10);

        assertThat(presenter.getTimelineView(), hasItem(equalTo(measurements.get(1))));
        assertThat(presenter.getTimelineView(), hasItem(equalTo(measurements.get(0))));
    }

    @Test
    public void shouldNotifyListenersOnScroll() {
        presenter.scroll(-10);

        verify(listener).onViewUpdated();
    }

    @Test
    public void shouldNotUpdateTheTimelineIfScrolled() {
        presenter.setZoom(4000);
        presenter.scroll(-0.5);

        presenter.getTimelineView();
        triggerMeasurement(measurement1);

        assertThat(presenter.getTimelineView(), not(hasItem(equalTo(measurement1))));
    }

    @Test
    public void shouldAverage() {
        when(presenter.settingsHelper.getAveragingTime()).thenReturn(4);
        Measurement expected = new Measurement(1.5, 1.5, 1.5, new Date(0, 0, 0, 0, 1, 3));

        presenter.onNewSession();

        assertThat(presenter.getFullView(), hasItem(equalTo(expected)));
    }

    @Test
    public void shouldAverageOnTheFly() {
        when(presenter.settingsHelper.getAveragingTime()).thenReturn(4);
        Measurement expected = new Measurement(3.5, 3.5, 3.5, new Date(0, 0, 0, 0, 1, 5));

        presenter.onNewSession();
        triggerMeasurement(measurement1);
        triggerMeasurement(measurement2);

        assertThat(presenter.getFullView(), hasItem(equalTo(expected)));
        assertThat(presenter.getFullView(), hasItem(equalTo(measurement2)));
    }

    @Test
    public void shouldAverageTimelineOnTheFly() {
        when(presenter.settingsHelper.getAveragingTime()).thenReturn(4);
        Measurement expected = new Measurement(3.5, 3.5, 3.5, new Date(0, 0, 0, 0, 1, 5));

        presenter.onNewSession();
        presenter.getTimelineView();
        triggerMeasurement(measurement1);
        triggerMeasurement(measurement2);

        assertThat(presenter.getTimelineView(), hasItem(equalTo(expected)));
        assertThat(presenter.getTimelineView(), hasItem(equalTo(measurement2)));
    }

    @Test
    public void shouldNotifyListenersAboutNewAveragedMeasurements() {
        when(presenter.settingsHelper.getAveragingTime()).thenReturn(4);
        Measurement expected = new Measurement(3.5, 3.5, 3.5, new Date(0, 0, 0, 0, 1, 5));
        presenter.registerListener(listener);

        triggerMeasurement(measurement1);
        triggerMeasurement(measurement2);
        triggerMeasurement(new Measurement(0, 0, 0, new Date(0, 0, 0, 0, 1, 10)));

        verify(listener, atLeastOnce()).onAveragedMeasurement(expected);
    }
}
