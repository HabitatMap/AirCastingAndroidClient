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
package pl.llp.aircasting.util.map;

import android.graphics.Point;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;

import static pl.llp.aircasting.util.Projection.project;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/24/11
 * Time: 11:33 AM
 */
public class ProjectionClone implements Projection {
    private GeoPoint topLeft;
    private GeoPoint bottomRight;
    private int height;
    private int width;


    public ProjectionClone(Projection projection, int width, int height) {
        topLeft = projection.fromPixels(0, 0);
        bottomRight = projection.fromPixels(width, height);

        this.width = width;
        this.height = height;
    }

    @Override
    public Point toPixels(GeoPoint geoPoint, Point point) {
        double x = project(geoPoint.getLongitudeE6(), topLeft.getLongitudeE6(), bottomRight.getLongitudeE6(), 0, width);
        double y = project(geoPoint.getLatitudeE6(), topLeft.getLatitudeE6(), bottomRight.getLatitudeE6(), 0, height);

        return new Point((int) x, (int) y);
    }

    @Override
    public GeoPoint fromPixels(int x, int y) {
        return null;
    }

    @Override
    public float metersToEquatorPixels(float x) {
        return 0;
    }
}
