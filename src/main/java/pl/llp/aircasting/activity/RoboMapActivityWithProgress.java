package pl.llp.aircasting.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.View;
import com.google.inject.Inject;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.task.SimpleProgressTask;
import pl.llp.aircasting.helper.NavigationDrawerHelper;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.SessionManager;
import roboguice.activity.RoboMapActivity;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 1/16/12
 * Time: 12:35 PM
 */
public abstract class RoboMapActivityWithProgress extends RoboMapActivity implements ActivityWithProgress, AppCompatCallback, View.OnClickListener {
    @Inject SessionManager sessionManager;
    @Inject SettingsHelper settingsHelper;
    @Inject NavigationDrawerHelper navigationDrawerHelper;
    @Inject Context context;

    private int progressStyle;
    private ProgressDialog dialog;
    private SimpleProgressTask task;
    public AppCompatDelegate delegate;
    public Toolbar toolbar;

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

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
       super.onPostCreate(savedInstanceState);
       getDelegate().onPostCreate(savedInstanceState);
    }

    public void initToolbar(String title) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.navigation_empty_icon);
        getDelegate().setSupportActionBar(toolbar);
        getDelegate().setTitle(title);
    }

    public void initNavigationDrawer() {
        navigationDrawerHelper.initNavigationDrawer(toolbar, this);
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

    @Override
    public void onStart() {
        super.onStart();
        getDelegate().onStart();

        navigationDrawerHelper.setDrawerHeader();
    }

    @Override
    public void onStop() {
        super.onStop();
        getDelegate().onStop();

        navigationDrawerHelper.removeHeader();
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();

        navigationDrawerHelper.removeHeader();
        navigationDrawerHelper.setDrawerHeader();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
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

    @Override
    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
        return null;
    }

    public void onProfileClick(View view) {
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
}
