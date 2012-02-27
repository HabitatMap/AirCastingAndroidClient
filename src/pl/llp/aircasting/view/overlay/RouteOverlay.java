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

import android.graphics.*;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.google.inject.Inject;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 2/24/12
 * Time: 12:46 PM
 */
public class RouteOverlay extends Overlay {
    private List<GeoPoint> points = newArrayList();
    private int zoomLevel;
    private GeoPoint mapCenter;
    private Paint paint;
    private Path path;

    @Inject
    public void init() {
        preparePaint();
    }

    public void addPoint(GeoPoint geoPoint, MapView view) {
        points.add(geoPoint);

        if (path != null) {
            Point point = view.getProjection().toPixels(geoPoint, null);
            path.lineTo(point.x, point.y);
        }
    }

    @Override
    public void draw(Canvas canvas, MapView view, boolean shadow) {
        if (shadow) return;

        if (path == null || view.getZoomLevel() != zoomLevel || !mapCenter.equals(view.getMapCenter())) {
            path = new Path();
            preparePath(view.getProjection());

            zoomLevel = view.getZoomLevel();
            mapCenter = view.getMapCenter();
        }

        canvas.drawPath(path, paint);
    }

    private void preparePaint() {
        paint = new Paint();

        paint.setColor(Color.BLUE);
        paint.setAlpha(255);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);
    }

    private void preparePath(Projection projection) {
        if (points.isEmpty()) return;

        Point point = new Point();

        projection.toPixels(points.get(0), point);
        path.moveTo(point.x, point.y);

        for (GeoPoint geoPoint : points) {
            projection.toPixels(geoPoint, point);
            path.lineTo(point.x, point.y);
        }
    }
}
