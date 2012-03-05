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

import android.location.Location;
import com.google.android.maps.GeoPoint;
import pl.llp.aircasting.model.SoundMeasurement;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class LocationConversionHelper {

    public static final int INTEGER_1E6 = 1000000;
    public static final double DOUBLE_1E6 = 1000000.0;

    public static GeoPoint geoPoint(double latitude, double longitude) {
        return new GeoPoint(geoPointize(latitude), geoPointize(longitude));
    }

    private static int geoPointize(double latLng) {
        return (int) (latLng * INTEGER_1E6);
    }

    public static GeoPoint geoPoint(SoundMeasurement measurement) {
        return geoPoint(measurement.getLatitude(), measurement.getLongitude());
    }

    public static GeoPoint geoPoint(Location location) {
        return geoPoint(location.getLatitude(), location.getLongitude());
    }

    public static Location location(GeoPoint geoPoint) {
        Location location = new Location("Manufactured");
        location.setLatitude(geoPoint.getLatitudeE6() / DOUBLE_1E6);
        location.setLongitude(geoPoint.getLongitudeE6() / DOUBLE_1E6);
        return location;
    }

    public static BoundingBox boundingBox(Iterable<SoundMeasurement> measurements) {
        int north, south, east, west;
        north = east = Integer.MIN_VALUE;
        south = west = Integer.MAX_VALUE;

        for (SoundMeasurement measurement : measurements) {
            north = max(north, geoPointize(measurement.getLatitude()));
            south = min(south, geoPointize(measurement.getLatitude()));
            east = max(east, geoPointize(measurement.getLongitude()));
            west = min(west, geoPointize(measurement.getLongitude()));
        }

        GeoPoint center = new GeoPoint((north + south) / 2, (east + west) / 2);
        int latSpan = north - south;
        int lonSpan = east - west;

        return new BoundingBox(center, latSpan, lonSpan);
    }

    public static class BoundingBox {
        private GeoPoint center;
        private int latSpan;
        private int lonSpan;

        public BoundingBox(GeoPoint center, int latSpan, int lonSpan) {
            this.center = center;
            this.latSpan = latSpan;
            this.lonSpan = lonSpan;
        }

        public GeoPoint getCenter() {
            return center;
        }

        public int getLatSpan() {
            return latSpan;
        }

        public int getLonSpan() {
            return lonSpan;
        }
    }
}