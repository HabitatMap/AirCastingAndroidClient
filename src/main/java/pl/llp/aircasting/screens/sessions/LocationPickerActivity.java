package pl.llp.aircasting.screens.sessions;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.inject.Inject;

import java.util.Arrays;

import pl.llp.aircasting.R;
import pl.llp.aircasting.screens.common.base.RoboActivityWithProgress;
import pl.llp.aircasting.screens.common.helpers.LocationHelper;

public class LocationPickerActivity extends RoboActivityWithProgress
        implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener, View.OnClickListener {
    public static final String LOCATION = "location";

    @Inject LocationHelper mLocationHelper;

    private GoogleMap mMap;
    private LatLng mCurrentLocation;
    private Button mSelectLocation;
    private View mLocate;
    private View mZoomIn;
    private View mZoomOut;
    private View mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mLocate = findViewById(R.id.locate);
        mZoomIn = findViewById(R.id.zoom_in);
        mZoomOut = findViewById(R.id.zoom_out);
        mSelectLocation = findViewById(R.id.select_location);
        mProgress = findViewById(R.id.progress_bar);

        mProgress.setVisibility(View.GONE);

        initializePlacesWidget();
        mLocate.setOnClickListener(this);
        mZoomIn.setOnClickListener(this);
        mZoomOut.setOnClickListener(this);
        mSelectLocation.setOnClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mCurrentLocation = mMap.getCameraPosition().target;

        mMap.setMaxZoomPreference(20.0f);
        mMap.setMinZoomPreference(5.0f);
        mMap.setOnCameraIdleListener(this);

        locateMe();
    }

    private void moveCamera(LatLng latLng, float zoom) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.animateCamera(cameraUpdate);
    }

    @Override
    public void onCameraIdle() {
        mCurrentLocation = mMap.getCameraPosition().target;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.locate:
                locateMe();
                break;
            case R.id.zoom_in:
                mMap.animateCamera(CameraUpdateFactory.zoomIn());
                break;
            case R.id.zoom_out:
                mMap.animateCamera(CameraUpdateFactory.zoomOut());
                break;
            case R.id.select_location:
                mProgress.setVisibility(View.VISIBLE);
                Intent intent = new Intent();
                intent.putExtra(LOCATION, mCurrentLocation);
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }

    public LatLng getLocation() {
        return mCurrentLocation;
    }

    private void initializePlacesWidget() {
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.com_google_android_geo_API_KEY));
        }

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mCurrentLocation = place.getLatLng();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(mCurrentLocation);
                mMap.animateCamera(cameraUpdate);
            }

            @Override
            public void onError(Status status) {
                Log.w("places", "something went wrong");
            }
        });
    }

    private void locateMe() {
        Location lastLocation = mLocationHelper.getLastLocation();
        if (lastLocation != null) {
            moveCamera(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 18.0f);
        }
    }
}
