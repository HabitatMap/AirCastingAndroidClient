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
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import pl.llp.aircasting.MeasurementLevel;
import pl.llp.aircasting.activity.AirCastingActivity;
import pl.llp.aircasting.event.ui.TapEvent;
import pl.llp.aircasting.helper.CalibrationHelper;
import pl.llp.aircasting.helper.ResourceHelper;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.util.Search;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Iterables.skip;
import static pl.llp.aircasting.util.DrawableTransformer.centerBottomAt;
import static pl.llp.aircasting.util.Search.binarySearch;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/3/11
 * Time: 12:06 PM
 */
public class NoisePlot extends View {
    public static final int OPAQUE = 255;
    private static final double CLICK_RADIUS = 50;

    private Paint paint = new Paint();

    private AirCastingActivity activity;
    private SettingsHelper settingsHelper;

    private List<Measurement> measurements = new ArrayList<Measurement>();
    private List<Note> notes;
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

    public void initialize(AirCastingActivity activity, SettingsHelper settingsHelper, ResourceHelper resourceHelper, CalibrationHelper calibrationHelper) {
        this.activity = activity;
        this.settingsHelper = settingsHelper;
        this.resourceHelper = resourceHelper;
        this.calibrationHelper = calibrationHelper;

        bottom = settingsHelper.getThreshold(MeasurementLevel.VERY_LOW);
        top = settingsHelper.getThreshold(MeasurementLevel.VERY_HIGH);
    }

    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    public void update(List<Measurement> measurements, List<Note> notes) {
        this.measurements = measurements;
        this.notes = notes;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBackground(canvas);

        if (!measurements.isEmpty()) {
            Path path = new Path();

            float lastY = project(calibrationHelper.calibrate(measurements.get(0).getValue()));
            path.moveTo(0, lastY);

            for (Measurement measurement : skip(measurements, 1)) {
                Point place = place(measurement);
                path.lineTo(place.x, place.y);
            }

            initializePaint();

            canvas.drawPath(path, paint);

            for (Note note : notes) {
                drawNote(canvas, note);
            }
        }
    }

    private Point place(Measurement measurement) {
        long time = measurement.getTime().getTime();
        float span = lastTime() - firstTime();
        float place = time - firstTime();
        int x = (int) (getWidth() * (place / span));

        double value = calibrationHelper.calibrate(measurement.getValue());
        int y = project(value);

        return new Point(x, y);
    }

    private void drawNote(Canvas canvas, Note note) {
        if (!inRange(note)) return;

        Point place = place(note);

        Drawable noteArrow = resourceHelper.getNoteArrow();
        centerBottomAt(noteArrow, place);
        noteArrow.draw(canvas);
    }

    private Point place(Note note) {
        Measurement measurement = findClosestMeasurement(note);
        return place(measurement);
    }

    private boolean inRange(Note note) {
        long noteTime = note.getDate().getTime();
        return noteTime >= firstTime() && noteTime <= lastTime();
    }

    private long lastTime() {
        return getLast(measurements).getTime().getTime();
    }

    private long firstTime() {
        return measurements.get(0).getTime().getTime();
    }

    private Measurement findClosestMeasurement(final Note note) {
        int index = binarySearch(measurements, new Search.Visitor<Measurement>() {
            @Override
            public int compareTo(Measurement value) {
                return note.getDate().compareTo(value.getTime());
            }
        });

        return measurements.get(index);
    }

    private void initializePaint() {
        paint.setColor(Color.WHITE);
        paint.setAlpha(OPAQUE);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);
    }

    private void drawBackground(Canvas canvas) {
        // Make the NoisePlot play nicely with Layout preview
        if (resourceHelper == null || settingsHelper == null) return;

        paint.setStrokeWidth(0);
        paint.setStyle(Paint.Style.FILL);

        int lastThreshold = top;

        for (MeasurementLevel measurementLevel : new MeasurementLevel[]{MeasurementLevel.HIGH, MeasurementLevel.MID, MeasurementLevel.LOW}) {
            paint.setColor(resourceHelper.getGraphColor(measurementLevel));
            int threshold = settingsHelper.getThreshold(measurementLevel);

            canvas.drawRect(0, project(lastThreshold), getWidth(), project(threshold), paint);
            lastThreshold = threshold;
        }

        paint.setColor(resourceHelper.getGraphColor(MeasurementLevel.VERY_LOW));
        canvas.drawRect(0, project(lastThreshold), getWidth(), getHeight(), paint);
    }

    private int project(double value) {
        return (int) (getHeight() * (top - value) / (top - bottom));
    }

    public boolean onTap(TapEvent event) {
        for (int i = 0; i < notes.size(); i++) {
            Note note = notes.get(i);
            Point place = place(note);

            if (inRange(note) && isClose(event, place)) {
                activity.noteClicked(i);
                return true;
            }
        }
        return false;
    }

    private boolean isClose(TapEvent event, Point place) {
        float x = event.getX() - place.x;
        float y = event.getY() - place.y;
        return Math.sqrt(x * x + y * y) < CLICK_RADIUS;
    }
}
