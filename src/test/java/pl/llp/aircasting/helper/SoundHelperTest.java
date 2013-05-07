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
import pl.llp.aircasting.event.sensor.ThresholdSetEvent;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.internal.MeasurementLevel;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/7/11
 * Time: 3:56 PM
 */
@RunWith(InjectedTestRunner.class)
public class SoundHelperTest
{
  public static final int VALUE_TOO_LOUD = 101;
  public static final int VALUE_TOO_QUIET = -21;
  @Inject SoundHelper soundHelper;
  @Inject EventBus bus;
  private final Sensor SENSOR = SimpleAudioReader.getSensor();

  @Before
  public void setUp() throws Exception
  {
    for (MeasurementLevel level : MeasurementLevel.OBTAINABLE_LEVELS)
    {
      bus.post(new ThresholdSetEvent(SENSOR, level, SENSOR.getThreshold(level)));
    }
  }

  @Test
  public void shouldnot_advise_displaying_normal_values()
  {
    assertThat(soundHelper.shouldDisplay(SENSOR, 45)).isTrue();
  }

  @Test
  public void should_not_advise_displaying_too_loud_data()
  {
    assertThat(soundHelper.shouldDisplay(SENSOR, VALUE_TOO_LOUD)).isFalse();
  }

  @Test
  public void shouldNotAdviseToDisplayAbsoluteTooQuietData()
  {
    assertThat(soundHelper.shouldDisplay(SENSOR, VALUE_TOO_QUIET)).isFalse();
  }

  @Test
  public void should_display_once_thresholds_have_been_changed() throws Exception
  {
    // given

    // when
    bus.post(new ThresholdSetEvent(SENSOR, MeasurementLevel.VERY_HIGH, VALUE_TOO_LOUD + 1));

    // then
    assertThat(soundHelper.shouldDisplay(SENSOR, VALUE_TOO_LOUD)).isTrue();
  }
}
