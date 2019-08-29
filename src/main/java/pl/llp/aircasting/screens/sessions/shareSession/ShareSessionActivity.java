package pl.llp.aircasting.screens.sessions.shareSession;

import android.app.Activity;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;

import android.os.AsyncTask;
import pl.llp.aircasting.networking.schema.ExportSession;
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
import static pl.llp.aircasting.networking.httpUtils.HttpBuilder.http;



import android.app.Application;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.google.inject.Inject;
import roboguice.inject.InjectResource;

import java.io.IOException;

public class ShareSessionActivity extends DialogActivity implements View.OnClickListener, SensorItemViewMvcImpl.Listener {
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
    private CharSequence mSelectedSensor;
    private CharSequence emailAddres;

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
                mShareSessionView.showEmailField();
                break;
            case R.id.send_file:
                sendFile();
                break;
        }
    }

    private static final String EXPORT_PATH = "/api/sessions/export_by_uuid.json";

    private void sendFile() {
        emailAddres = mShareSessionView.emailAddress();
        session = currentSessionManager.getCurrentSession();


        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                http()
                        .get()
                        .from(EXPORT_PATH)
                        .with("email", (String) emailAddres)
//                        .with("uuid", session.getUUID().toString())
                        .with("uuid", "c77413f4-542f-4892-aa53-340b9a9308b8")
                        .into(ExportSession.class);
                return true;
            }
        }.execute();
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

    @Override
    public void onSensorSelected(View view) {
        mSelectedSensor = ((TextView) view.findViewById(R.id.sensor_name)).getText();
        shareLink();
    }
}
