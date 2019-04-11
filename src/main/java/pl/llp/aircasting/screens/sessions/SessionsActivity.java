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
package pl.llp.aircasting.screens.sessions;

import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.view.ActionMode;
import com.google.common.eventbus.Subscribe;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.screens.common.ToastHelper;
import pl.llp.aircasting.screens.common.base.RoboListActivityWithProgress;
import pl.llp.aircasting.event.network.SyncStateChangedEvent;
import pl.llp.aircasting.screens.common.helpers.NoOp;
import pl.llp.aircasting.screens.common.helpers.SelectSensorHelper;
import pl.llp.aircasting.screens.common.helpers.SettingsHelper;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.screens.common.sessionState.ViewingSessionsManager;
import pl.llp.aircasting.sessionSync.SyncBroadcastReceiver;
import pl.llp.aircasting.storage.db.UncalibratedMeasurementCalibrator;
import pl.llp.aircasting.storage.repository.SessionRepository;

import android.app.Application;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import pl.llp.aircasting.util.SyncState;
import roboguice.inject.InjectView;

import java.util.List;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class SessionsActivity extends RoboListActivityWithProgress implements ActivityCompat.OnRequestPermissionsResultCallback, AppCompatCallback {
    @Inject SessionAdapterFactory sessionAdapterFactory;
    @Inject SelectSensorHelper selectSensorHelper;
    @Inject SessionRepository sessionRepository;
    @Inject ViewingSessionsManager viewingSessionsManager;
    @Inject SettingsHelper settingsHelper;
    @Inject Application context;
    @Inject EventBus eventBus;
    @Inject UncalibratedMeasurementCalibrator calibrator;
    @Inject SyncBroadcastReceiver syncBroadcastReceiver;
    @Inject SyncState syncState;

    @InjectView(R.id.sessions_swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshList();
        }
    };

    private static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 3;

    private SessionAdapter sessionAdapter;
    private long sessionId;
    private String sessionUUID;
    private boolean calibrationAttempted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        calibrateOldRecords();
        setContentView(R.layout.sessions);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Intents.triggerSync(SessionsActivity.this);
                refreshList();
            }
        });

        getDelegate().onCreate(savedInstanceState);
        initToolbar("Sessions");
        initNavigationDrawer();
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshList();
        showSpinnerIfSyncInProgress();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intents.ACTION_SYNC_UPDATE);
        registerReceiver(broadcastReceiver, filter);

        registerReceiver(syncBroadcastReceiver, SyncBroadcastReceiver.INTENT_FILTER);
        eventBus.register(this);
    }

    private void showSpinnerIfSyncInProgress() {
        if (syncState.isInProgress()) {
            swipeRefreshLayout.setRefreshing(true);
        }
    }

    private void refreshItems() {
        List<Session> sessions = sessionRepository.notDeletedSessions();

        if (sessionAdapter == null) {
            sessionAdapter = sessionAdapterFactory.getSessionAdapter(this);
            setListAdapter(sessionAdapter);
        }

        sessionAdapter.setSessions(sessions);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(syncBroadcastReceiver);
        eventBus.unregister(this);
    }

    @Subscribe
    public void onEvent(SyncStateChangedEvent event) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void refreshList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refreshItems();
            }
        });
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        Session s = sessionAdapter.getSession(position);
        sessionId = s.getId();
        sessionUUID = s.getUUID().toString();
        Intent intent;

        if (s.isFixed())
            intent = new Intent(this, OpenFixedSessionActivity.class);
        else
            intent = new Intent(this, OpenMobileSessionActivity.class);

        startActivityForResult(intent, 0);
    }

    @Override
    public void onProfileClick(View view) {
        super.onProfileClick(view);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case R.id.view:
                viewSession();
                break;
            case R.id.delete_session:
                deleteSession(sessionId);
                break;
            case R.id.edit:
                editSession(sessionId);
                break;
            case R.id.save_button:
                updateSession(data);
                break;
            case R.id.share:
                if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{ WRITE_EXTERNAL_STORAGE }, MY_PERMISSIONS_REQUEST_WRITE_STORAGE);
                } else {
                    Intents.shareSession(this, sessionId);
                }

                break;
            case R.id.continue_streaming:
                continueAircastingSession();
                finish();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intents.shareSession(this, sessionId);
                }
                return;
            }
        }
    }

    private void updateSession(Intent data) {
        Session session = Intents.editSessionResult(data);

        sessionRepository.update(session);
        Intents.triggerSync(context);

        refreshList();
    }

    private void continueAircastingSession() {

        viewSession();
        viewingSessionsManager.continueStreaming(sessionId, sessionUUID);
        Intents.continueSessionStreaming(this, sessionUUID);
    }

    private void editSession(long id) {
        Session session = sessionRepository.loadShallow(id);
        Intents.editSession(this, session);
    }

    private void deleteSession(final long id) {
        AlertDialog.Builder b = new AlertDialog.Builder(SessionsActivity.this);
        b.setMessage("Delete session?").
                setCancelable(true).
                setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sessionRepository.markSessionForRemoval(id);
                        refreshList();
                    }
                }).setNegativeButton("No", NoOp.dialogOnClick());
        AlertDialog dialog = b.create();
        dialog.show();
    }

    private void viewSession() {
        new OpenSessionTask(this) {
            @Override
            protected void onPreExecute() {
                viewingSessionsManager.addLoadingSession(sessionId);
                sessionAdapter.notifyDataSetChanged();
            }

            @Override
            protected Session doInBackground(Long... longs) {
                viewingSessionsManager.view(longs[0], NoOp.progressListener());
                return null;
            }

            @Override
            protected void onPostExecute(Session session) {
                super.onPostExecute(session);

                sessionAdapter.notifyDataSetChanged();
            }
        }.execute(sessionId);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case SelectSensorHelper.DIALOG_ID:
                return selectSensorHelper.chooseSensor(this);
            default:
                return super.onCreateDialog(id);
        }
    }

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {
    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {
    }

    @android.support.annotation.Nullable
    @Override
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
        return null;
    }

    private void calibrateOldRecords() {
        if (calibrationAttempted)
            return;

        calibrationAttempted = true;
        if (calibrator.sessionsToCalibrate() > 0) {
            CalibrateSessionsTask task = new CalibrateSessionsTask(this, calibrator);
            task.execute();
        }
    }
}
