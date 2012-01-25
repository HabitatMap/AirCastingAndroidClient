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
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.google.inject.Inject;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.SoundLevel;
import pl.llp.aircasting.activity.adapter.AdapterFactory;
import pl.llp.aircasting.activity.adapter.SessionAdapter;
import pl.llp.aircasting.activity.menu.MainMenu;
import pl.llp.aircasting.activity.task.OpenSessionTask;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.SessionManager;
import pl.llp.aircasting.repository.SessionRepository;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/5/11
 * Time: 3:59 PM
 */
public class SessionsActivity extends RoboListActivityWithProgress implements AdapterView.OnItemLongClickListener, View.OnClickListener {
    @Inject SessionRepository sessionRepository;
    @Inject SessionManager sessionManager;
    @Inject AdapterFactory adapterFactory;
    @Inject SettingsHelper settingsHelper;
    @Inject MainMenu mainMenu;
    @Inject Application context;

    @InjectView(R.id.top_bar_quiet) TextView topBarQuiet;
    @InjectView(R.id.top_bar_average) TextView topBarAverage;
    @InjectView(R.id.top_bar_loud) TextView topBarLoud;
    @InjectView(R.id.top_bar_very_loud) TextView topBarVeryLoud;
    @InjectView(R.id.top_bar_too_loud) TextView topBarTooLoud;

    @InjectView(R.id.sync) Button sync;
    @InjectView(R.id.sync_summary) TextView syncSummary;

    @InjectResource(R.string.sync_summary_template) String syncSummaryTemplate;

    Cursor cursor;
    private long sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sessions);

        refreshList();
        getListView().setOnItemLongClickListener(this);

        sync.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateTopBar();
    }

    private void updateTopBar() {
        topBarQuiet.setText("" + settingsHelper.getThreshold(SoundLevel.QUIET));
        topBarAverage.setText("" + settingsHelper.getThreshold(SoundLevel.AVERAGE));
        topBarLoud.setText("" + settingsHelper.getThreshold(SoundLevel.LOUD));
        topBarVeryLoud.setText("" + settingsHelper.getThreshold(SoundLevel.VERY_LOUD));
        topBarTooLoud.setText("" + settingsHelper.getThreshold(SoundLevel.TOO_LOUD));
    }

    private void refreshList() {
        int all = sessionRepository.getSessionsCount();
        int uploaded = sessionRepository.getUploadedCount();
        String text = String.format(syncSummaryTemplate, uploaded, all);
        syncSummary.setText(text);

        boolean uploadPending = all > uploaded;
        sync.setVisibility(uploadPending ? View.VISIBLE : View.GONE);

        cursor = sessionRepository.notDeletedCursor();
        startManagingCursor(cursor);

        SessionAdapter adapter = adapterFactory.getSessionAdapter(this, cursor);
        setListAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sessionRepository.close();
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        viewSession(id);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        Intent intent = new Intent(this, OpenSessionActivity.class);
        sessionId = id;
        startActivityForResult(intent, 0);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case R.id.view:
                viewSession(sessionId);
                break;
            case R.id.delete:
                deleteSession(sessionId);
                break;
            case R.id.edit:
                editSession(sessionId);
                break;
            case R.id.save_button:
                updateSession(data);
                break;
            case R.id.share:
                Intents.shareSession(this, sessionId);
                break;
        }
    }

    private void updateSession(Intent data) {
        Session session = Intents.editSessionResult(data);

        sessionRepository.update(session);
        Intents.triggerSync(context);

        refreshList();
    }

    private void editSession(long id) {
        Session session = sessionRepository.load(id);
        Intents.editSession(this, session);
    }

    private void deleteSession(long id) {
        sessionRepository.markSessionForRemoval(id);
        Intents.triggerSync(context);

        refreshList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return mainMenu.create(this, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mainMenu.handleClick(this, item);
    }

    private void viewSession(long id) {
        if (sessionManager.isSessionStarted()) {
            Toast.makeText(context, R.string.stop_aircasting, Toast.LENGTH_LONG).show();
            return;
        }

        new OpenSessionTask(this) {
            @Override
            protected Session doInBackground(Long... longs) {
                sessionManager.loadSession(longs[0], this);

                return null;
            }

            @Override
            protected void onPostExecute(Session session) {
                super.onPostExecute(session);

                Intent intent = new Intent(getApplicationContext(), SoundTraceActivity.class);
                startActivity(intent);

                finish();
            }
        }.execute(id);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sync:
                Intents.triggerSync(context);
                refreshList();
                break;
        }
    }
}
