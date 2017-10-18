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

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.activity.ApplicationState;
import pl.llp.aircasting.activity.events.SessionAddedEvent;
import pl.llp.aircasting.event.ui.ViewStreamEvent;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.CurrentSessionManager;
import pl.llp.aircasting.model.events.MeasurementEvent;

import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.helper.VisibleSensor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(InjectedTestRunner.class)
public class MeasurementPresenterTest
{
  @Inject MeasurementPresenter presenter;
  List<Measurement> measurements;
  private Measurement measurement1 = new Measurement(4, 4, 4, new Date(0, 0, 0, 0, 1, 8));
  private Measurement measurement2 = new Measurement(5, 6, 5, new Date(0, 0, 0, 0, 1, 10));
  private MeasurementPresenter.Listener listener;
  private MeasurementStream stream;
  private Session session;
  private Sensor sensor;
  private SessionAddedEvent EVENT;

  @Inject ApplicationState state;

  @Before
  public void setup()
  {
    measurements = new ArrayList<Measurement>();
    for (int i = 0; i < 4; i++)
    {
      measurements.add(new Measurement(i, i, i, new Date(0, 0, 0, 0, 1, 2 * i)));
    }

    sensor = mock(Sensor.class);
    when(sensor.getSensorName()).thenReturn("LHC");

    presenter.visibleSensor = mock(VisibleSensor.class);
    when(presenter.visibleSensor.getSensor()).thenReturn(sensor);
    presenter.onEvent(new ViewStreamEvent(sensor, null));

    stream = mock(MeasurementStream.class);
    when(stream.getMeasurements()).thenReturn(measurements);

    session = mock(Session.class);

    presenter.currentSessionManager = mockSessionManager();
    when(presenter.currentSessionManager.getMeasurementStream("LHC")).thenReturn(stream);
    when(presenter.currentSessionManager.isSessionRecording()).thenReturn(true);
    when(presenter.currentSessionManager.getCurrentSession()).thenReturn(session);

    listener = mock(MeasurementPresenter.Listener.class);
    presenter.registerListener(listener);

    presenter.settingsHelper = mock(SettingsHelper.class);
    when(presenter.settingsHelper.getAveragingTime()).thenReturn(1);
    EVENT = new SessionAddedEvent(new Session());
  }

  private CurrentSessionManager mockSessionManager()
  {
    CurrentSessionManager result = mock(CurrentSessionManager.class);
    when(result.isSessionRecording()).thenReturn(true);
    return result;
  }

  private void triggerMeasurement(Measurement measurement)
  {
    presenter.onEvent(new MeasurementEvent(measurement, sensor));
  }

  @Test
  public void shouldShowLastNMillis()
  {
    state.recording().startRecording();
    presenter.setZoom(100);

    List<Measurement> result = presenter.getTimelineView();

    assertThat(result).contains(measurements.get(3));
    assertThat(result).excludes(measurements.get(2));
  }

  @Test
  public void shouldNotCluster()
  {
    state.recording().startRecording();
    presenter.setZoom(4000);

    assertThat(presenter.getTimelineView()).contains(measurements.get(3), measurements.get(2));
  }

  @Test
  public void shouldHandleEmptyList()
  {
    when(stream.getMeasurements()).thenReturn(new ArrayList<Measurement>());

    assertThat(presenter.getTimelineView()).isEmpty();
  }

  @Test
  public void shouldAppendWithoutRecalculating()
  {
    state.recording().startRecording();
    presenter.setZoom(4000);
    presenter.getTimelineView();
    presenter.currentSessionManager = mockSessionManager();

    triggerMeasurement(measurement1);
    List<Measurement> result = presenter.getTimelineView();

    assertThat(result).contains(measurement1, measurements.get(3)).excludes(measurements.get(2));
  }

  @Test
  public void shouldNotifyListeners_when_recording()
  {
    // given
    state.recording().startRecording();

    // when
    triggerMeasurement(measurement1);

    // then
    verify(listener).onViewUpdated();
  }

  @Test
  public void shouldUnregisterListeners()
  {
    presenter.unregisterListener(listener);

    triggerMeasurement(measurement1);

    verifyZeroInteractions(listener);
  }

  @Test
  public void shouldAllowToZoomOut()
  {
    state.recording().startRecording();
    presenter.setZoom(2000);
    assertThat(presenter.canZoomOut()).isTrue();
  }

  @Test
  public void shouldNotAllowToZoomOutTooMuch()
  {
    presenter.setZoom(100000000);
    assertThat(presenter.canZoomOut()).isFalse();
  }

  @Test
  public void shouldAllowToZoomOutAfterMoreDataArrives()
  {
    state.recording().startRecording();
    presenter.setZoom(8000);
    presenter.getTimelineView();
    triggerMeasurement(measurement1);
    assertThat(presenter.canZoomOut()).isTrue();
  }

  @Test
  public void shouldUpdateTheFullView()
  {
    state.recording().startRecording();
    presenter.getFullView();
    presenter.currentSessionManager = mockSessionManager();

    triggerMeasurement(measurement1);
    List<Measurement> result = presenter.getFullView();

    assertThat(result).contains(measurement1);
  }

  @Test
  public void shouldNotUpdateWhenViewingASession()
  {
    presenter.getFullView();
    presenter.currentSessionManager = mockSessionManager();
    when(presenter.currentSessionManager.isSessionRecording()).thenReturn(false);
    when(presenter.currentSessionManager.isSessionBeingViewed()).thenReturn(true);

    presenter.onEvent(EVENT);
    triggerMeasurement(measurement1);

    assertThat(presenter.getFullView()).isEmpty();
  }

