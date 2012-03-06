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
import com.xtremelabs.robolectric.shadows.ShadowToast;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.llp.aircasting.InjectedTestRunner;
import pl.llp.aircasting.R;
import pl.llp.aircasting.api.UsersDriver;
import pl.llp.aircasting.util.http.HttpResult;
import pl.llp.aircasting.util.http.Status;

import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pl.llp.aircasting.TestHelper.click;
import static pl.llp.aircasting.TestHelper.fill;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/29/11
 * Time: 3:17 PM
 */
@RunWith(InjectedTestRunner.class)
public class ResetPasswordActivityTest {
    @Inject ResetPasswordActivity activity;
    private HttpResult<Void> result;

    @Before
    public void setup() {
        activity.onCreate(null);

        result = new HttpResult<Void>();
        result.setStatus(Status.SUCCESS);

        activity.userDriver = mock(UsersDriver.class);
        when(activity.userDriver.resetPassword("joe@example.org")).thenReturn(result);

        fill(activity, R.id.login, "joe@example.org");
    }

    @Test
    public void shouldResetPassword() throws URISyntaxException {
        click(activity, R.id.ok);

        verify(activity.userDriver).resetPassword("joe@example.org");
        assertThat(ShadowToast.getTextOfLatestToast(), equalTo(activity.getString(R.string.reset_email_sent)));
        assertThat(activity.isFinishing(), equalTo(true));
    }
}
