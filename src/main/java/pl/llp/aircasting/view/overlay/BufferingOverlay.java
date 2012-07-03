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

import pl.llp.aircasting.R;
import pl.llp.aircasting.util.Constants;
import pl.llp.aircasting.util.bitmap.BitmapHolder;
import pl.llp.aircasting.util.map.ProjectionClone;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.google.inject.Inject;
import roboguice.inject.InjectResource;

public abstract class BufferingOverlay<UpdateData> extends Overlay
{
  @Inject BitmapHolder bitmapHolder;
  @InjectResource(R.color.transparent) int transparent;

  private Bitmap bitmap;
  private int bitmapIndex = 0;

  private GeoPoint mapCenter;
  private int zoomLevel;
  private Projection projection;

  private volatile boolean paused;

  @Override
  public synchronized void draw(Canvas canvas, MapView mapView, boolean shadow)
  {
    if (shadow || bitmap == null || mapView.getZoomLevel() != zoomLevel) return;

    Projection projection = mapView.getProjection();
    Point oldCenter = projection.toPixels(mapCenter, null);
    Point newCenter = projection.toPixels(mapView.getMapCenter(), null);

    int x = oldCenter.x - newCenter.x;
    int y = oldCenter.y - newCenter.y;
    if(bitmap.isRecycled())
    {
      String msg = "Trying to draw [" + mapView.getWidth() + ", " + mapView.getHeight() + "] with a recycled bitmap ";
      Log.e(Constants.TAG, msg);
      return;
    }

    canvas.drawBitmap(bitmap, x, y, null);
  }

  public void refresh(MapView mapView)
  {
    if (drawingIsPaused())
    {
      return;
    }

    bitmapIndex = 1 - bitmapIndex;

    Canvas canvas = new Canvas();

    int width = mapView.getWidth();
    int height = mapView.getHeight();
    Bitmap newBitmap = bitmapHolder.getBitmap(width, height, bitmapIndex);

    if (newBitmap != null)
    {
      newBitmap.eraseColor(transparent);

      canvas.setBitmap(newBitmap);

      int newZoomLevel = mapView.getZoomLevel();
      GeoPoint newMapCenter = mapView.getMapCenter();
      Projection newProjection = mapView.getProjection();
      ProjectionClone projectionClone = new ProjectionClone(newProjection, width, height);

      performDraw(canvas, projectionClone);

      synchronized (this)
      {
        this.projection = projectionClone;
        zoomLevel = newZoomLevel;
        mapCenter = newMapCenter;

        bitmap = newBitmap;
      }
    }
  }

  private boolean drawingIsPaused()
  {
    return paused;
  }

  public void update(UpdateData updateData)
  {
    Canvas canvas = null;
    Projection projectionToUse = null;

    synchronized (this)
    {
      if (bitmap != null)
      {
        canvas = new Canvas(bitmap);
        projectionToUse = this.projection;
      }
    }

    if (canvas != null)
    {
      performUpdate(canvas, projectionToUse, updateData);
    }
  }

  protected abstract void performDraw(Canvas canvas, Projection projection);

  protected abstract void performUpdate(Canvas canvas, Projection projection, UpdateData updateData);

  protected void stopDrawing(MapView view)
  {
    paused = true;
    bitmap = null;
    bitmapHolder.release(view.getWidth(), view.getHeight());
  }

  public void startDrawing()
  {
    paused = false;
  }
}
