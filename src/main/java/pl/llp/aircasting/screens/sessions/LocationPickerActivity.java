package pl.llp.aircasting.screens.sessions;

import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.inject.Inject;

import pl.llp.aircasting.R;
import pl.llp.aircasting.screens.common.base.RoboActivityWithProgress;
import pl.llp.aircasting.screens.common.helpers.LocationHelper;

public class LocationPickerActivity extends RoboActivityWithProgress implements OnMapReadyCallback {
    @Inject LocationHelper locationHelper;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        Location currentLocation = locationHelper.getLastLocation();
//        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
//
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
    }
}
