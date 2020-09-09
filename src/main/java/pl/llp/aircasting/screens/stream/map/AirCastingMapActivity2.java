/**
    AirCasting - Share your Air!
    Copyright (C) 2011-2012 HabitatMap, Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    You can contact the authors by email at <info@habitatmap.org>
*/
package pl.llp.aircasting.screens.stream.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.*;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.libraries.maps.CameraUpdateFactory;
import com.google.android.libraries.maps.GoogleMap;
import com.google.android.libraries.maps.OnMapReadyCallback;
import com.google.android.libraries.maps.SupportMapFragment;
import com.google.android.libraries.maps.model.LatLng;
import com.google.android.libraries.maps.model.MarkerOptions;
import com.google.android.maps.MapController;
import com.google.android.maps.OverlayItem;
import com.google.common.eventbus.Subscribe;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.screens.common.base.RoboActivityWithProgress;
import pl.llp.aircasting.screens.common.helpers.LocationHelper;
import pl.llp.aircasting.screens.stream.base.AirCastingActivity;
import pl.llp.aircasting.event.session.VisibleSessionUpdatedEvent;
import pl.llp.aircasting.networking.drivers.AveragesDriver;
import pl.llp.aircasting.event.sensor.LocationEvent;
import pl.llp.aircasting.event.session.NoteCreatedEvent;
import pl.llp.aircasting.event.ui.DoubleTapEvent;
import pl.llp.aircasting.event.ui.VisibleStreamUpdatedEvent;
import pl.llp.aircasting.screens.common.ToastHelper;
import pl.llp.aircasting.screens.common.sessionState.VisibleSession;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.internal.Region;
import pl.llp.aircasting.networking.httpUtils.HttpResult;
import pl.llp.aircasting.screens.stream.MeasurementPresenter;

import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;
import com.google.inject.Inject;

import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

import java.util.List;

import static java.lang.Math.min;
import static pl.llp.aircasting.screens.common.helpers.LocationHelper.REQUEST_CHECK_SETTINGS;
import static pl.llp.aircasting.screens.stream.map.LocationConversionHelper.boundingBox;
import static pl.llp.aircasting.screens.stream.map.LocationConversionHelper.geoPoint;
import static pl.llp.aircasting.screens.stream.map.MapIdleDetector.detectMapIdle;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/17/11
 * Time: 5:04 PM
 */
public class AirCastingMapActivity2 extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.heat_map2);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions()
                .position(sydney)
                .title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
