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
package pl.llp.aircasting.screens;

import com.google.inject.Inject;
import com.xtremelabs.robolectric.shadows.ShadowToast;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.R;
import pl.llp.aircasting.networking.schema.UserInfo;
import pl.llp.aircasting.networking.drivers.UsersDriver;
import pl.llp.aircasting.screens.common.helpers.SettingsHelper;
import pl.llp.aircasting.networking.httpUtils.HttpResult;
import pl.llp.aircasting.networking.httpUtils.Status;
import pl.llp.aircasting.screens.userAccount.SignInActivity;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static pl.llp.aircasting.TestHelper.click;
import static pl.llp.aircasting.TestHelper.fill;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/15/11
 * Time: 11:06 AM
 */
@RunWith(InjectedTestRunner.class)
public class SignInActivityTest {
    @Inject
    SignInActivity activity;

    private HttpResult<UserInfo> result = new HttpResult<UserInfo>();

    @Before
    public void setup() throws IOException, URISyntaxException {
        activity.onCreate(null);

        activity.userDriver = mock(UsersDriver.class);
        activity.settingsHelper = mock(SettingsHelper.class);

        fill(activity, R.id.login, "joe@example.org");
        fill(activity, R.id.password, "password");

        result.setContent(new UserInfo("joe@example.org", "joe", "some_token"));
        result.setStatus(Status.SUCCESS);

        when(activity.userDriver.connect("joe@example.org", "password")).thenReturn(result);
    }

    @Test
    public void shouldConnectToExistingAccount() throws IOException, URISyntaxException {
        click(activity, R.id.ok);

        verify(activity.userDriver).connect("joe@example.org", "password");
        verify(activity.settingsHelper).setAuthToken("some_token");
        verify(activity.settingsHelper).setUserLogin("joe");
        assertThat(activity.isFinishing(), equalTo(true));
    }

    @Test
    public void shouldHandleOtherErrors() {
        result.setStatus(Status.ERROR);

        click(activity, R.id.ok);

        assertThat(ShadowToast.getTextOfLatestToast(), equalTo(activity.getString(R.string.unknown_error)));
        verifyZeroInteractions(activity.settingsHelper);
        assertThat(activity.isFinishing(), equalTo(false));
    }

    @Test
    public void shouldToastOnLoginFailure() throws IOException, URISyntaxException {
        result.setStatus(Status.FAILURE);

        click(activity, R.id.ok);

        verify(activity.userDriver).connect("joe@example.org", "password");
        verifyZeroInteractions(activity.settingsHelper);
        assertThat(ShadowToast.getTextOfLatestToast(), equalTo(activity.getString(R.string.login_error)));
        assertThat(activity.isFinishing(), equalTo(false));
    }
}
