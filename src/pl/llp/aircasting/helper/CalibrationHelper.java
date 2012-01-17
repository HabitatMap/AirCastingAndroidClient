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
import pl.llp.aircasting.model.Session;

import static pl.llp.aircasting.util.Projection.project;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 12/1/11
 * Time: 3:33 PM
 */
public class CalibrationHelper {
    @Inject SettingsHelper settingsHelper;

    public double calibrate(double value) {
        int calibration = settingsHelper.getCalibration();
        int offset60DB = settingsHelper.getOffset60DB();

        return calibrate(value, calibration, offset60DB);
    }

    public double calibrate(double value, int calibration, int offset60DB) {
        int highCalibrated = calibration;
        int low = -(highCalibrated - 60) + offset60DB;

        if (low == 0) return 0;
        return project(value, low, 0, 60, highCalibrated);
    }

    public double calibrate(double power, Session session) {
        if (session.isSaved()) {
            return calibrate(power, session.getCalibration(), session.getOffset60DB());
        } else {
            return calibrate(power);
        }
    }
}
