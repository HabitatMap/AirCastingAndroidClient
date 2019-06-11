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
package pl.llp.aircasting.screens.stream.map;

import pl.llp.aircasting.event.session.SessionLoadedForViewingEvent;
import pl.llp.aircasting.event.session.SessionStartedEvent;
import pl.llp.aircasting.event.session.SessionStoppedEvent;
import pl.llp.aircasting.screens.common.helpers.ResourceHelper;
import pl.llp.aircasting.screens.common.sessionState.VisibleSession;
import pl.llp.aircasting.util.map.PathSmoother;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import java.util.List;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Iterables.skip;
import static com.google.common.collect.Lists.newArrayList;

public class RouteOverlay extends Overlay {
    public static final int OPAQUE = 255;
    public static final int SMOOTHING_BATCH = 10;

    @Inject PathSmoother pathSmoother;
    @Inject ResourceHelper resourceHelper;
    @Inject VisibleSession visibleSession;

    private List<GeoPoint> points = newArrayList();
    private List<GeoPoint> pendingPoints = newArrayList();
    private List<GeoPoint> smoothedPoints = newArrayList();

    private int zoomLevel;
    private GeoPoint mapCenter;
    private Paint paint;
    private Path path;

    private boolean skipDrawing;


    @Inject
    public void init() {
        preparePaint();
    }

    public void addPoint(GeoPoint geoPoint) {
        pendingPoints.add(geoPoint);
        invalidate();
    }

    public void invalidate() {
        path = null;
    }

    @Override
    public void draw(Canvas canvas, MapView view, boolean shadow) {
        if (shadow) return;

        if (skipDrawing()) {
            return;
        }

        if (isRefreshRequired(view)) {
            path = new Path();

            preparePoints();
            preparePath(view.getProjection());

            zoomLevel = view.getZoomLevel();
            mapCenter = view.getMapCenter();
        }

        canvas.drawPath(path, paint);
    }

    @Subscribe
    public void onEvent(SessionStartedEvent event) {
        skipDrawing = false;
    }

    @Subscribe
    public void onEvent(SessionStoppedEvent event) {
        skipDrawing = true;
    }

    @Subscribe
    public void onEvent(SessionLoadedForViewingEvent event) {
        this.skipDrawing = false;
    }

    private boolean skipDrawing() {
        return this.skipDrawing || visibleSession.isSessionLocationless();
    }

    private boolean isRefreshRequired(MapView view) {
        return path == null ||
                zoomLevel != view.getZoomLevel() ||
                !mapCenter.equals(view.getMapCenter());
    }

    private void preparePoints() {
        if (pendingPoints.size() > SMOOTHING_BATCH) {
            points.addAll(pendingPoints);
            pendingPoints.clear();

//            smoothedPoints = pathSmoother.getSmoothed(points);
        }
    }

    private void preparePaint() {
        paint = new Paint();

        paint.setColor(resourceHelper.getGpsRoute());
        paint.setAlpha(OPAQUE);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);
    }

    private void preparePath(Projection projection) {
        Iterable<GeoPoint> pointsToDraw = concat(smoothedPoints, pendingPoints);
        if (isEmpty(pointsToDraw)) return;

        Point point = new Point();

        projection.toPixels(get(pointsToDraw, 0), point);
        path.moveTo(point.x, point.y);

        for (GeoPoint geoPoint : skip(pointsToDraw, 1)) {
            projection.toPixels(geoPoint, point);
            path.lineTo(point.x, point.y);
        }
    }

    public void clear() {
        points.clear();
        pendingPoints.clear();
        smoothedPoints.clear();
    }
}
