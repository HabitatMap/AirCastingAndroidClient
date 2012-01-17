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

import com.google.inject.Inject;
import pl.llp.aircasting.SoundLevel;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/10/11
 * Time: 3:33 PM
 */
public class SoundHelper {
    public static final double TOTALLY_QUIET = -80;

    private static final SoundLevel[] soundLevels = new SoundLevel[]{
            SoundLevel.TOO_LOUD,
            SoundLevel.VERY_LOUD,
            SoundLevel.LOUD,
            SoundLevel.AVERAGE,
            SoundLevel.QUIET
    };

    @Inject SettingsHelper settingsHelper;
    @Inject CalibrationHelper calibrationHelper;

    public SoundLevel soundLevel(double value) {
        double calibrated = calibrationHelper.calibrate(value);

        return soundLevelAbsolute(calibrated);
    }

    public SoundLevel soundLevelAbsolute(double value) {
        for (SoundLevel soundLevel : soundLevels) {
            if ((int) value > settingsHelper.getThreshold(soundLevel)) {
                return soundLevel;
            }
        }
        return SoundLevel.INDISTINCT;
    }

    public boolean shouldDisplayAbsolute(double value) {
        SoundLevel soundLevel = soundLevelAbsolute(value);
        return soundLevel != SoundLevel.TOO_LOUD && soundLevel != SoundLevel.INDISTINCT;
    }
}
