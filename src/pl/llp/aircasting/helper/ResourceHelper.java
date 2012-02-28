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
import pl.llp.aircasting.R;
import pl.llp.aircasting.SoundLevel;
import roboguice.inject.InjectResource;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/17/11
 * Time: 12:54 PM
 */
@Singleton
public class ResourceHelper {
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
    
    @InjectResource(R.color.gps_route) int gpsRoute;

    @Inject SoundHelper soundHelper;

    public Drawable getGaugeAbsolute(double value, MarkerSize size) {
        switch (soundHelper.soundLevelAbsolute(value)) {
            case INDISTINCT:
            case QUIET:
                return size == MarkerSize.SMALL ? smallGreenGauge : bigGreenGauge;
            case AVERAGE:
                return size == MarkerSize.SMALL ? smallYellowGauge : bigYellowGauge;
            case LOUD:
                return size == MarkerSize.SMALL ? smallOrangeGauge : bigOrangeGauge;
            default:
                return size == MarkerSize.SMALL ? smallRedGauge : bigRedGauge;
        }
    }

    public int getGraphColor(SoundLevel soundLevel) {
        switch (soundLevel) {
            case QUIET:
                return graphGreen;
            case AVERAGE:
                return graphYellow;
            case LOUD:
                return graphOrange;
            default:
                return graphRed;
        }
    }

    public Drawable getLocationBullet(double value) {
        switch (soundHelper.soundLevel(value)) {
            case AVERAGE:
                return dotYellow;
            case LOUD:
                return dotOrange;
            case VERY_LOUD:
            case TOO_LOUD:
                return dotRed;
            case INDISTINCT:
            case QUIET:
            default:
                return dotGreen;
        }
    }

    public int getColorAbsolute(double value) {
        switch (soundHelper.soundLevelAbsolute(value)) {
            case QUIET:
                return green;
            case AVERAGE:
                return yellow;
            case LOUD:
                return orange;
            default:
                return red;
        }
    }

    public Drawable getBulletAbsolute(double value) {
        SoundLevel soundLevel = soundHelper.soundLevelAbsolute(value);

        switch (soundLevel) {
            case INDISTINCT:
            case QUIET:
                return greenBullet;
            case AVERAGE:
                return yellowBullet;
            case LOUD:
                return orangeBullet;
            case VERY_LOUD:
            default:
                return redBullet;
        }
    }

    public float getTextSize(double power, MarkerSize size) {
        switch (size) {
            case SMALL:
                return power < 100 ? 35 : 25;
            case BIG:
            default:
                return power < 100 ? 40 : 30;
        }
    }

    public int getGpsRoute() {
        return gpsRoute;
    }
}
