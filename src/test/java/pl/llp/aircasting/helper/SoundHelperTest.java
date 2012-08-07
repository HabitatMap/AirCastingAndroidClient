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

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.MeasurementLevel;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;

import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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
public class SoundHelperTest
{
  @Inject SoundHelper soundHelper;
  private final Sensor SENSOR = SimpleAudioReader.getSensor();

  @Before
  public void setup()
  {
    soundHelper.settingsHelper = mock(SettingsHelper.class);

    when(soundHelper.settingsHelper.getThreshold(SENSOR, MeasurementLevel.VERY_HIGH)).thenReturn(60);
    when(soundHelper.settingsHelper.getThreshold(SENSOR, MeasurementLevel.HIGH)).thenReturn(50);
    when(soundHelper.settingsHelper.getThreshold(SENSOR, MeasurementLevel.MID)).thenReturn(30);
    when(soundHelper.settingsHelper.getThreshold(SENSOR, MeasurementLevel.LOW)).thenReturn(10);
    when(soundHelper.settingsHelper.getThreshold(SENSOR, MeasurementLevel.VERY_LOW)).thenReturn(-20);
  }

  @Test
  public void shouldAdviseToDisplayAbsoluteAverageData()
  {
    assertThat(soundHelper.shouldDisplay(SENSOR, 45), equalTo(true));
  }

  @Test
  public void shouldNotAdviseToDisplayAbsoluteTooLoudData()
  {
    assertThat(soundHelper.shouldDisplay(SENSOR, 101), equalTo(false));
  }

  @Test
  public void shouldNotAdviseToDisplayAbsoluteTooQuietData()
  {
    assertThat(soundHelper.shouldDisplay(SENSOR, -21), equalTo(false));
  }
}
