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

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.event.sensor.AudioReaderErrorEvent;
import pl.llp.aircasting.event.sensor.SensorEvent;
import pl.llp.aircasting.helper.SettingsHelper;

import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 12/21/11
 * Time: 2:23 PM
 */
@RunWith(InjectedTestRunner.class)
public class SimpleAudioReaderTest {
    public static final int SAMPLE_RATE = 44100;

    @Inject SimpleAudioReader audioReader;

    @Before
    public void setup() {
        audioReader.audioReader = mock(AudioReader.class);
        audioReader.settingsHelper = mock(SettingsHelper.class);
        audioReader.signalPower = mock(SignalPower.class);
        audioReader.eventBus = mock(EventBus.class);
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

        audioReader.start();
        audioReader.onReadComplete(new short[0]);
        audioReader.onReadComplete(new short[0]);

        SensorEvent expected1 = new SensorEvent("Phone microphone", "Sound level", "decibels", "dB", 12.3);
        SensorEvent expected2 = new SensorEvent("Phone microphone", "Sound level", "decibels", "dB", 12.4);
        verify(audioReader.eventBus).post(expected1);
        verify(audioReader.eventBus).post(expected2);
    }

    @Test
    public void shouldIgnoreBatchesOfFaultyData() {
        when(audioReader.signalPower.calculatePowerDb(Mockito.any(short[].class)))
                .thenReturn(null)
                .thenReturn(2.2);

        audioReader.start();
        audioReader.onReadComplete(new short[0]);
        audioReader.onReadComplete(new short[0]);

        SensorEvent expected = new SensorEvent("Phone microphone", "Sound level", "decibels", "dB", 2.2);
        verify(audioReader.eventBus).post(expected);
    }
}
