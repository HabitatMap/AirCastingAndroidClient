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
import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;
import com.google.inject.Inject;
import pl.llp.aircasting.helper.CalibrationHelper;
import pl.llp.aircasting.helper.LocationConversionHelper;
import pl.llp.aircasting.helper.ResourceHelper;
import pl.llp.aircasting.helper.SoundHelper;
import pl.llp.aircasting.model.SoundMeasurement;
import pl.llp.aircasting.view.presenter.MeasurementPresenter;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 9/26/11
 * Time: 3:23 PM
 */
public class SoundTraceOverlay extends BufferingOverlay<SoundMeasurement> {
    @Inject MeasurementPresenter measurementPresenter;
    @Inject ResourceHelper resourceHelper;
    @Inject SoundHelper soundHelper;
    @Inject CalibrationHelper calibrationHelper;

    private boolean isSaved;
    private int calibration;
    private int offset60DB;

    @Override
    protected void performDraw(Canvas canvas, Projection projection) {
        List<SoundMeasurement> fullView = measurementPresenter.getFullView();

        // Avoid concurrent modification
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0, fullViewSize = fullView.size(); i < fullViewSize - 1; i++) {
            SoundMeasurement measurement = fullView.get(i);
            drawPoint(canvas, projection, measurement);
        }
    }

    @Override
    protected void performUpdate(Canvas canvas, Projection projection, SoundMeasurement measurement) {
        drawPoint(canvas, projection, measurement);
    }

    private void drawPoint(Canvas canvas, Projection projection, SoundMeasurement measurement) {
        double value = measurement.getValue();

        if (isSaved) {
            value = calibrationHelper.calibrate(value, calibration, offset60DB);
        } else {
            value = calibrationHelper.calibrate(value);
        }

        if (soundHelper.shouldDisplayAbsolute(value)) {
            Drawable bullet = resourceHelper.getBulletAbsolute(value);

            GeoPoint geoPoint = LocationConversionHelper.geoPoint(measurement.getLatitude(), measurement.getLongitude());
            Point point = projection.toPixels(geoPoint, null);

            centerAt(bullet, point);
            bullet.draw(canvas);
        }
    }

    private void centerAt(Drawable bullet, Point point) {
        bullet.setBounds(
                point.x - bullet.getIntrinsicWidth() / 2,
                point.y - bullet.getIntrinsicWidth() / 2,
                point.x + bullet.getIntrinsicWidth() / 2,
                point.y + bullet.getIntrinsicHeight() / 2
        );
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    public void setCalibration(int newCalibration) {
        this.calibration = newCalibration;
    }

    public void setOffset60DB(int newOffset60DB) {
        this.offset60DB = newOffset60DB;
    }
}
