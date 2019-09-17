/**
 * AirCasting - Share your Air!
 * Copyright (C) 2011-2012 HabitatMap, Inc.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * You can contact the authors by email at <info@habitatmap.org>
 */
package pl.llp.aircasting.networking.drivers;

import pl.llp.aircasting.networking.schema.CreateSessionResponse;
import pl.llp.aircasting.networking.schema.DeleteSessionResponse;
import pl.llp.aircasting.screens.common.helpers.PhotoHelper;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.util.bitmap.BitmapTransformer;
import pl.llp.aircasting.networking.httpUtils.HttpResult;
import pl.llp.aircasting.networking.httpUtils.PerformRequest;
import pl.llp.aircasting.networking.httpUtils.Uploadable;
import pl.llp.aircasting.networking.schema.ExportSession;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.inject.Inject;

import java.io.IOException;

import static pl.llp.aircasting.networking.httpUtils.HttpBuilder.error;
import static pl.llp.aircasting.networking.httpUtils.HttpBuilder.http;

public class SessionDriver {
    public static final String SESSION_KEY = "session";
    private static final String SESSIONS_PATH = "/api/sessions.json";
    private static final String DELETE_SESSION_PATH = "/api/user/sessions/delete_session";
    private static final String DELETE_SESSION_STREAMS_PATH = "/api/user/sessions/delete_session_streams";
    private static final String USER_SESSION_PATH = "/api/user/sessions/";
    private static final String EMPTY_ID = "empty";
    public static final String COMPRESSION = "compression";
    public static final String STREAM_MEASUREMENTS = "stream_measurements";
    private static final String UPDATE_USER_SESSION_PATH = "/api/user/sessions/update_session";
    private static final String JSON_SUFFIX = ".json";
    private static final String DATA_KEY = "data";
    private static final String EXPORT_PATH = "/api/sessions/export_by_uuid.json";

    @Inject Gson gson;
    @Inject GZIPHelper gzipHelper;
    @Inject PhotoHelper photoHelper;
    @Inject BitmapTransformer bitmapTransformer;

    /**
     * Uploads the given session to the backend
     *
     * @param session The Session to upload
     * @return true on success, false otherwise
     */
    public HttpResult<CreateSessionResponse> create(Session session) {
        String zipped;
        try {
            zipped = new String(gzip(session));
        } catch (IOException e) {
            return error();
        }

        PerformRequest builder = http()
                .post()
                .to(SESSIONS_PATH)
                .with(SESSION_KEY, zipped)
                .with(COMPRESSION, "true");

        builder = attachPhotos(session, builder);

        return builder.into(CreateSessionResponse.class);
    }

    private byte[] gzip(Session session) throws IOException {
        return gzipHelper.zippedSession(session);
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

    public HttpResult<Session> show(long id) {
        return http()
                .get()
                .from(USER_SESSION_PATH + id + JSON_SUFFIX)
                .with(STREAM_MEASUREMENTS, "false")
                .into(Session.class);
    }

    public HttpResult<Session> show(long id, String uuid, Boolean streamMeasurements) {
        return http()
                .get()
                .from(USER_SESSION_PATH + EMPTY_ID + JSON_SUFFIX)
                .with(STREAM_MEASUREMENTS, String.valueOf(streamMeasurements))
                .with("uuid", uuid)
                .into(Session.class);
    }

    public HttpResult<Session> syncToServer(Session session) {
        return http()
                .post()
                .to(UPDATE_USER_SESSION_PATH + JSON_SUFFIX)
                .with(DATA_KEY, gson.toJson(session))
                .into(Session.class);
    }

    public HttpResult<DeleteSessionResponse> deleteSession(Session session) {
        Session copy = new Session();
        copy.setUuid(session.getUUID());

        try {
            String zipped = new String(gzip(copy));
            PerformRequest builder = http()
                    .post()
                    .to(DELETE_SESSION_PATH)
                    .with(SESSION_KEY, zipped)
                    .with(COMPRESSION, "true");
            return builder.into(DeleteSessionResponse.class);
        } catch (IOException e) {
            return error();
        }
    }

    public HttpResult<DeleteSessionResponse> deleteStreams(Session session) {
        Session copy = new Session();
        copy.setUuid(session.getUUID());
        for (MeasurementStream stream : session.getMeasurementStreams()) {
            if (stream.isMarkedForRemoval()) {
                copy.add(stream);
            }
        }

        try {
            String zipped = new String(gzip(copy));
            PerformRequest builder = http()
                    .post()
                    .to(DELETE_SESSION_STREAMS_PATH)
                    .with(SESSION_KEY, zipped)
                    .with(COMPRESSION, "true");

            return builder.into(DeleteSessionResponse.class);
        } catch (IOException e) {
            return error();
        }
    }

    public static AsyncTask<Void,Void,Boolean> exportSessionByEmail(final String email, final String uuid) {
      return new AsyncTask<Void, Void, Boolean>() {
          @Override
          protected Boolean doInBackground(Void... voids) {
              http()
                  .get()
                  .from(EXPORT_PATH)
                  .with("email", (String) email)
                  .with("uuid", uuid)
                  .into(ExportSession.class);
              return true;
          }
      }.execute();
    }

}
