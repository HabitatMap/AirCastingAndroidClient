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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.inject.Singleton;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import pl.llp.aircasting.R;
import pl.llp.aircasting.common.BaseViewMvc;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.screens.common.sessionState.VisibleSession;
import pl.llp.aircasting.util.map.PathSmoother;

import static pl.llp.aircasting.screens.stream.map.LocationConversionHelper.boundingBox;

@Singleton
public class StreamMapViewMvcImpl implements BaseViewMvc, View.OnClickListener, GoogleMap.OnCameraIdleListener {
    private final Activity mContext;
    private final LayoutInflater mLayoutInflater;
    private final View mLocate;
    private final View mZoomIn;
    private final View mZoomOut;
    private final View mProgress;
    private final VisibleSession mVisibleSession;
    private final PathSmoother mPathSmoother;
    private View mRootView;
    private GoogleMap mMap;
    private LatLng mCurrentLocation;
    private List<LatLng> mRoutePoints = new ArrayList<>();

    public StreamMapViewMvcImpl(Activity context, ViewGroup parent, VisibleSession visibleSession) {
        mContext = context;
        mLayoutInflater = mContext.getLayoutInflater();
        mRootView = mLayoutInflater.inflate(R.layout.activity_map, parent, false);

        mVisibleSession = visibleSession;

        mLocate = findViewById(R.id.locate);
        mZoomIn = findViewById(R.id.zoom_in);
        mZoomOut = findViewById(R.id.zoom_out);
        mProgress = findViewById(R.id.progress_bar);

        mPathSmoother = new PathSmoother();

        mProgress.setVisibility(View.GONE);

        mLocate.setOnClickListener(this);
        mZoomIn.setOnClickListener(this);
        mZoomOut.setOnClickListener(this);
    }

    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMaxZoomPreference(20.0f);
        mMap.setMinZoomPreference(1.0f);
        mMap.setOnCameraIdleListener(this);

        drawSessionRoute();
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
            LocationConversionHelper.BoundingBox boundingBox = boundingBox(mVisibleSession.getSession());
            mCurrentLocation = boundingBox.getCenter();
        }
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mCurrentLocation, 16f);
        mMap.animateCamera(cameraUpdate);
    }

    private void drawSessionRoute() {
        List<Measurement> measurements = mVisibleSession.getStream().getMeasurements();

        for (Measurement measurement : measurements) {
            LatLng point = new LatLng(measurement.getLatitude(), measurement.getLongitude());
            mRoutePoints.add(point);
        }

        List<LatLng> smoothedPath = mPathSmoother.getSmoothed(mRoutePoints);
        PolylineOptions options = new PolylineOptions();
        options.width(5f);
        options.color(R.color.gps_route);

        for (LatLng point : smoothedPath) {
            options.add(point);
        }

        mMap.addPolyline(options);
    }
}
