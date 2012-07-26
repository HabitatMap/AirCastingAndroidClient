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

import pl.llp.aircasting.api.data.CreateSessionResponse;
import pl.llp.aircasting.helper.GZIPHelper;
import pl.llp.aircasting.helper.PhotoHelper;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.repository.SessionRepository;
import pl.llp.aircasting.util.bitmap.BitmapTransformer;
import pl.llp.aircasting.util.http.HttpResult;
import pl.llp.aircasting.util.http.PerformRequest;
import pl.llp.aircasting.util.http.Uploadable;

import com.google.inject.Inject;

import java.io.IOException;

import static pl.llp.aircasting.util.http.HttpBuilder.error;
import static pl.llp.aircasting.util.http.HttpBuilder.http;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/24/11
 * Time: 3:59 PM
 */
public class SessionDriver {
    public static final String SESSION_KEY = "session";
    private static final String SESSIONS_PATH = "/api/sessions.json";
    private static final String USER_SESSION_PATH = "/api/user/sessions/";
    public static final String COMPRESSION = "compression";

    @Inject SessionRepository sessionRepository;
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

  private byte[] gzip(Session session) throws IOException
  {
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
                .from(USER_SESSION_PATH + id + ".json")
                .into(Session.class);
    }
}
