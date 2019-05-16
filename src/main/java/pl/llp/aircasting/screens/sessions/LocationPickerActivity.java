package pl.llp.aircasting.screens.sessions;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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
                mMap.moveCamera(CameraUpdateFactory.newLatLng(mCurrentLocation));
                break;
        }
    }
}
