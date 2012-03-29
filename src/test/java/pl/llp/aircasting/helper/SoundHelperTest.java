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
package pl.llp.aircasting.helper;

import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.MeasurementLevel;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/7/11
 * Time: 3:56 PM
 */
@RunWith(InjectedTestRunner.class)
public class SoundHelperTest {
    @Inject SoundHelper soundHelper;

    @Before
    public void setup() {
        soundHelper.thresholdsHelper = mock(ThresholdsHelper.class);

        when(soundHelper.thresholdsHelper.getThreshold(SimpleAudioReader.getSensor(), MeasurementLevel.VERY_HIGH)).thenReturn(60);
        when(soundHelper.thresholdsHelper.getThreshold(SimpleAudioReader.getSensor(), MeasurementLevel.HIGH)).thenReturn(50);
        when(soundHelper.thresholdsHelper.getThreshold(SimpleAudioReader.getSensor(), MeasurementLevel.MID)).thenReturn(30);
        when(soundHelper.thresholdsHelper.getThreshold(SimpleAudioReader.getSensor(), MeasurementLevel.LOW)).thenReturn(10);
        when(soundHelper.thresholdsHelper.getThreshold(SimpleAudioReader.getSensor(), MeasurementLevel.VERY_LOW)).thenReturn(-20);
    }

    @Test
    public void shouldRecognizeIndistinctSounds() {
        assertThat(soundHelper.level(SimpleAudioReader.getSensor(), -35), equalTo(MeasurementLevel.TOO_LOW));
    }

    @Test
    public void shouldRecognizeQuietSounds() {
        assertThat(soundHelper.level(SimpleAudioReader.getSensor(), -10), equalTo(MeasurementLevel.VERY_LOW));
    }

    @Test
    public void shouldRecognizeAverageSounds() {
        assertThat(soundHelper.level(SimpleAudioReader.getSensor(), 15), equalTo(MeasurementLevel.LOW));
    }

    @Test
    public void shouldRecognizeLoudSounds() {
        assertThat(soundHelper.level(SimpleAudioReader.getSensor(), 35), equalTo(MeasurementLevel.MID));
    }

    @Test
    public void shouldRecognizeVeryLoudSounds() {
        assertThat(soundHelper.level(SimpleAudioReader.getSensor(), 55), equalTo(MeasurementLevel.HIGH));
    }

    @Test
    public void shouldRecognizeTooLoudSounds() {
        assertThat(soundHelper.level(SimpleAudioReader.getSensor(), 65), equalTo(MeasurementLevel.VERY_HIGH));
    }

    @Test
    public void shouldAdviseToDisplayAbsoluteAverageData() {
        assertThat(soundHelper.shouldDisplay(SimpleAudioReader.getSensor(), 45), equalTo(true));
    }

    @Test
    public void shouldNotAdviseToDisplayAbsoluteTooLoudData() {
        assertThat(soundHelper.shouldDisplay(SimpleAudioReader.getSensor(), 61), equalTo(false));
    }

    @Test
    public void shouldNotAdviseToDisplayAbsoluteTooQuietData() {
        assertThat(soundHelper.shouldDisplay(SimpleAudioReader.getSensor(), -21), equalTo(false));
    }

    @Test
    public void shouldReturnLowerOneOnTheBorder() {
        Sensor sensor = SimpleAudioReader.getSensor();
        
        assertThat(soundHelper.level(sensor, 10.1), equalTo(MeasurementLevel.VERY_LOW));
        assertThat(soundHelper.level(sensor, 30.1), equalTo(MeasurementLevel.LOW));
        assertThat(soundHelper.level(sensor, 50.1), equalTo(MeasurementLevel.MID));
        assertThat(soundHelper.level(sensor, 60.1), equalTo(MeasurementLevel.HIGH));
    }
}
