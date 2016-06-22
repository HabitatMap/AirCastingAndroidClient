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
import pl.llp.aircasting.api.data.CreateFixedSessionsMeasurementResponse;
import pl.llp.aircasting.helper.GZIPHelper;
import pl.llp.aircasting.helper.PhotoHelper;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.FixedSessionsMeasurement;
import pl.llp.aircasting.util.bitmap.BitmapTransformer;
import pl.llp.aircasting.util.http.HttpResult;
import pl.llp.aircasting.util.http.PerformRequest;
import pl.llp.aircasting.util.http.Uploadable;

import java.io.IOException;

import static pl.llp.aircasting.util.http.HttpBuilder.error;
import static pl.llp.aircasting.util.http.HttpBuilder.http;

@Singleton
public class FixedSessionDriver
{
    public static final String SESSION_KEY = "session";
    public static final String COMPRESSION = "compression";
    private static final String CREATE_FIXED_SESSIONS_MEASUREMENT_PATH = "/api/realtime/measurements.json";
    private static final String CREATE_FIXED_SESSION_PATH = "/api/realtime/sessions.json";

    @Inject Gson gson;
    @Inject SessionDriver sessionDriver;
    @Inject GZIPHelper gzipHelper;
    @Inject PhotoHelper photoHelper;
    @Inject BitmapTransformer bitmapTransformer;

    public HttpResult<CreateSessionResponse> create(Session session) {
      String zipped;
      try {
        zipped = new String(gzip(session));
      } catch (IOException e) {
        return error();
      }

      PerformRequest builder = http()
              .post()
              .to(CREATE_FIXED_SESSION_PATH)
              .with(SESSION_KEY, zipped)
              .with(COMPRESSION, "true");

      builder = attachPhotos(session, builder);

      return builder.into(CreateSessionResponse.class);
    }

    private byte[] gzip(Session session) throws IOException
    {
      return gzipHelper.zippedSession(session);
    }

    private byte[] gzip(FixedSessionsMeasurement fixedSessionsMeasurement) throws IOException
    {
      return gzipHelper.zippedFixedSessionsMeasurement(fixedSessionsMeasurement);
    }

    private PerformRequest attachPhotos(Session session, PerformRequest builder) {
      for (int i = 0; i < session.getNotes().size(); i++) {
        Note note = session.getNotes().get(i);

        if (photoHelper.photoExistsLocally(note)) {
          String path = note.getPhotoPath();
          Uploadable uploadable = bitmapTransformer.readScaledBitmap(path);

          builder = builder.upload("photos[]", uploadable);
        } else {
          builder = builder.with("photos[]", "");
        }
      }
      return builder;
    }

    public HttpResult<CreateFixedSessionsMeasurementResponse> create_measurement(FixedSessionsMeasurement fixedSessionsMeasurement) {
      String zipped;
      try {
        zipped = new String(gzip(fixedSessionsMeasurement));
      } catch (IOException e) {
        return error();
      }

      return http()
              .post()
              .to(CREATE_FIXED_SESSIONS_MEASUREMENT_PATH)
              .with("data", zipped)
              .with(COMPRESSION, "true")
              .into(CreateFixedSessionsMeasurementResponse.class);
    }
}
