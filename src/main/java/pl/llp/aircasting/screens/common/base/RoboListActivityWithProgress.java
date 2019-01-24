package pl.llp.aircasting.screens.common.base;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import com.google.inject.Inject;
import pl.llp.aircasting.R;
import pl.llp.aircasting.screens.common.helpers.NavigationDrawerHelper;
import pl.llp.aircasting.screens.common.helpers.SettingsHelper;
import pl.llp.aircasting.screens.userAccount.ProfileActivity;
import pl.llp.aircasting.screens.userAccount.SignOutActivity;
import roboguice.activity.RoboListActivity;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 1/17/12
 * Time: 3:19 PM
 */
public class RoboListActivityWithProgress extends RoboListActivity implements ActivityWithProgress, AppCompatCallback {
    @Inject
    SettingsHelper settingsHelper;
    @Inject
    NavigationDrawerHelper navigationDrawerHelper;
    @Inject Context context;

    public AppCompatDelegate delegate;
    public Toolbar toolbar;
    private int progressStyle;
    private ProgressDialog dialog;
    private SimpleProgressTask task;

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
        toolbar.setContentInsetStartWithNavigation(0);
        getDelegate().setSupportActionBar(toolbar);
        getDelegate().setTitle(title);
    }

    public void initNavigationDrawer() {
        navigationDrawerHelper.initNavigationDrawer(toolbar, this);
   }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
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
