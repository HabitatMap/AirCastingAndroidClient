package pl.llp.aircasting.screens.stream.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.screens.stream.base.AirCastingActivity;

public class StreamMapActivity extends AirCastingActivity implements OnMapReadyCallback {
    private StreamMapViewMvcImpl mStreamMapViewMvcImpl;
    private int mRequestedAction;

    private static final int ACTION_TOGGLE = 1;
    private static final int ACTION_CENTER = 2;
    private boolean mZoomToSession = true;
    private MapFragment mMapFragment;
    private GoogleMap mMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.w("map activity", "oncreate");

        mStreamMapViewMvcImpl = new StreamMapViewMvcImpl(this, null, visibleSession);
        setContentView(mStreamMapViewMvcImpl.getRootView());

        if (mMapFragment == null) {
            mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
            mMapFragment.setRetainInstance(true);
            mMapFragment.getMapAsync((OnMapReadyCallback) context);
        }

        initToolbar("Map");
        initNavigationDrawer();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (mMap == null) {
            mMap = googleMap;
            mStreamMapViewMvcImpl.onMapReady(googleMap);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (visibleSession.isVisibleSessionViewed() && mZoomToSession) {
//            mStreamMapViewMvcImpl.showSessionLocation();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mZoomToSession = false;
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
