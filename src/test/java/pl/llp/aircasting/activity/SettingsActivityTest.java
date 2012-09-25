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

import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.R;
import pl.llp.aircasting.helper.SettingsHelper;

import android.content.SharedPreferences;
import android.preference.Preference;
import com.google.inject.Inject;
import com.xtremelabs.robolectric.shadows.ShadowToast;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 12/1/11
 * Time: 6:06 PM
 */
@RunWith(InjectedTestRunner.class)
public class SettingsActivityTest {
    @Inject SettingsActivity activity;
    @Inject SharedPreferences preferences;

    @Before
    public void setup() {
        activity.onCreate(null);

        activity.settingsHelper = mock(SettingsHelper.class);

        when(activity.settingsHelper.validateFormat(anyString())).thenReturn(true);
    }

    @Test
    public void shouldComplainAboutInvalidOffset() {
        when(activity.settingsHelper.validateOffset60DB(anyInt())).thenReturn(false);

        activity.offset60DbInputListener.onPreferenceChange(mock(Preference.class), SettingsHelper.MAX_OFFSET_60_DB + 1);

        assertThat(ShadowToast.getTextOfLatestToast(), equalTo(activity.getString(R.string.offset_error)));
    }

    @Test
    public void shouldValidateAveragingInterval() {
        when(activity.settingsHelper.validateAveragingTime()).thenReturn(false);

        activity.onSharedPreferenceChanged(preferences, SettingsHelper.AVERAGING_TIME);

        assertThat(ShadowToast.getTextOfLatestToast(), equalTo(activity.getString(R.string.averaging_time_error)));
    }
}
