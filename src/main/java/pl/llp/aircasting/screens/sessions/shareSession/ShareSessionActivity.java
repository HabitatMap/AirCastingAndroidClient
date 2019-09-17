package pl.llp.aircasting.screens.sessions.shareSession;

import android.app.Activity;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;

import pl.llp.aircasting.networking.drivers.SessionDriver;
import pl.llp.aircasting.screens.common.base.DialogActivity;
import pl.llp.aircasting.screens.common.base.SimpleProgressTask;
import pl.llp.aircasting.screens.common.helpers.NoOp;
import pl.llp.aircasting.screens.common.helpers.SettingsHelper;
import pl.llp.aircasting.screens.common.ToastHelper;
import pl.llp.aircasting.screens.common.sessionState.CurrentSessionManager;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.screens.sessions.CSVHelper;
import pl.llp.aircasting.screens.sessions.OpenSessionTask;
import pl.llp.aircasting.storage.repository.SessionRepository;

import android.app.Application;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.inject.Inject;

import java.io.IOException;

import roboguice.inject.InjectResource;

public class ShareSessionActivity extends DialogActivity implements View.OnClickListener, SensorItemViewMvcImpl.Listener {
    @InjectResource(R.string.share_title)
    String shareTitle;
    @InjectResource(R.string.share_file)
    String shareChooserTitle;
    @InjectResource(R.string.session_file_template)
    String shareText;

    @Inject ShareHelper shareHelper;
    @Inject CurrentSessionManager currentSessionManager;
    @Inject SessionRepository sessionRepository;
    @Inject SettingsHelper settingsHelper;
    @Inject CSVHelper csvHelper;

    @Inject Application context;

    private Session session;
    private ShareSessionViewMvcImpl mShareSessionView;
    private CharSequence mSelectedSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mShareSessionView = new ShareSessionViewMvcImpl(this, null);
        setContentView(mShareSessionView.getRootView());
        initDialogToolbar("Share Session");

        mShareSessionView.registerListener((View.OnClickListener) this);
        mShareSessionView.registerListener((SensorItemViewMvcImpl.Listener) this);
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

        mShareSessionView.bindData(session);
        mShareSessionView.toggleLink(session.isLocationless());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.share_link:
                mShareSessionView.showSensors();
                break;
            case R.id.share_file:
                shareFile();
                break;
            case R.id.send_file:
                sendFile();
                break;
        }
    }

    private void shareFile() {
        if (session.isLocationless()) {
            shareLocalFile();
        } else {
            mShareSessionView.showEmailField();
        }
    }

    private void sendFile() {
        String email = mShareSessionView.getEmailAddress();
        String uuid = session.getUUID().toString();

        SessionDriver.exportSessionByEmail(email, uuid);

        finish();

        ToastHelper.show(context, R.string.session_exported, Toast.LENGTH_LONG);
    }

    private void shareLink() {
        if (session.getLocation() != null) {
            shareHelper.shareLink(this, session, mSelectedSensor);
        } else if (settingsHelper.hasCredentials()) {
            ToastHelper.show(context, R.string.session_not_uploaded, Toast.LENGTH_LONG);
        } else {
            ToastHelper.show(context, R.string.account_reminder, Toast.LENGTH_LONG);
        }
        finish();
    }

    private void shareLocalFile() {
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

    @Override
    public void onSensorSelected(View view) {
        mSelectedSensor = ((TextView) view.findViewById(R.id.sensor_name)).getText();
        shareLink();
    }
}
