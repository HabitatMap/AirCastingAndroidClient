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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.google.inject.Inject;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.task.SimpleProgressTask;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.SessionManager;
import roboguice.activity.RoboListActivity;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 1/17/12
 * Time: 3:19 PM
 */
public class RoboListActivityWithProgress extends RoboListActivity implements ActivityWithProgress, AppCompatCallback {
    @Inject SessionManager sessionManager;
    @Inject SettingsHelper settingsHelper;
    @Inject Context context;

    public AppCompatDelegate delegate;
    public Toolbar toolbar;
    public DrawerLayout drawerLayout;
    private int progressStyle;
    private ProgressDialog dialog;
    private SimpleProgressTask task;
    private NavigationView navigationView;
    private View navHeader;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        navigationView = (NavigationView)findViewById(R.id.navigation_view);
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
    public void onStart() {
        super.onStart();
        getDelegate().onStart();

        setDrawerHeader();
    }

    @Override
    public void onStop() {
        super.onStop();
        getDelegate().onStop();

        navigationView.removeHeaderView(navHeader);
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();

        navigationView.removeHeaderView(navHeader);
        setDrawerHeader();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
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
    public void onSupportActionModeStarted(ActionMode mode) { }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) { }

    @Nullable
    @Override
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
        return null;
    }

    public void onLogInClick(View view) {
        signInOrOut();
    }

    private void signInOrOut()
    {
        if (settingsHelper.hasCredentials())
        {
            startActivity(new Intent(this, SignOutActivity.class));
        }
        else
        {
            startActivity(new Intent(this, ProfileActivity.class));
        }
    }

    private void setDrawerHeader() {
        navHeader = chooseHeaderView();
        navigationView.addHeaderView(navHeader);
        View header = navigationView.getHeaderView(0);
        TextView email = (TextView) header.findViewById(R.id.profile_name);

        if (email != null) {
            email.setText(settingsHelper.getUserLogin());
        }
    }

    private View chooseHeaderView() {
        if (settingsHelper.hasCredentials()) {
            return LayoutInflater.from(context).inflate(R.layout.nav_header_user_info, null);
        } else {
            return LayoutInflater.from(context).inflate(R.layout.nav_header_log_in, null);
        }
    }
}
