package pl.llp.aircasting.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.google.inject.Inject;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.*;
import pl.llp.aircasting.activity.extsens.ExternalSensorActivity;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.SessionManager;

/**
 * Created by radek on 09/06/17.
 */
public class NavigationDrawerHelper {
    @Inject SessionManager sessionManager;
    @Inject SettingsHelper settingsHelper;
    @Inject Context context;

    public NavigationView navigationView;
    public DrawerLayout drawerLayout;
    public View navHeader;

    public void initNavigationDrawer(final Toolbar toolbar, final Activity activity) {
        navigationView = (NavigationView) activity.findViewById(R.id.navigation_view);
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
                        Intent intent = new Intent(context, DashboardActivity.class);
                        intent.putExtra("startingAircasting", true);
                        activity.startActivity(intent);
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.settings:
                        activity.startActivity(new Intent(context, SettingsActivity.class));
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.sessions:
                        activity.startActivity(new Intent(context, SessionsActivity.class));
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.about:
                        activity.startActivity(new Intent(context, AboutActivity.class));
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.phone_microphone:
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.external_sensors:
                        activity.startActivity(new Intent(context, ExternalSensorActivity.class));
                        drawerLayout.closeDrawers();
                        break;
                }
                return true;
            }
        });

        drawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(activity, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close){

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

    public void setDrawerHeader() {
        navHeader = chooseHeaderView();
        if (navigationView != null) {
            navigationView.addHeaderView(navHeader);
            View header = navigationView.getHeaderView(0);
            TextView email = (TextView) header.findViewById(R.id.profile_name);

            if (email != null) {
                email.setText(settingsHelper.getUserLogin());
            }
        }
    }

    public void removeHeader() {
        if (navigationView != null) {
            navigationView.removeHeaderView(navHeader);
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
