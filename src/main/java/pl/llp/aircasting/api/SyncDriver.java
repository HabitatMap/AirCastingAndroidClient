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

import com.google.gson.Gson;
import com.google.inject.Inject;
import pl.llp.aircasting.api.data.SyncResponse;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.util.http.HttpResult;

import static pl.llp.aircasting.util.http.HttpBuilder.http;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 12/7/11
 * Time: 2:47 PM
 */
public class SyncDriver {
    @Inject Gson gson;

    public static final String SYNC_PATH = "/api/user/sessions/sync.json";

    public HttpResult<SyncResponse> sync(Iterable<Session> sessions) {
        String json = gson.toJson(sessions);

        return http()
                .post()
                .to(SYNC_PATH)
                .with("data", json)
                .into(SyncResponse.class);
    }
}
