/**
 * AirCasting - Share your Air!
 * Copyright (C) 2011-2012 HabitatMap, Inc.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * You can contact the authors by email at <info@habitatmap.org>
 */
package pl.llp.aircasting.screens.common.helpers;

import pl.llp.aircasting.event.sensor.LocationEvent;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import static pl.llp.aircasting.util.Constants.LOCATION_PERMISSION;
import static pl.llp.aircasting.util.Constants.PERMISSIONS_REQUEST_FINE_LOCATION;

@Singleton
public class LocationHelper {
    public static final int REQUEST_CHECK_SETTINGS = 2;

    @Inject EventBus eventBus;
    @Inject Context mContext;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastLocation;
    private LocationRequestListener mLocationRequestListener;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Boolean mLocationUpdatesStarted = false;

    public interface LocationRequestListener {
        void onLocationRequestSuccess();
    }

    public void checkLocationSettings(Activity activity) {
        checkLocationSettingsSatisfied(activity);
    }

    public void initLocation(Activity activity) {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, LOCATION_PERMISSION, PERMISSIONS_REQUEST_FINE_LOCATION);
            return;
        }

        mFusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            mLastLocation = location;
                        }
                    }
                });
    }

    // suppress warning, because we should already have a permission coming here
    @SuppressLint("MissingPermission")
    public synchronized void startLocationUpdates() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateLocation(location);
                }
            }
        };

        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        mLocationUpdatesStarted = true;
    }

    public void stopLocationUpdates() {
        if (mFusedLocationProviderClient != null && mLocationUpdatesStarted) {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
            mLocationUpdatesStarted = false;
        }
    }

    private boolean checkLocationSettingsSatisfied(final Activity activity) {
        mLocationRequest = createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(mContext);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startLocationUpdates();
                mLocationRequestListener.onLocationRequestSuccess();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(activity,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                    }
                }
            }
        });

        return true;
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return locationRequest;
    }

    public void registerListener(LocationRequestListener listener) {
        mLocationRequestListener = listener;
    }

    public Location getLastLocation() {
        return mLastLocation;
    }

    private void updateLocation(Location location) {
        mLastLocation = location;

        LocationEvent locationEvent = new LocationEvent();
        eventBus.post(locationEvent);
    }
}
