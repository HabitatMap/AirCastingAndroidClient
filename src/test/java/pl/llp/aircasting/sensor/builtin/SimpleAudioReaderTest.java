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
package pl.llp.aircasting.sensor.builtin;

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.event.sensor.AudioReaderErrorEvent;
import pl.llp.aircasting.screens.sessions.CalibrationHelper;
import pl.llp.aircasting.screens.common.helpers.SettingsHelper;
import pl.llp.aircasting.model.events.SensorEvent;
import pl.llp.aircasting.model.internal.MeasurementLevel;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.hamcrest.core.IsEqual;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(InjectedTestRunner.class)
public class SimpleAudioReaderTest
{
    public static final int SAMPLE_RATE = 44100;

    @Inject SimpleAudioReader audioReader;

    @Before
    public void setup() {
        audioReader.audioReader = mock(AudioReader.class);
        audioReader.settingsHelper = mock(SettingsHelper.class);
        audioReader.signalPower = mock(SignalPower.class);
        audioReader.eventBus = mock(EventBus.class);
        audioReader.calibrationHelper = mock(CalibrationHelper.class);
    }

    private SensorEvent expected(double value) {
        return new SensorEvent(SimpleAudioReader.SENSOR_PACKAGE_NAME, SimpleAudioReader.SENSOR_NAME,
                               SimpleAudioReader.MEASUREMENT_TYPE, SimpleAudioReader.SHORT_TYPE,
                               SimpleAudioReader.UNIT, SimpleAudioReader.SYMBOL,
                               SimpleAudioReader.VERY_LOW, SimpleAudioReader.LOW, SimpleAudioReader.MID, SimpleAudioReader.HIGH,
                               SimpleAudioReader.VERY_HIGH, value);
    }

    @Test
    public void shouldStartReader() {
        audioReader.start();

        verify(audioReader.audioReader).startReader(SAMPLE_RATE, SAMPLE_RATE / 2, audioReader);
    }

    @Test
    public void shouldStopReader() {
        audioReader.stop();

        verify(audioReader.audioReader).stopReader();
    }

    @Test
    public void shouldPassErrorsToListener() {
        audioReader.start();

        audioReader.onReadError(102);

        verify(audioReader.eventBus).post(any(AudioReaderErrorEvent.class));
    }

    @Test
    public void shouldProcessSamples() {
        when(audioReader.signalPower.calculatePowerDb(Mockito.any(short[].class)))
                .thenReturn(12.3)
                .thenReturn(12.4);
        when(audioReader.calibrationHelper.calibrate(12.3)).thenReturn(13.3);
        when(audioReader.calibrationHelper.calibrate(12.4)).thenReturn(13.4);

        audioReader.start();
        audioReader.onReadComplete(new short[0]);
        audioReader.onReadComplete(new short[0]);

        SensorEvent expected1 = expected(13.3);
        SensorEvent expected2 = expected(13.4);
        verify(audioReader.eventBus).post(expected1);
        verify(audioReader.eventBus).post(expected2);
    }

    @Test
    public void shouldIgnoreBatchesOfFaultyData() {
        when(audioReader.signalPower.calculatePowerDb(Mockito.any(short[].class)))
                .thenReturn(null)
                .thenReturn(2.2);
        when(audioReader.calibrationHelper.calibrate(2.2)).thenReturn(2.2);

        audioReader.start();
        audioReader.onReadComplete(new short[0]);
        audioReader.onReadComplete(new short[0]);

        SensorEvent expected = expected(2.2);
        verify(audioReader.eventBus).post(expected);
    }

  @Test
  public void shouldRecognizeIndistinctSounds()
  {
    assertThat(SimpleAudioReader.getSensor().level(-35), IsEqual.equalTo(MeasurementLevel.TOO_LOW));
  }

  @Test
  public void shouldRecognizeQuietSounds()
  {
    assertThat(SimpleAudioReader.getSensor().level(51), IsEqual.equalTo(MeasurementLevel.VERY_LOW));
  }

  @Test
  public void shouldRecognizeAverageSounds()
  {
    assertThat(SimpleAudioReader.getSensor().level(65), IsEqual.equalTo(MeasurementLevel.LOW));
  }

  @Test
  public void shouldRecognizeLoudSounds()
  {
    assertThat(SimpleAudioReader.getSensor().level(75), IsEqual.equalTo(MeasurementLevel.MID));
  }

  @Test
  public void shouldRecognizeVeryLoudSounds()
  {
    assertThat(SimpleAudioReader.getSensor().level(87), IsEqual.equalTo(MeasurementLevel.HIGH));
  }

  @Test
  public void shouldRecognizeTooLoudSounds()
  {
    assertThat(SimpleAudioReader.getSensor().level(101), IsEqual.equalTo(MeasurementLevel.VERY_HIGH));
  }

}
