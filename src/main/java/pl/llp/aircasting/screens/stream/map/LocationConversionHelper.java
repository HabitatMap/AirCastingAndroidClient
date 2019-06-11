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

import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Session;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.maps.GeoPoint;
import com.google.common.base.Function;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
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

    public static GeoPoint geoPoint(Measurement measurement) {
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

    public static BoundingBox boundingBox(Session session) {
        double north, south, east, west;
        north = east = Integer.MIN_VALUE;
        south = west = Integer.MAX_VALUE;

        for (Measurement measurement : allMeasurements(session)) {
            north = max(north, measurement.getLatitude());
            south = min(south, measurement.getLatitude());
            east = max(east, measurement.getLongitude());
            west = min(west, measurement.getLongitude());
        }

        LatLng center = new LatLng((north + south) / 2, (east + west) / 2);
        double latSpan = north - south;
        double lonSpan = east - west;

        return new BoundingBox(center, latSpan, lonSpan);
    }

    private static Iterable<Measurement> allMeasurements(Session session) {
        Iterable<Iterable<Measurement>> measurements =
                transform(session.getMeasurementStreams(), new Function<MeasurementStream, Iterable<Measurement>>() {
                    @Override
                    public Iterable<Measurement> apply(MeasurementStream input) {
                        return input.getMeasurements();
                    }
                });

        return concat(measurements);
    }

    public static class BoundingBox {
        private LatLng center;
        private double latSpan;
        private double lonSpan;

        public BoundingBox(LatLng center, double latSpan, double lonSpan) {
            this.center = center;
            this.latSpan = latSpan;
            this.lonSpan = lonSpan;
        }

        public LatLng getCenter() {
            return center;
        }

        public double getLatSpan() {
            return latSpan;
        }

        public double getLonSpan() {
            return lonSpan;
        }
    }
}