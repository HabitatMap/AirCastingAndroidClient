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
import pl.llp.aircasting.screens.common.ToastHelper;
import pl.llp.aircasting.util.Constants;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
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

@Singleton
public class LocationHelper implements LocationListener {
    public static final int ACCURACY_THRESHOLD = 200;
    public static final int REQUEST_CHECK_SETTINGS = 2;

    @Inject EventBus eventBus;
    @Inject Context mContext;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastLocation;
    private LocationRequestListener mLocationRequestListener;
    private int starts;

    public interface LocationRequestListener {
        void onLocationRequestSuccess();
    }

    public void checkLocationSettings(Activity activity) {
        checkLocationSettingsSatisfied(activity);

    }

    public synchronized void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mContext);
        } else {
            ToastHelper.showText(mContext, "The app needs a permission to access your location data", Toast.LENGTH_SHORT);
        }
    }

    private boolean checkLocationSettingsSatisfied(final Activity activity) {
        LocationRequest locationRequest = createLocationRequest();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
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

//    private void getLastLocation() {
//        fusedLocationProviderClient.getLastLocation()
//                .addOnSuccessListener(new OnSuccessListener<Location>() {
//                    @Override
//                    public void onSuccess(Location location) {
//                        updateLocation(location);
//                    }
//                });
//    }

    public synchronized void stop() {
//        starts -= 1;
//        if (starts <= 0) {
//            starts = 0;
//            mLocationManager.removeUpdates(this);
//        }
    }

    public Location getLastLocation() {
        return mLastLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
        updateLocation(location);
    }

    private void updateLocation(Location location) {
        if (isBetterLocation(location, mLastLocation)) {
            mLastLocation = location;

            LocationEvent locationEvent = new LocationEvent();
            eventBus.post(locationEvent);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }

    /**
     * Determines whether one Location reading is better than the current Location fix
     * <p/>
     * Source: http://developer.android.com/guide/topics/location/obtaining-user-location.html
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        } else if (location == null) {
            // A null location is not so good
            return false;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > Constants.TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -Constants.TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > ACCURACY_THRESHOLD;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public boolean hasGPSFix() {
        return (mLastLocation != null) &&
                LocationManager.GPS_PROVIDER.equals(mLastLocation.getProvider());
    }

    public boolean hasNoGPSFix() {
        return !hasGPSFix();
    }


}
