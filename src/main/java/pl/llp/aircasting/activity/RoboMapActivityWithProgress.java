package pl.llp.aircasting.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.google.inject.Inject;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.task.SimpleProgressTask;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.SessionManager;
import roboguice.activity.RoboMapActivity;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 1/16/12
 * Time: 12:35 PM
 */
public abstract class RoboMapActivityWithProgress extends RoboMapActivity implements ActivityWithProgress, AppCompatCallback {
    @Inject SessionManager sessionManager;

    private int progressStyle;
    private ProgressDialog dialog;
    private SimpleProgressTask task;
    public AppCompatDelegate delegate;
    public Toolbar toolbar;
    public DrawerLayout drawerLayout;

    @Override
    public ProgressDialog showProgressDialog(int progressStyle, SimpleProgressTask task) {
        this.progressStyle = progressStyle;
        this.task = task;

        showDialog(SPINNER_DIALOG);
        return dialog;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        this.dialog = SimpleProgressTask.prepareDialog(this, progressStyle);

        return dialog;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDelegate().onCreate(savedInstanceState);

        Object instance = getLastNonConfigurationInstance();
        if (instance != null) {
            ((SimpleProgressTask) instance).setActivity(this);
        }
    }

    public void initToolbar(String title) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.navigation_empty_icon);
        getDelegate().setSupportActionBar(toolbar);
        getDelegate().setTitle(title);
    }

    public void initNavigationDrawer(final Context context) {
        NavigationView navigationView = (NavigationView)findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                switch (menuItem.getItemId()){
                    case R.id.dashboard:
                        if (sessionManager.isSessionSaved())
                        {
                            Session session = sessionManager.getSession();
                            Long sessionId = session.getId();
                            sessionManager.resetSession(sessionId);
                        }
                        Intent intent = new Intent(context, StreamsActivity.class);
                        intent.putExtra("startingAircasting", true);
                        startActivity(intent);
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.settings:
                        startActivity(new Intent(context, SettingsActivity.class));
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.sessions:
                        startActivity(new Intent(context, SessionsActivity.class));
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.about:
                        startActivity(new Intent(context, AboutActivity.class));
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.phone_microphone:
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.external_sensors:
                        drawerLayout.closeDrawers();
                        break;
                }
                return true;
            }
        });

        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close){

            @Override
            public void onDrawerClosed(View v){
                super.onDrawerClosed(v);
            }

            @Override
            public void onDrawerOpened(View v) {
                super.onDrawerOpened(v);
            }
        };

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return task;
    }

    @Override
    public void hideProgressDialog() {
        try {
            dismissDialog(SPINNER_DIALOG);
            removeDialog(SPINNER_DIALOG);
        } catch (IllegalArgumentException e) {
            // Ignore - there was no dialog after all
        }

    }

    public AppCompatDelegate getDelegate() {
        if (delegate == null) {
            delegate = AppCompatDelegate.create(this, this);
        }
        return delegate;
    }

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {
        //let's leave this empty, for now
    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {
        // let's leave this empty, for now
    }

    @Nullable
    @Override
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
        return null;
    }
}
