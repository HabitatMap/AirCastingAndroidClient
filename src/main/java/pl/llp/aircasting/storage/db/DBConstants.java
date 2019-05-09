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
package pl.llp.aircasting.storage.db;

import android.provider.BaseColumns;

public interface DBConstants
{
  String DB_NAME = "sessions.db";

  int DB_VERSION = 36;

  String SESSION_TABLE_NAME = "Sessions";
  String SESSION_ID = BaseColumns._ID;
  String SESSION_TITLE = "Title";
  String SESSION_TAGS = "Tags";
  String SESSION_START = "Start";
  String SESSION_END = "End";
  String SESSION_UUID = "UUID";
  /**
   * Location on the server (unique url component)
   **/
  String SESSION_LOCATION = "Location";
  String SESSION_CALIBRATION = "Calibration";
  String SESSION_CONTRIBUTE = "Contribute";

  String SESSION_MARKED_FOR_REMOVAL = "MarkedForRemoval";
  String SESSION_SUBMITTED_FOR_REMOVAL = "SubmittedForRemoval";
  String SESSION_CALIBRATED = "is_calibrated";
  /**
   * recorded without location
   **/
  String SESSION_LOCAL_ONLY = "local_only";
  /**
   * in progress (part of continuous session tracking)
   **/
  String SESSION_INCOMPLETE = "incomplete";

  String SESSION_TYPE = "type";
  String SESSION_INDOOR = "is_indoor";
  String SESSION_LATITUDE = "Latitude";
  String SESSION_LONGITUDE = "Longitude";

  /** @deprecated belongs to streams */
  String DEPRECATED_SESSION_AVG = "Average";
  /** @deprecated belongs to streams */
  String DEPRECATED_SESSION_PEAK = "Peak";

  String MEASUREMENT_TABLE_NAME = "Measurements";
  String MEASUREMENT_ID = BaseColumns._ID;
  String MEASUREMENT_SESSION_ID = "SessionID";
  String MEASUREMENT_LATITUDE = "Latitude";
  String MEASUREMENT_LONGITUDE = "Longitude";
  String MEASUREMENT_VALUE = "Value";
  String MEASUREMENT_TIME = "Time";
  String MEASUREMENT_STREAM_ID = "stream_id";
  String MEASUREMENT_MEASURED_VALUE = "measured_value";

  String STREAM_TABLE_NAME = "streams";
  String STREAM_ID = BaseColumns._ID;
  String STREAM_AVG = "average";
  String STREAM_PEAK = "peak";
  String STREAM_SESSION_ID = "stream_session_id";
  String STREAM_SENSOR_NAME = "sensor_name";
  String STREAM_SENSOR_PACKAGE_NAME = "sensor_package_name";
  String STREAM_MEASUREMENT_TYPE = "measurement_type";
  String STREAM_SHORT_TYPE= "short_type";
  String STREAM_MEASUREMENT_UNIT = "measurement_unit";
  String STREAM_MEASUREMENT_SYMBOL = "measurement_symbol";
  String STREAM_THRESHOLD_VERY_LOW = "threshold_very_low";
  String STREAM_THRESHOLD_LOW = "threshold_low";
  String STREAM_THRESHOLD_MEDIUM = "threshold_mid";
  String STREAM_THRESHOLD_HIGH = "threshold_high";
  String STREAM_THRESHOLD_VERY_HIGH = "threshold_very_high";
  String STREAM_MARKED_FOR_REMOVAL = "marked_for_removal";
  String STREAM_SUBMITTED_FOR_REMOVAL = "submitted_for_removal";

  String NOTE_TABLE_NAME = "Notes";
  String NOTE_SESSION_ID = "SessionID";
  String NOTE_LATITUDE = "Latitude";
  String NOTE_LONGITUDE = "Longitude";
  String NOTE_TEXT = "Text";
  String NOTE_DATE = "Date";
  String NOTE_PHOTO = "Photo";
  String NOTE_NUMBER = "Number";

  String REGRESSION_TABLE_NAME = "regressions";
  String REGRESSION_ID = BaseColumns._ID;
  String REGRESSION_COEFFICIENTS = "coefficients";
  String REGRESSION_THRESHOLD_LOW = "threshold_low";
  String REGRESSION_THRESHOLD_VERY_LOW = "threshold_very_low";
  String REGRESSION_THRESHOLD_HIGH = "threshold_high";
  String REGRESSION_THRESHOLD_VERY_HIGH = "threshold_very_high";
  String REGRESSION_THRESHOLD_MEDIUM = "threshold_mid";
  String REGRESSION_MEASUREMENT_UNIT = "measurement_unit";
  String REGRESSION_MEASUREMENT_SYMBOL = "measurement_symbol";
  String REGRESSION_MEASUREMENT_TYPE = "measurement_type";
  String REGRESSION_SHORT_TYPE = "measurement_short_type";
  String REGRESSION_SENSOR_NAME = "sensor_name";
  String REGRESSION_SENSOR_PACKAGE_NAME = "sensor_package_name";
  String REGRESSION_REFERENCE_SENSOR_NAME = "reference_sensor_name";
  String REGRESSION_REFERENCE_SENSOR_PACKAGE_NAME = "reference_sensor_package_name";
  String REGRESSION_IS_OWNER = "is_owner";
  String REGRESSION_BACKEND_ID = "backend_id";
  String REGRESSION_IS_ENABLED = "is_enabled";
  String REGRESSION_CREATED_AT = "created_at";

}
