package pl.llp.aircasting.screens.stream.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.screens.stream.base.AirCastingActivity;

public class StreamMapActivity extends AirCastingActivity {
    private StreamMapViewMvcImpl mStreamMapViewMvcImpl;
    private int mRequestedAction;

    private static final int ACTION_TOGGLE = 1;
    private static final int ACTION_CENTER = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStreamMapViewMvcImpl = new StreamMapViewMvcImpl(this, null);
        setContentView(mStreamMapViewMvcImpl.getRootView());
        initToolbar("Map");
        initNavigationDrawer();
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        getDelegate().invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuInflater inflater = getDelegate().getMenuInflater();
        inflater.inflate(R.menu.toolbar_crowd_map_toggle, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        super.onOptionsItemSelected(menuItem);

        switch (menuItem.getItemId()) {
            case R.id.toggle_aircasting:
                mRequestedAction = ACTION_TOGGLE;

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationHelper.checkLocationSettings(this);
                }

                break;
            case R.id.make_note:
                Intents.makeANote(this);
                break;
            case R.id.toggle_heat_map_button:
//                toggleHeatMapVisibility(menuItem);
                break;
        }
        return true;
    }

    @Override
    protected void refreshNotes() {

    }

    @Override
    public void onLocationSettingsSatisfied() {
        if (mRequestedAction == ACTION_TOGGLE) {
            toggleAirCasting();
        } else if (mRequestedAction == ACTION_CENTER) {
            mStreamMapViewMvcImpl.locateMe();
        }
    }
}
