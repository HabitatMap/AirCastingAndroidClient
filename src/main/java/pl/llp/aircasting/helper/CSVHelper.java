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

import android.content.Context;
import android.net.Uri;
import com.csvreader.CsvWriter;
import com.google.inject.Inject;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.model.SoundMeasurement;

import java.io.*;
import java.text.SimpleDateFormat;

import static com.google.common.io.Closeables.closeQuietly;
import static java.lang.String.valueOf;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/21/11
 * Time: 2:31 PM
 */
public class CSVHelper {
    public static final String BASE_PATH = "/mnt/sdcard/../..";
    public static final String SESSION_TEMP_FILE = "session.csv";

    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

    @Inject CalibrationHelper calibrationHelper;

    public Uri prepareCSV(Context context, Session session) throws IOException {
        OutputStream outputStream = null;

        try {
            outputStream = context.openFileOutput(SESSION_TEMP_FILE, Context.MODE_WORLD_READABLE);
            Writer writer = new OutputStreamWriter(outputStream);

            CsvWriter csvWriter = new CsvWriter(writer, ',');

            csvWriter.write("Date");
            csvWriter.write("Time");
            csvWriter.write("Latitude");
            csvWriter.write("Longitude");
            csvWriter.write("Decibel Level");
            csvWriter.endRecord();

            for (SoundMeasurement measurement : session.getSoundMeasurements()) {
                csvWriter.write(DATE_FORMAT.format(measurement.getTime()));
                csvWriter.write(TIME_FORMAT.format(measurement.getTime()));
                csvWriter.write(valueOf(measurement.getLongitude()));
                csvWriter.write(valueOf(measurement.getLatitude()));
                csvWriter.write(valueOf(calibrationHelper.calibrate(measurement.getValue())));
                csvWriter.endRecord();
            }
            csvWriter.close();

            File file = getTarget(context);
            return Uri.fromFile(file);
        } finally {
            closeQuietly(outputStream);
        }
    }

    private File getTarget(Context context) {
        // Gmail app hack - it requires all file attachments to begin with /mnt/sdcard
        String path = new StringBuilder(BASE_PATH)
                .append(context.getFilesDir())
                .append("/")
                .append(SESSION_TEMP_FILE)
                .toString();
        return new File(path);
    }
}
