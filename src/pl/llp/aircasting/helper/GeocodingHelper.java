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
package pl.llp.aircasting.helper;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.List;

import static com.google.common.collect.Iterables.getOnlyElement;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/14/11
 * Time: 11:39 AM
 */
@Singleton
public class GeocodingHelper {
    private static final String TAG = GeocodingHelper.class.getSimpleName();
    public static final int REQUESTED_RESULTS = 1;

    @Inject Geocoder geocoder;
    @Inject LocationHelper locationHelper;

    public String getFromLocation(Location location) {
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), REQUESTED_RESULTS);
            return getOnlyElement(addresses).getThoroughfare();
        } catch (Exception e) {
            Log.e(TAG, "Geocoding failure", e);
            return null;
        }
    }
}
