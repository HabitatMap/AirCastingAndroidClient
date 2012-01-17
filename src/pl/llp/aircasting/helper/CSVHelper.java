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

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/21/11
 * Time: 2:31 PM
 */
public class CSVHelper {
    @Inject CalibrationHelper calibrationHelper;

    public static final String SESSION_TEMP_FILE = "session.csv";

    public Uri prepareCSV(Context context, Session session) throws IOException {
        OutputStream outputStream = null;

        try {
            outputStream = context.openFileOutput(SESSION_TEMP_FILE, Context.MODE_WORLD_READABLE);
            Writer writer = new OutputStreamWriter(outputStream);

            CsvWriter csvWriter = new CsvWriter(writer, ',');

            // Should we have this in strings.xml?
            // My rationale for not putting it there is that it probably shouldn't be localized
            csvWriter.write("Date");
            csvWriter.write("Time");
            csvWriter.write("Latitude");
            csvWriter.write("Longitude");
            csvWriter.write("Decibel Level");
            csvWriter.endRecord();

            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

            for (SoundMeasurement measurement : session.getSoundMeasurements()) {
                csvWriter.write(dateFormat.format(measurement.getTime()));
                csvWriter.write(timeFormat.format(measurement.getTime()));
                csvWriter.write("" + measurement.getLatitude());
                csvWriter.write("" + measurement.getLongitude());
                csvWriter.write("" + calibrationHelper.calibrate(measurement.getValue()));
                csvWriter.endRecord();
            }
            csvWriter.close();

            // Gmail app hack - it requires all file attachments to begin with /mnt/sdcard
            File file = new File("/mnt/sdcard/../.." + context.getFilesDir() + "/" + SESSION_TEMP_FILE);
            return Uri.fromFile(file);
        } finally {
            closeQuietly(outputStream);
        }
    }
}
