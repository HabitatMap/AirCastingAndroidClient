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

        when(soundHelper.thresholdsHelper.getThreshold(SimpleAudioReader.SENSOR_NAME, MeasurementLevel.VERY_HIGH)).thenReturn(60);
        when(soundHelper.thresholdsHelper.getThreshold(SimpleAudioReader.SENSOR_NAME, MeasurementLevel.HIGH)).thenReturn(50);
        when(soundHelper.thresholdsHelper.getThreshold(SimpleAudioReader.SENSOR_NAME, MeasurementLevel.MID)).thenReturn(30);
        when(soundHelper.thresholdsHelper.getThreshold(SimpleAudioReader.SENSOR_NAME, MeasurementLevel.LOW)).thenReturn(10);
        when(soundHelper.thresholdsHelper.getThreshold(SimpleAudioReader.SENSOR_NAME, MeasurementLevel.VERY_LOW)).thenReturn(-20);
    }

    @Test
    public void shouldRecognizeIndistinctSounds() {
        assertThat(soundHelper.soundLevel(-35), equalTo(MeasurementLevel.TOO_LOW));
    }

    @Test
    public void shouldRecognizeQuietSounds() {
        assertThat(soundHelper.soundLevel(-10), equalTo(MeasurementLevel.VERY_LOW));
    }

    @Test
    public void shouldRecognizeAverageSounds() {
        assertThat(soundHelper.soundLevel(15), equalTo(MeasurementLevel.LOW));
    }

    @Test
    public void shouldRecognizeLoudSounds() {
        assertThat(soundHelper.soundLevel(35), equalTo(MeasurementLevel.MID));
    }

    @Test
    public void shouldRecognizeVeryLoudSounds() {
        assertThat(soundHelper.soundLevel(55), equalTo(MeasurementLevel.HIGH));
    }

    @Test
    public void shouldRecognizeTooLoudSounds() {
        assertThat(soundHelper.soundLevel(65), equalTo(MeasurementLevel.VERY_HIGH));
    }

    @Test
    public void shouldAdviseToDisplayAbsoluteAverageData() {
        assertThat(soundHelper.shouldDisplayAbsolute(45), equalTo(true));
    }

    @Test
    public void shouldNotAdviseToDisplayAbsoluteTooLoudData() {
        assertThat(soundHelper.shouldDisplayAbsolute(61), equalTo(false));
    }

    @Test
    public void shouldNotAdviseToDisplayAbsoluteTooQuietData() {
        assertThat(soundHelper.shouldDisplayAbsolute(-21), equalTo(false));
    }

    @Test
    public void shouldReturnLowerOneOnTheBorder() {
        String sensorName = SimpleAudioReader.SENSOR_NAME;
        
        assertThat(soundHelper.soundLevelAbsolute(sensorName, 10.1), equalTo(MeasurementLevel.VERY_LOW));
        assertThat(soundHelper.soundLevelAbsolute(sensorName, 30.1), equalTo(MeasurementLevel.LOW));
        assertThat(soundHelper.soundLevelAbsolute(sensorName, 50.1), equalTo(MeasurementLevel.MID));
        assertThat(soundHelper.soundLevelAbsolute(sensorName, 60.1), equalTo(MeasurementLevel.HIGH));
    }
}
