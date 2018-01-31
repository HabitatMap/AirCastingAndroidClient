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
package pl.llp.aircasting.api;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.android.Logger;
import pl.llp.aircasting.api.data.CreateSessionResponse;
import pl.llp.aircasting.helper.GZIPHelper;
import pl.llp.aircasting.helper.PhotoHelper;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.storage.repository.RepositoryException;
import pl.llp.aircasting.storage.repository.SessionRepository;
import pl.llp.aircasting.util.bitmap.BitmapTransformer;
import pl.llp.aircasting.util.http.HttpResult;
import pl.llp.aircasting.util.http.PerformRequest;
import pl.llp.aircasting.util.http.Status;
import pl.llp.aircasting.util.http.Uploadable;

import java.io.IOException;
import java.util.Date;

import static pl.llp.aircasting.util.http.HttpBuilder.error;
import static pl.llp.aircasting.util.http.HttpBuilder.http;

@Singleton
public class FixedSessionDriver {
    public static final String SESSION_KEY = "session";
    public static final String COMPRESSION = "compression";
    private static final String CREATE_FIXED_SESSION_PATH = "/api/realtime/sessions.json";
    private static final String SYNC_MEASUREMENTS_PATH = "/api/realtime/sync_measurements.json";

    @Inject GZIPHelper gzipHelper;
    @Inject PhotoHelper photoHelper;
    @Inject BitmapTransformer bitmapTransformer;
    @Inject SessionRepository sessionRepository;

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

    public void downloadNewData(Session session) {
        String uuid = session.getUUID().toString();
        Date lastMeasurementSyncTime = session.getLastMeasurementSyncTime();
        HttpResult<Session> result = syncMeasurements(uuid, lastMeasurementSyncTime);
        Logger.w("last meas date is " + lastMeasurementSyncTime);

        if (result.getStatus() == Status.SUCCESS) {
            Session downloadedSession = result.getContent();
            if (downloadedSession == null) {
                Logger.w("Data for session [" + uuid + "] couldn't be downloaded");
            } else {
                try {
                    sessionRepository.saveNewData(session, downloadedSession);
                } catch (RepositoryException e) {
                    Logger.e("Error saving data for session [" + uuid + "]", e);
                }
            }
        }
    }

    private HttpResult<Session> syncMeasurements(String uuid, Date lastMeasurementSyncTime) {
        PerformRequest builder = http()
                .get()
                .from(SYNC_MEASUREMENTS_PATH)
                .with("uuid", uuid)
                .with("last_measurement_sync", lastMeasurementSyncTime.toString());

        return builder.into(Session.class);
    }
}
