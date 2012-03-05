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
import pl.llp.aircasting.SoundLevel;

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
        SettingsHelper settingsHelper = mock(SettingsHelper.class);
        soundHelper.calibrationHelper.settingsHelper = settingsHelper;
        soundHelper.settingsHelper = settingsHelper;

        when(soundHelper.settingsHelper.getCalibration()).thenReturn(10);
        when(soundHelper.settingsHelper.getOffset60DB()).thenReturn(0);

        when(soundHelper.settingsHelper.getThreshold(SoundLevel.TOO_LOUD)).thenReturn(60);
        when(soundHelper.settingsHelper.getThreshold(SoundLevel.VERY_LOUD)).thenReturn(50);
        when(soundHelper.settingsHelper.getThreshold(SoundLevel.LOUD)).thenReturn(30);
        when(soundHelper.settingsHelper.getThreshold(SoundLevel.AVERAGE)).thenReturn(10);
        when(soundHelper.settingsHelper.getThreshold(SoundLevel.QUIET)).thenReturn(-20);
    }

    @Test
    public void shouldRecognizeIndistinctSounds() {
        assertThat(soundHelper.soundLevel(-35), equalTo(SoundLevel.INDISTINCT));
    }

    @Test
    public void shouldRecognizeQuietSounds() {
        assertThat(soundHelper.soundLevel(-10), equalTo(SoundLevel.QUIET));
    }

    @Test
    public void shouldRecognizeAverageSounds() {
        assertThat(soundHelper.soundLevel(5), equalTo(SoundLevel.AVERAGE));
    }

    @Test
    public void shouldRecognizeLoudSounds() {
        assertThat(soundHelper.soundLevel(25), equalTo(SoundLevel.LOUD));
    }

    @Test
    public void shouldRecognizeVeryLoudSounds() {
        assertThat(soundHelper.soundLevel(45), equalTo(SoundLevel.VERY_LOUD));
    }

    @Test
    public void shouldRecognizeTooLoudSounds() {
        assertThat(soundHelper.soundLevel(55), equalTo(SoundLevel.TOO_LOUD));
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
        assertThat(soundHelper.soundLevelAbsolute(10.1), equalTo(SoundLevel.QUIET));
        assertThat(soundHelper.soundLevelAbsolute(30.1), equalTo(SoundLevel.AVERAGE));
        assertThat(soundHelper.soundLevelAbsolute(50.1), equalTo(SoundLevel.LOUD));
        assertThat(soundHelper.soundLevelAbsolute(60.1), equalTo(SoundLevel.VERY_LOUD));
    }
}
