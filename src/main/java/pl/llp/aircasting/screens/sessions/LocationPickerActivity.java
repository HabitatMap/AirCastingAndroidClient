package pl.llp.aircasting.screens.sessions;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.inject.Inject;

import pl.llp.aircasting.R;
import pl.llp.aircasting.screens.common.base.RoboActivityWithProgress;
import pl.llp.aircasting.screens.common.helpers.LocationHelper;

public class LocationPickerActivity extends RoboActivityWithProgress
        implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener, View.OnClickListener {
    @Inject LocationHelper locationHelper;

    private GoogleMap mMap;
    private Button mLocateButton;
    private LatLng mCurrentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);
        mLocateButton = findViewById(R.id.locate);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mCurrentLocation = mMap.getCameraPosition().target;
        mMap.setOnCameraIdleListener(this);
        mLocateButton.setOnClickListener(this);
    }

    @Override
    public void onCameraIdle() {
        mCurrentLocation = mMap.getCameraPosition().target;
        Log.w("current location", String.valueOf(mCurrentLocation));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.locate:
                // should move to locationHelper.lastLocation()
                mMap.moveCamera(CameraUpdateFactory.newLatLng(mCurrentLocation));
                break;
        }
    }

    public LatLng getLocation() {
        return mCurrentLocation;
    }
}
