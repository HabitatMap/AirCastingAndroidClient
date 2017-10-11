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

import pl.llp.aircasting.helper.ResourceHelper;
import pl.llp.aircasting.helper.SoundHelper;
import pl.llp.aircasting.model.CurrentSessionSensorManager;
import pl.llp.aircasting.model.internal.Region;
import pl.llp.aircasting.model.Sensor;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.google.inject.Inject;

import static pl.llp.aircasting.helper.LocationConversionHelper.geoPoint;

public class HeatMapOverlay extends Overlay
{
  private static final int ALPHA = 100;

  @Inject SoundHelper soundHelper;
  @Inject ResourceHelper resourceHelper;
  @Inject Paint paint;
  @Inject
  CurrentSessionSensorManager sensors;

  private Iterable<Region> regions;

  @Override
  public void draw(Canvas canvas, MapView view, boolean shadow)
  {
    if (shadow || regions == null) return;

    Projection projection = view.getProjection();

    Sensor visibleSensor = sensors.getVisibleSensor();
    for (Region region : regions)
    {
      double value = region.getValue();

      if (soundHelper.shouldDisplay(visibleSensor, value))
      {
        int color = resourceHelper.getColorAbsolute(visibleSensor, value);

        paint.setColor(color);
        paint.setAlpha(ALPHA);

        GeoPoint southWest = geoPoint(region.getSouth(), region.getWest());
        GeoPoint northEast = geoPoint(region.getNorth(), region.getEast());
        Point bottomLeft = projection.toPixels(southWest, null);
        Point topRight = projection.toPixels(northEast, null);

        canvas.drawRect(bottomLeft.x, bottomLeft.y, topRight.x, topRight.y, paint);
      }
    }
  }

  public void setRegions(Iterable<Region> regions)
  {
    this.regions = regions;
  }
}