  @Test
  public void shouldOnlyUpdateFromVisibleSensorEvents()
  {
    presenter.getFullView();
    presenter.currentSessionManager = mockSessionManager();
    when(presenter.currentSessionManager.isSessionRecording()).thenReturn(false);
    when(presenter.currentSessionManager.isSessionBeingViewed()).thenReturn(true);

    presenter.onEvent(EVENT);
    triggerMeasurement(measurement1);

    assertThat(presenter.getFullView()).isEmpty();
  }

  @Test
  public void fullViewShouldBeEmptyWithoutASession()
  {
    when(presenter.currentSessionManager.isSessionBeingViewed()).thenReturn(false);
    when(presenter.currentSessionManager.isSessionRecording()).thenReturn(false);

    triggerMeasurement(measurement1);

    assertThat(presenter.getFullView()).isEmpty();
  }

  @Test
  public void timelineViewShouldBeEmptyWithoutASession()
  {
    when(presenter.currentSessionManager.isSessionBeingViewed()).thenReturn(false);
    when(presenter.currentSessionManager.isSessionRecording()).thenReturn(false);

    assertThat(presenter.getTimelineView()).isEmpty();
  }
//
//  @Test
//  public void shouldScrollLeft()
//  {
//    presenter.setZoom(4000);
//
//    presenter.scroll(-0.5);
//
//    assertThat(presenter.getTimelineView(), hasItem(equalTo(measurements.get(2))));
//    assertThat(presenter.getTimelineView(), hasItem(equalTo(measurements.get(1))));
//  }
//
//  @Test
//  public void shouldScrollRight()
//  {
//    presenter.setZoom(4000);
//
//    presenter.scroll(-1);
//    presenter.scroll(0.5);
//
//    assertThat(presenter.getTimelineView(), hasItem(equalTo(measurements.get(2))));
//    assertThat(presenter.getTimelineView(), hasItem(equalTo(measurements.get(1))));
//  }
//
//  @Test
//  public void shouldNotScrollTooMuchRight()
//  {
//    presenter.setZoom(4000);
//
//    presenter.scroll(2);
//
//    assertThat(presenter.getTimelineView(), hasItem(equalTo(measurements.get(3))));
//    assertThat(presenter.getTimelineView(), hasItem(equalTo(measurements.get(2))));
//  }
//
//  @Test
//  public void shouldNotScrollTooMuchLeft()
//  {
//    presenter.setZoom(4000);
//
//    presenter.scroll(-10);
//
//    assertThat(presenter.getTimelineView(), hasItem(equalTo(measurements.get(1))));
//    assertThat(presenter.getTimelineView(), hasItem(equalTo(measurements.get(0))));
//  }
//
//  @Test
//  public void shouldNotifyListenersOnScroll()
//  {
//    presenter.scroll(-10);
//
//    verify(listener).onViewUpdated();
//  }
//
//  @Test
//  public void shouldNotUpdateTheTimelineIfScrolled()
//  {
//    presenter.setZoom(4000);
//    presenter.scroll(-0.5);
//
//    presenter.getTimelineView();
//    triggerMeasurement(measurement1);
//
//    assertThat(presenter.getTimelineView(), not(hasItem(equalTo(measurement1))));
//  }
//
//  @Test
//  public void shouldAverage()
//  {
//    when(presenter.settingsHelper.getAveragingTime()).thenReturn(4);
//    Measurement expected = new Measurement(1.5, 1.5, 1.5, new Date(0, 0, 0, 0, 1, 3));
//
//    presenter.onEvent(EVENT);
//
//    assertThat(presenter.getFullView(), hasItem(equalTo(expected)));
//  }
//
  @Test
  public void shouldAverageOnTheFly()
  {
    state.recording().startRecording();
    when(presenter.settingsHelper.getAveragingTime()).thenReturn(4);
    Measurement expected = new Measurement(3.5, 3.5, 3.5, new Date(0, 0, 0, 0, 1, 5));

    presenter.onEvent(EVENT);
    triggerMeasurement(measurement1);
    triggerMeasurement(measurement2);

    assertThat(presenter.getFullView()).contains(expected, measurement2);
  }

  @Test
  public void shouldAverageTimelineOnTheFly()
  {
    state.recording().startRecording();
    when(presenter.settingsHelper.getAveragingTime()).thenReturn(4);
    Measurement expected = new Measurement(3.5, 3.5, 3.5, new Date(0, 0, 0, 0, 1, 5));

    presenter.onEvent(EVENT);
    presenter.getTimelineView();
    triggerMeasurement(measurement1);
    triggerMeasurement(measurement2);

    assertThat(presenter.getTimelineView()).contains(expected, measurement2);
  }

  @Test
  public void shouldNotifyListenersAboutNewAveragedMeasurements()
  {
    state.recording().startRecording();
    when(presenter.settingsHelper.getAveragingTime()).thenReturn(4);
    Measurement expected = new Measurement(3.5, 3.5, 3.5, new Date(0, 0, 0, 0, 1, 5));
    presenter.registerListener(listener);

    triggerMeasurement(measurement1);
    triggerMeasurement(measurement2);
    triggerMeasurement(new Measurement(0, 0, 0, new Date(0, 0, 0, 0, 1, 10)));

    verify(listener, atLeastOnce()).onAveragedMeasurement(expected);
  }
}
