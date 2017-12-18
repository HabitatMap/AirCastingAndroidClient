package pl.llp.aircasting.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.view.View;
import com.google.inject.Inject;
import com.google.inject.Injector;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.task.SimpleProgressTask;
import pl.llp.aircasting.helper.NavigationDrawerHelper;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.sensor.airbeam.Airbeam2Configurator;
import roboguice.activity.event.*;
import roboguice.application.RoboApplication;
import roboguice.event.EventManager;
import roboguice.inject.ContextScope;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 1/16/12
 * Time: 12:02 PM
 */
public abstract class RoboActivityWithProgress extends AppCompatActivity implements ActivityWithProgress, AppCompatCallback {
    @Inject NavigationDrawerHelper navigationDrawerHelper;
    @Inject SettingsHelper settingsHelper;
    @Inject Airbeam2Configurator airbeam2Configurator;

    public AppCompatDelegate delegate;
    public Toolbar toolbar;
    protected ContextScope scope;
    protected EventManager eventManager;
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
        final Injector injector = getInjector();
        eventManager = injector.getInstance(EventManager.class);
        scope = injector.getInstance(ContextScope.class);
        scope.enter(this);
        injector.injectMembers(this);
        super.onCreate(savedInstanceState);
        eventManager.fire(new OnCreateEvent(savedInstanceState));

        Object instance = getLastNonConfigurationInstance();
        if (instance != null) {
            ((SimpleProgressTask) instance).setActivity(this);
        }
    }

    // we need to include code from RoboActivity here
    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        scope.injectViews();
        eventManager.fire(new OnContentViewAvailableEvent());
    }

    @Override
    protected void onRestart() {
        scope.enter(this);
        super.onRestart();
        eventManager.fire(new OnRestartEvent());
    }

    @Override
    protected void onStart() {
        scope.enter(this);
        eventManager.fire(new OnStartEvent());
        navigationDrawerHelper.setDrawerHeader();
        super.onStart();
    }

    @Override
    protected void onResume() {
        scope.enter(this);
        super.onResume();
        eventManager.fire(new OnResumeEvent());
    }

    @Override
    public void onPostResume() {
        super.onPostResume();

        navigationDrawerHelper.removeHeader();
        navigationDrawerHelper.setDrawerHeader();
    }

    @Override
    protected void onPause() {
        super.onPause();
        eventManager.fire(new OnPauseEvent());
    }

    @Override
    protected void onNewIntent( Intent intent ) {
        super.onNewIntent(intent);
        scope.enter(this);
        eventManager.fire(new OnNewIntentEvent());;
    }

    @Override
    protected void onStop() {
        scope.enter(this);
        try {
            eventManager.fire(new OnStopEvent());
        } finally {
            scope.exit(this);
            super.onStop();
            navigationDrawerHelper.removeHeader();
        }
    }

    @Override
    protected void onDestroy() {
        scope.enter(this);
        try {
            eventManager.fire(new OnDestroyEvent());
        } finally {
            eventManager.clear(this);
            scope.exit(this);
            scope.dispose(this);
            super.onDestroy();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        scope.enter(this);
        try {
            eventManager.fire(new OnActivityResultEvent(requestCode, resultCode, data));
        } finally {
            scope.exit(this);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        final Configuration currentConfig = getResources().getConfiguration();
        super.onConfigurationChanged(newConfig);
        eventManager.fire(new OnConfigurationChangedEvent(currentConfig, newConfig));
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        eventManager.fire(new OnContentChangedEvent());
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

    public void initToolbar(String title) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.navigation_empty_icon);
        toolbar.setContentInsetStartWithNavigation(0);
        this.setSupportActionBar(toolbar);
        this.setTitle(title);
    }

    public void initNavigationDrawer() {
        navigationDrawerHelper.initNavigationDrawer(toolbar, this);
    }

    public AppCompatDelegate getDelegate() {
        if (delegate == null) {
            delegate = AppCompatDelegate.create(this, this);
        }
        return delegate;
    }

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

    public Injector getInjector() {
        return ((RoboApplication) getApplication()).getInjector();
    }
}
