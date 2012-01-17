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
package pl.llp.aircasting.model;

import android.provider.BaseColumns;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/24/11
 * Time: 11:34 AM
 */
public interface DBConstants {
    String DB_NAME = "sessions.db";
    int DB_VERSION = 20;

    String SESSION_TABLE_NAME = "Sessions";
    String SESSION_ID = BaseColumns._ID;
    String SESSION_TITLE = "Title";
    String SESSION_DESCRIPTION = "Description";
    String SESSION_TAGS = "Tags";
    String SESSION_AVG = "Average";
    String SESSION_PEAK = "Peak";
    String SESSION_START = "Start";
    String SESSION_END = "End";
    String SESSION_UUID = "UUID";
    String SESSION_LOCATION = "Location";
    String SESSION_CALIBRATION = "Calibration";
    String SESSION_CONTRIBUTE = "Contribute";
    String SESSION_PHONE_MODEL = "PhoneModel";
    String SESSION_INSTRUMENT = "Instrument";
    String SESSION_DATA_TYPE = "DataType";
    String SESSION_OS_VERSION = "OSVersion";
    String SESSION_OFFSET_60_DB = "Offset60DB";
    String SESSION_MARKED_FOR_REMOVAL = "MarkedForRemoval";

    String MEASUREMENT_TABLE_NAME = "Measurements";
    String MEASUREMENT_ID = BaseColumns._ID;
    String MEASUREMENT_SESSION_ID = "SessionID";
    String MEASUREMENT_LATITUDE = "Latitude";
    String MEASUREMENT_LONGITUDE = "Longitude";
    String MEASUREMENT_VALUE = "Value";
    String MEASUREMENT_TIME = "Time";

    String NOTE_TABLE_NAME = "Notes";
    String NOTE_SESSION_ID = "SessionID";
    String NOTE_LATITUDE = "Latitude";
    String NOTE_LONGITUDE = "Longitude";
    String NOTE_TEXT = "Text";
    String NOTE_DATE = "Date";
    String NOTE_PHOTO = "Photo";
    String NOTE_NUMBER = "Number";
}
