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
package pl.llp.aircasting.screens.sessions.shareSession;

import android.app.Activity;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.screens.common.base.DialogActivity;
import pl.llp.aircasting.screens.common.base.SimpleProgressTask;
import pl.llp.aircasting.screens.common.helpers.NoOp;
import pl.llp.aircasting.screens.sessions.CSVHelper;
import pl.llp.aircasting.screens.sessions.OpenSessionTask;
import pl.llp.aircasting.util.Logger;
import pl.llp.aircasting.screens.common.helpers.SettingsHelper;
import pl.llp.aircasting.screens.common.ToastHelper;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionManager;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.storage.repository.SessionRepository;

import android.app.Application;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.inject.Inject;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

import java.io.IOException;

public class ShareSessionActivity extends DialogActivity implements View.OnClickListener {
    @InjectResource(R.string.share_title)
    String shareTitle;
    @InjectResource(R.string.share_file)
    String shareChooserTitle;
    @InjectResource(R.string.session_file_template)
    String shareText;

    @Inject ShareHelper shareHelper;
    @Inject CurrentSessionManager currentSessionManager;
    @Inject CSVHelper csvHelper;
    @Inject SessionRepository sessionRepository;
    @Inject SettingsHelper settingsHelper;

    @Inject Application context;

    private Session session;
    private ShareSessionViewMvcImpl mShareSessionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mShareSessionView = new ShareSessionViewMvcImpl(this, null);
        setContentView(mShareSessionView.getRootView());
        initDialogToolbar("Share Session");
        mShareSessionView.registerListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getIntent().hasExtra(Intents.SESSION_ID)) {
            long sessionId = getIntent().getLongExtra(Intents.SESSION_ID, 0);
            session = sessionRepository.loadShallow(sessionId);
        } else {
            session = currentSessionManager.getCurrentSession();
        }

        mShareSessionView.toggleLink(session.isLocationless());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.share_link:
                shareLink();
                break;
            case R.id.share_file:
                shareFile();
                break;
        }
    }

    private void shareLink() {
        if (session.getLocation() != null) {
            shareHelper.shareLink(this, session);
        } else if (settingsHelper.hasCredentials()) {
            ToastHelper.show(context, R.string.session_not_uploaded, Toast.LENGTH_LONG);
        } else {
            ToastHelper.show(context, R.string.account_reminder, Toast.LENGTH_LONG);
        }
        finish();
    }

    private void shareFile() {
        if (currentSessionManager.isSessionRecording()) {
            session = currentSessionManager.getCurrentSession();
            prepareAndShare();
        } else {
            loadSession();
        }
    }

    private void prepareAndShare() {
        //noinspection unchecked
        final Activity context = this;
        new SimpleProgressTask<Void, Void, Uri>(this) {
            @Override
            protected Uri doInBackground(Void... voids) {
                try {
                    return csvHelper.prepareCSV(context, session);
                } catch (IOException e) {
                    Logger.e("Error while creating session CSV", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Uri uri) {
                super.onPostExecute(uri);

                if (uri == null) {
                    ToastHelper.show(context, R.string.unknown_error, Toast.LENGTH_SHORT);
                } else {
                    Intents.shareCSV(ShareSessionActivity.this, uri, shareChooserTitle, shareTitle, shareText);
                }

                finish();
            }
        }.execute();
    }

    private void loadSession() {
        long id = getIntent().getLongExtra(Intents.SESSION_ID, 0);

        new OpenSessionTask(this) {
            @Override
            protected Session doInBackground(Long... ids) {
                session = sessionRepository.loadFully(ids[0], NoOp.progressListener());
                return session;
            }

            @Override
            protected void onPostExecute(Session session) {
                super.onPostExecute(session);

                prepareAndShare();
            }
        }.execute(id);
    }
}
