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
package pl.llp.aircasting.api;

import pl.llp.aircasting.api.data.UserInfo;
import pl.llp.aircasting.util.http.HttpResult;

import static pl.llp.aircasting.util.http.HttpBuilder.http;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/14/11
 * Time: 4:35 PM
 */
public class UsersDriver {
    private static final String USER_PATH = "/api/user.json";
    private static final String RESET_PASSWORD_PATH = "/users/password.json";

    public HttpResult<UserInfo> create(String email, String username, String password, boolean sendEmails) {
        return http()
                .post()
                .to(USER_PATH)
                .with("user[email]", email)
                .with("user[username]", username)
                .with("user[password]", password)
                .with("user[send_emails]", "" + sendEmails)
                .into(UserInfo.class);
    }

    public HttpResult<UserInfo> connect(String login, String password) {
        return http()
                .get()
                .from(USER_PATH)
                .authenticate(login, password)
                .into(UserInfo.class);
    }

    public HttpResult<Void> resetPassword(String login) {
        return http()
                .post()
                .to(RESET_PASSWORD_PATH)
                .with("user[login]", login)
                .execute();
    }
}
