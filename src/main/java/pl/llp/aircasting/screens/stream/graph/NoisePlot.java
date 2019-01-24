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
package pl.llp.aircasting.screens.stream.graph;

import android.view.MotionEvent;
import pl.llp.aircasting.R;
import pl.llp.aircasting.screens.stream.base.AirCastingActivity;
import pl.llp.aircasting.util.Logger;
import pl.llp.aircasting.screens.common.helpers.ResourceHelper;
import pl.llp.aircasting.screens.common.helpers.SettingsHelper;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.Note;
import pl.llp.aircasting.model.Sensor;
import pl.llp.aircasting.model.internal.MeasurementLevel;
import pl.llp.aircasting.sensor.common.ThresholdsHolder;
import pl.llp.aircasting.util.Search;
import pl.llp.aircasting.screens.stream.MeasurementAggregator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import com.google.common.base.Stopwatch;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Lists.newArrayList;
import static pl.llp.aircasting.util.DrawableTransformer.centerBottomAt;
import static pl.llp.aircasting.util.Search.binarySearch;

public class NoisePlot extends View {
    public static final int OPAQUE = 255;
    private static final double CLICK_RADIUS = 50;

    private AirCastingActivity activity;
    private SettingsHelper settingsHelper;
    private ThresholdsHolder thresholds;

    private ArrayList<Measurement> measurements = new ArrayList<Measurement>();
    private ArrayList<Note> notes;
    private ResourceHelper resourceHelper;
    private int bottom;
    private int top;
    private Sensor sensor;

    MeasurementAggregator aggregator = new MeasurementAggregator();

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

    public void initialize(AirCastingActivity activity, SettingsHelper settingsHelper, ThresholdsHolder thresholds, ResourceHelper resourceHelper) {
        this.activity = activity;
        this.settingsHelper = settingsHelper;
        this.thresholds = thresholds;
        this.resourceHelper = resourceHelper;
    }

    @SuppressWarnings("AssignmentToCollectionOrArrayFieldFromParameter")
    public void update(Sensor sensor, ArrayList<Measurement> measurements, ArrayList<Note> notes) {
        this.measurements = measurements;
        this.notes = notes;
        this.sensor = sensor;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Stopwatch stopwatch = new Stopwatch().start();

        bottom = thresholds.getValue(sensor, MeasurementLevel.VERY_LOW);
        top = thresholds.getValue(sensor, MeasurementLevel.VERY_HIGH);

        Paint paint = new Paint();
        drawBackground(canvas, paint);

        if (!measurements.isEmpty()) {
            measurements = aggregator.smoothenSamplesToReduceCount(newArrayList(measurements), 1000);
            Path path = new Path();

            float lastY = project(measurements.get(0).getValue());
            path.moveTo(0, lastY);

            // Avoid concurrent modification
            for (int i = 1; i < measurements.size(); i++) {
                Measurement measurement = measurements.get(i);
                Point place = place(measurement);
                path.lineTo(place.x, place.y);
            }
            Logger.logGraphPerformance("onDraw to path creation took " + stopwatch.elapsed(TimeUnit.MILLISECONDS));

            initializePaint(paint);

            canvas.drawPath(path, paint);
            Logger.logGraphPerformance("onDraw to path draw took " + stopwatch.elapsed(TimeUnit.MILLISECONDS));

            for (Note note : notes) {
                drawNote(canvas, note);
            }

            if (settingsHelper.showGraphMetadata()) {
                String message = "[" + measurements.size() + "] pts";
                String message2 = "drawing took " + stopwatch.elapsed(TimeUnit.MILLISECONDS);
                long textSize = getResources().getDimensionPixelSize(R.dimen.debugFontSize);
                Paint textPaint = new Paint();
                textPaint.setColor(Color.WHITE);
                textPaint.setAlpha(OPAQUE);
                textPaint.setAntiAlias(true);
                textPaint.setTextSize(textSize);

                float textWidth = Math.max(textPaint.measureText(message), textPaint.measureText(message2));

                canvas.drawText(message, getWidth() - textWidth - 5, getHeight() - textSize - 5, textPaint);
                canvas.drawText(message2, getWidth() - textWidth - 5, getHeight() - 5, textPaint);
            }
        }
    }

    private Point place(Measurement measurement) {
        long time = measurement.getTime().getTime();
        float span = lastTime() - firstTime();
        float place = time - firstTime();
        int x = (int) (getWidth() * (place / span));

        double value = measurement.getValue();
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

    private void initializePaint(Paint paint) {
        paint.setColor(Color.WHITE);
        paint.setAlpha(OPAQUE);
        paint.setStrokeWidth(9);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);
    }

    private void drawBackground(Canvas canvas, Paint paint) {
        // Make the NoisePlot play nicely with Layout preview
        if (resourceHelper == null || settingsHelper == null) return;

        paint.setStrokeWidth(0);
        paint.setStyle(Paint.Style.FILL);

        int lastThreshold = top;

        for (MeasurementLevel measurementLevel : new MeasurementLevel[]{MeasurementLevel.HIGH, MeasurementLevel.MID, MeasurementLevel.LOW}) {
            paint.setColor(resourceHelper.getGraphColor(measurementLevel));
            int threshold = settingsHelper.getThreshold(sensor, measurementLevel);

            canvas.drawRect(0, project(lastThreshold), getWidth(), project(threshold), paint);
            lastThreshold = threshold;
        }

        paint.setColor(resourceHelper.getGraphColor(MeasurementLevel.VERY_LOW));
        canvas.drawRect(0, project(lastThreshold), getWidth(), getHeight(), paint);
    }

    private int project(double value) {
        return (int) (getHeight() * (top - value) / (top - bottom));
    }

    public boolean onTap(MotionEvent event) {
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

    private boolean isClose(MotionEvent event, Point place) {
        float x = event.getX() - place.x;
        float y = event.getY() - place.y;
        return Math.sqrt(x * x + y * y) < CLICK_RADIUS;
    }
}
