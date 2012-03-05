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
package pl.llp.aircasting.view.overlay;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.inject.Inject;
import pl.llp.aircasting.helper.ResourceHelper;

import static pl.llp.aircasting.helper.LocationConversionHelper.geoPoint;
import static pl.llp.aircasting.util.DrawableTransformer.centerAt;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/31/11
 * Time: 4:40 PM
 */
public class LocationOverlay extends Overlay {
    @Inject ResourceHelper resourceHelper;

    private Location location;
    private double value;

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        if (shadow) return;

        if (location != null) {
            Drawable bullet = resourceHelper.getLocationBullet(value);
            GeoPoint geoPoint = geoPoint(location);
            Point point = mapView.getProjection().toPixels(geoPoint, null);

            centerAt(bullet, point);
            bullet.draw(canvas);
        }
    }

    public void setLocation(Location newLocation) {
        this.location = newLocation;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
