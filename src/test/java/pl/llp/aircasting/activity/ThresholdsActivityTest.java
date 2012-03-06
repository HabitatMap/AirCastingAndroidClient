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
package pl.llp.aircasting.activity;

import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.R;
import pl.llp.aircasting.SoundLevel;
import pl.llp.aircasting.helper.SettingsHelper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static pl.llp.aircasting.TestHelper.click;
import static pl.llp.aircasting.TestHelper.fill;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/8/11
 * Time: 6:11 PM
 */
@RunWith(InjectedTestRunner.class)
public class ThresholdsActivityTest {
    @Inject ThresholdsActivity activity;

    @Before
    public void setup() {
        activity.onCreate(null);

        activity.settingsHelper = mock(SettingsHelper.class);

        fill(activity, R.id.color_scale_too_loud, "100");
        fill(activity, R.id.color_scale_very_loud, "90");
        fill(activity, R.id.color_scale_loud, "80");
        fill(activity, R.id.color_scale_average, "70");
        fill(activity, R.id.color_scale_quiet, "60");
    }

    @Test
    public void shouldSaveThresholds() {
        click(activity, R.id.save);

        verify(activity.settingsHelper).setThreshold(SoundLevel.TOO_LOUD, 100);
        verify(activity.settingsHelper).setThreshold(SoundLevel.VERY_LOUD, 90);
        verify(activity.settingsHelper).setThreshold(SoundLevel.LOUD, 80);
        verify(activity.settingsHelper).setThreshold(SoundLevel.AVERAGE, 70);
        verify(activity.settingsHelper).setThreshold(SoundLevel.QUIET, 60);

        assertThat(activity.isFinishing(), equalTo(true));
    }

    @Test
    public void shouldFixThresholdsBeforeSaving() {
        activity = spy(activity);
        when(activity.getCurrentFocus()).thenReturn(activity.veryLoudEdit);
        fill(activity, R.id.color_scale_very_loud, "110");

        click(activity, R.id.save);

        verify(activity.settingsHelper).setThreshold(SoundLevel.TOO_LOUD, 111);
        verify(activity.settingsHelper).setThreshold(SoundLevel.VERY_LOUD, 110);

        assertThat(activity.isFinishing(), equalTo(true));
    }

    @Test
    public void shouldResetThresholds() {
        click(activity, R.id.reset);

        verify(activity.settingsHelper).resetThresholds();
        assertThat(activity.isFinishing(), equalTo(true));
    }
}

