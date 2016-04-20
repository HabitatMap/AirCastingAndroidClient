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
import com.google.inject.Singleton;
import pl.llp.aircasting.api.data.CreateSessionResponse;
import pl.llp.aircasting.api.data.CreateRealtimeMeasurementResponse;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.RealtimeSession;
import pl.llp.aircasting.util.http.HttpResult;

import static pl.llp.aircasting.util.http.HttpBuilder.http;

@Singleton
public class RealtimeSessionDriver
{
    private static final String REALTIME_MEASUREMENT_PATH = "/api/realtime/measurements.json";

    @Inject Gson gson;
    @Inject SessionDriver sessionDriver;

    public HttpResult<CreateSessionResponse> create(Session session) {
      return sessionDriver.create(session);
    }

    public HttpResult<CreateRealtimeMeasurementResponse> create_measurement(RealtimeSession realtimeSession) {
      String json = gson.toJson(realtimeSession);

        return http()
                .post()
                .to(REALTIME_MEASUREMENT_PATH)
                .with("data", json)
                .into(CreateRealtimeMeasurementResponse.class);
    }
}
