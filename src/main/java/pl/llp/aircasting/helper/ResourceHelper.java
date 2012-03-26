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
package pl.llp.aircasting.helper;

import android.graphics.drawable.Drawable;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.MarkerSize;
import pl.llp.aircasting.MeasurementLevel;
import pl.llp.aircasting.R;
import pl.llp.aircasting.sensor.builtin.SimpleAudioReader;
import roboguice.inject.InjectResource;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/17/11
 * Time: 12:54 PM
 */
@Singleton
public class ResourceHelper {
    public static final int THREE_DIGITS = 100;
    public static final int SMALL_GAUGE_BIG_TEXT = 35;
    public static final int SMALL_GAUGE_SMALL_TEXT = 25;
    public static final int BIG_GAUGE_BIG_TEXT = 40;
    public static final int BIG_GAUGE_SMALL_TEXT = 30;

    @InjectResource(R.drawable.green) Drawable greenBullet;
    @InjectResource(R.drawable.yellow) Drawable yellowBullet;
    @InjectResource(R.drawable.orange) Drawable orangeBullet;
    @InjectResource(R.drawable.red) Drawable redBullet;

    @InjectResource(R.drawable.round_bottom_green) Drawable smallGreenGauge;
    @InjectResource(R.drawable.round_bottom_green_big) Drawable bigGreenGauge;
    @InjectResource(R.drawable.round_bottom_yellow) Drawable smallYellowGauge;
    @InjectResource(R.drawable.round_bottom_yellow_big) Drawable bigYellowGauge;
    @InjectResource(R.drawable.round_bottom_orange) Drawable smallOrangeGauge;
    @InjectResource(R.drawable.round_bottom_orange_big) Drawable bigOrangeGauge;
    @InjectResource(R.drawable.round_bottom_red) Drawable smallRedGauge;
    @InjectResource(R.drawable.round_bottom_red_big) Drawable bigRedGauge;

    @InjectResource(R.color.green) int green;
    @InjectResource(R.color.orange) int orange;
    @InjectResource(R.color.yellow) int yellow;
    @InjectResource(R.color.red) int red;

    @InjectResource(R.color.graph_green) int graphGreen;
    @InjectResource(R.color.graph_orange) int graphOrange;
    @InjectResource(R.color.graph_yellow) int graphYellow;
    @InjectResource(R.color.graph_red) int graphRed;

    @InjectResource(R.drawable.dot_green) Drawable dotGreen;
    @InjectResource(R.drawable.dot_yellow) Drawable dotYellow;
    @InjectResource(R.drawable.dot_orange) Drawable dotOrange;
    @InjectResource(R.drawable.dot_red) Drawable dotRed;
    @InjectResource(R.drawable.arrow_down) Drawable noteArrow;

    @InjectResource(R.color.gps_route) int gpsRoute;

    @Inject SoundHelper soundHelper;

    public Drawable getGaugeAbsolute(String sensorName, MarkerSize size, double value) {
        switch (soundHelper.soundLevelAbsolute(sensorName, value)) {
            case TOO_LOW:
            case VERY_LOW:
                return size == MarkerSize.SMALL ? smallGreenGauge : bigGreenGauge;
            case LOW:
                return size == MarkerSize.SMALL ? smallYellowGauge : bigYellowGauge;
            case MID:
                return size == MarkerSize.SMALL ? smallOrangeGauge : bigOrangeGauge;
            default:
                return size == MarkerSize.SMALL ? smallRedGauge : bigRedGauge;
        }
    }

    public int getGraphColor(MeasurementLevel measurementLevel) {
        switch (measurementLevel) {
            case VERY_LOW:
                return graphGreen;
            case LOW:
                return graphYellow;
            case MID:
                return graphOrange;
            default:
                return graphRed;
        }
    }

    public Drawable getLocationBullet(double value) {
        switch (soundHelper.soundLevel(value)) {
            case LOW:
                return dotYellow;
            case MID:
                return dotOrange;
            case HIGH:
            case VERY_HIGH:
                return dotRed;
            case TOO_LOW:
            case VERY_LOW:
            default:
                return dotGreen;
        }
    }

    public int getColorAbsolute(double value) {
        switch (soundHelper.soundLevelAbsolute(SimpleAudioReader.SENSOR_NAME, value)) {
            case VERY_LOW:
                return green;
            case LOW:
                return yellow;
            case MID:
                return orange;
            default:
                return red;
        }
    }

    public Drawable getBulletAbsolute(double value) {
        MeasurementLevel measurementLevel = soundHelper.soundLevelAbsolute(SimpleAudioReader.SENSOR_NAME, value);

        switch (measurementLevel) {
            case TOO_LOW:
            case VERY_LOW:
                return greenBullet;
            case LOW:
                return yellowBullet;
            case MID:
                return orangeBullet;
            case HIGH:
            default:
                return redBullet;
        }
    }

    public float getTextSize(double power, MarkerSize size) {
        switch (size) {
            case SMALL:
                return power < THREE_DIGITS ? SMALL_GAUGE_BIG_TEXT : SMALL_GAUGE_SMALL_TEXT;
            case BIG:
            default:
                return power < THREE_DIGITS ? BIG_GAUGE_BIG_TEXT : BIG_GAUGE_SMALL_TEXT;
        }
    }

    public int getGpsRoute() {
        return gpsRoute;
    }

    public Drawable getNoteArrow() {
        return noteArrow;
    }
}
