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

import android.app.Application;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.inject.Inject;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.task.OpenSessionTask;
import pl.llp.aircasting.activity.task.SimpleProgressTask;
import pl.llp.aircasting.helper.CSVHelper;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.helper.ShareHelper;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.SessionManager;
import pl.llp.aircasting.repository.SessionRepository;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 12/5/11
 * Time: 12:43 PM
 */
public class ShareSessionActivity extends DialogActivity implements View.OnClickListener {
    private static final String TAG = ShareSessionActivity.class.getSimpleName();

    @InjectView(R.id.share_file) Button shareFile;
    @InjectView(R.id.share_link) Button shareLink;

    @InjectResource(R.string.share_title) String shareTitle;
    @InjectResource(R.string.share_file) String shareChooserTitle;
    @InjectResource(R.string.session_file_template) String shareText;

    @Inject ShareHelper shareHelper;
    @Inject SessionManager sessionManager;
    @Inject CSVHelper csvHelper;
    @Inject SessionRepository sessionRepository;
    @Inject SettingsHelper settingsHelper;

    @Inject Application context;

    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.share_session);

        shareFile.setOnClickListener(this);
        shareLink.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sessionRepository.close();
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
        if (getIntent().hasExtra(Intents.SESSION_ID)) {
            long sessionId = getIntent().getLongExtra(Intents.SESSION_ID, 0);
            session = sessionRepository.load(sessionId);
        } else {
            session = sessionManager.getSession();
        }

        if (session.getLocation() != null) {
            shareHelper.shareLink(this, session);
        } else if (settingsHelper.hasCredentials()) {
            Toast.makeText(context, R.string.session_not_uploaded, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, R.string.account_reminder, Toast.LENGTH_LONG).show();
        }
        finish();
    }

    private void shareFile() {
        if (sessionManager.isSessionSaved()) {
            session = sessionManager.getSession();
            prepareAndShare();
        } else {
            loadSession();
        }
    }

    private void prepareAndShare() {
        //noinspection unchecked
        new SimpleProgressTask<Void, Void, Uri>(this) {
            @Override
            protected Uri doInBackground(Void... voids) {
                try {
                    return csvHelper.prepareCSV(ShareSessionActivity.this, session);
                } catch (IOException e) {
                    Log.e(TAG, "Error while creating session CSV", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Uri uri) {
                super.onPostExecute(uri);

                if (uri == null) {
                    Toast.makeText(context, R.string.unknown_error, Toast.LENGTH_SHORT).show();
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
                session = sessionRepository.loadEager(ids[0], this);
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
