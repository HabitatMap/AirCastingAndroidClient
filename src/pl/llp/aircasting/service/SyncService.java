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
package pl.llp.aircasting.service;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.google.common.base.Predicate;
import com.google.inject.Inject;
import pl.llp.aircasting.api.SessionDriver;
import pl.llp.aircasting.api.SyncDriver;
import pl.llp.aircasting.api.data.CreateSessionResponse;
import pl.llp.aircasting.api.data.SyncResponse;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.repository.SessionRepository;
import pl.llp.aircasting.util.SyncState;
import pl.llp.aircasting.util.http.HttpResult;
import pl.llp.aircasting.util.http.Status;
import roboguice.service.RoboIntentService;

import java.util.UUID;

import static com.google.common.collect.Iterables.find;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/25/11
 * Time: 4:08 PM
 */
public class SyncService extends RoboIntentService {
    @Inject ConnectivityManager connectivityManager;
    @Inject SessionRepository sessionRepository;
    @Inject SyncDriver syncDriver;
    @Inject SettingsHelper settingsHelper;
    @Inject SessionDriver sessionDriver;
    @Inject SyncState syncState;

    public SyncService() {
        super(SyncService.class.getSimpleName());
    }

    @Override
    public void onDestroy() {
        sessionRepository.close();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            syncState.setInProgress(true);
            if (canUpload()) {
                sync();
            }
        } finally {
            syncState.setInProgress(false);
        }
    }

    private void sync() {
        Iterable<Session> sessions = sessionRepository.all();
        HttpResult<SyncResponse> result = syncDriver.sync(sessions);
        SyncResponse syncResponse = result.getContent();

        if (syncResponse != null) {
            sessionRepository.deleteMarked();
            uploadSessions(syncResponse.getUpload());

            downloadSessions(syncResponse.getDownload());
        }
    }

    private boolean canUpload() {
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null
                && networkInfo.isConnected()
                && settingsHelper.hasCredentials();
    }

    private void uploadSessions(UUID[] uuids) {
        for (UUID uuid : uuids) {
            Session session = sessionRepository.loadEager(uuid);
            if (session != null) {
                HttpResult<CreateSessionResponse> result = sessionDriver.create(session);

                if (result.getStatus() == Status.SUCCESS) {
                    updateSession(session, result.getContent());
                }
            }
        }
    }

    private void updateSession(Session session, CreateSessionResponse sessionResponse) {
        session.setLocation(sessionResponse.getLocation());

        for (CreateSessionResponse.Note responseNote : sessionResponse.getNotes()) {
            final int number = responseNote.getNumber();

            Note note = find(session.getNotes(), new Predicate<Note>() {
                @Override
                public boolean apply(Note note) {
                    return note.getNumber() == number;
                }
            });

            note.setPhotoPath(responseNote.getPhotoLocation());
        }

        sessionRepository.update(session);
    }

    private void downloadSessions(long[] ids) {
        for (long id : ids) {
            HttpResult<Session> result = sessionDriver.show(id);

            if (result.getStatus() == Status.SUCCESS) {
                sessionRepository.save(result.getContent());
            }
        }
    }
}
