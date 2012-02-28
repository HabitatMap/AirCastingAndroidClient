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
package pl.llp.aircasting.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import pl.llp.aircasting.SoundLevel;
import pl.llp.aircasting.helper.CalibrationHelper;
import pl.llp.aircasting.helper.ResourceHelper;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.SoundMeasurement;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Iterables.skip;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/3/11
 * Time: 12:06 PM
 */
public class NoisePlot extends View {
    public static final int OPAQUE = 255;
    private Paint paint = new Paint();

    private SettingsHelper settingsHelper;

    private List<SoundMeasurement> measurements = new ArrayList<SoundMeasurement>();
    private ResourceHelper resourceHelper;
    private CalibrationHelper calibrationHelper;
    private int bottom;
    private int top;

    @SuppressWarnings("UnusedDeclaration")
    public NoisePlot(Context context) {
        super(context);
    }

    @SuppressWarnings("UnusedDeclaration")
    public NoisePlot(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressWarnings("UnusedDeclaration")
    public NoisePlot(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void initialize(SettingsHelper settingsHelper, ResourceHelper resourceHelper, CalibrationHelper calibrationHelper) {
        this.settingsHelper = settingsHelper;
        this.resourceHelper = resourceHelper;
        this.calibrationHelper = calibrationHelper;

        bottom = settingsHelper.getThreshold(SoundLevel.QUIET);
        top = settingsHelper.getThreshold(SoundLevel.TOO_LOUD);
    }

    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    public void update(List<SoundMeasurement> measurements) {
        this.measurements = measurements;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBackground(canvas);

        if (!measurements.isEmpty()) {
            float span = measurements.get(measurements.size() - 1).getTime().getTime() - measurements.get(0).getTime().getTime();

            Path path = new Path();

            float lastY = project(calibrationHelper.calibrate(measurements.get(0).getValue()));
            path.moveTo(0, lastY);

            for (SoundMeasurement measurement : skip(measurements, 1)) {
                float place = measurement.getTime().getTime() - measurements.get(0).getTime().getTime();
                float x = getWidth() * (place / span);

                double value = calibrationHelper.calibrate(measurement.getValue());
                float y = (float) (project(value));

                path.lineTo(x, y);
            }

            paint.setColor(Color.WHITE);
            paint.setAlpha(OPAQUE);
            paint.setStrokeWidth(3);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setAntiAlias(true);

            canvas.drawPath(path, paint);
        }
    }

    private void drawBackground(Canvas canvas) {
        // Make the NoisePlot play nicely with Layout preview
        if (resourceHelper == null || settingsHelper == null) return;

        paint.setStrokeWidth(0);
        paint.setStyle(Paint.Style.FILL);

        int lastThreshold = top;

        for (SoundLevel soundLevel : new SoundLevel[]{SoundLevel.VERY_LOUD, SoundLevel.LOUD, SoundLevel.AVERAGE}) {
            paint.setColor(resourceHelper.getGraphColor(soundLevel));
            int threshold = settingsHelper.getThreshold(soundLevel);

            canvas.drawRect(0, project(lastThreshold), getWidth(), project(threshold), paint);
            lastThreshold = threshold;
        }

        paint.setColor(resourceHelper.getGraphColor(SoundLevel.QUIET));
        canvas.drawRect(0, project(lastThreshold), getWidth(), getHeight(), paint);
    }

    private int project(double value) {
        return (int) (getHeight() * (top - value) / (top - bottom));
    }
}
