package pl.llp.aircasting.screens.stream.map;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.inject.Singleton;

import pl.llp.aircasting.R;
import pl.llp.aircasting.common.BaseViewMvc;
import pl.llp.aircasting.model.Session;

import static pl.llp.aircasting.screens.stream.map.LocationConversionHelper.boundingBox;

@Singleton
public class StreamMapViewMvcImpl implements BaseViewMvc, OnMapReadyCallback, View.OnClickListener, GoogleMap.OnCameraIdleListener {
    private final Activity mContext;
    private final LayoutInflater mLayoutInflater;
    private final View mLocate;
    private final View mZoomIn;
    private final View mZoomOut;
    private final View mProgress;
    private final Session mSession;
    private View mRootView;
    private GoogleMap mMap;
    private LatLng mCurrentLocation;

    public StreamMapViewMvcImpl(Activity context, ViewGroup parent, Session session) {
        mContext = context;
        mLayoutInflater = mContext.getLayoutInflater();
        mRootView = mLayoutInflater.inflate(R.layout.activity_map, parent, false);

        MapFragment mapFragment = (MapFragment) mContext.getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mSession = session;

        mLocate = findViewById(R.id.locate);
        mZoomIn = findViewById(R.id.zoom_in);
        mZoomOut = findViewById(R.id.zoom_out);
        mProgress = findViewById(R.id.progress_bar);

        mProgress.setVisibility(View.GONE);

        mLocate.setOnClickListener(this);
        mZoomIn.setOnClickListener(this);
        mZoomOut.setOnClickListener(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMaxZoomPreference(20.0f);
        mMap.setMinZoomPreference(1.0f);
        mMap.setOnCameraIdleListener(this);

        moveToSessionLocation();
    }

    public void locateMe() {
    }

    @Override
    public View getRootView() {
        return mRootView;
    }

    private <T extends View>T findViewById(int id) {
        return mRootView.findViewById(id);
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
        }
    }

    @Override
    public void onCameraIdle() {
        mCurrentLocation = mMap.getCameraPosition().target;
    }

    public void moveToSessionLocation() {
        if (mCurrentLocation == null) {
            LocationConversionHelper.BoundingBox boundingBox = boundingBox(mSession);
            mCurrentLocation = boundingBox.getCenter();
        }
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mCurrentLocation, 16f);
        mMap.animateCamera(cameraUpdate);
    }
}
