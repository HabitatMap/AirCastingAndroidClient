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

import pl.llp.aircasting.guice.GsonProvider;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.util.Constants;

import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import com.csvreader.CsvWriter;
import com.google.common.base.Strings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.google.common.io.Closeables.closeQuietly;
import static java.lang.String.valueOf;

public class CSVHelper
{
  // Gmail app hack - it requires all file attachments to begin with /mnt/sdcard
  public static final String SESSION_ZIP_FILE = "session.zip";
  public static final String SESSION_TEMP_FILE = "session.csv";

  public Uri prepareCSV(Session session) throws IOException
  {
    OutputStream outputStream = null;

    try
    {
      File storage = Environment.getExternalStorageDirectory();
      File dir = new File(storage, "aircasting_sessions");
      dir.mkdirs();
      String sessionName = fileName(session.getTitle());
      File file = new File(dir, sessionName);
      outputStream = new FileOutputStream(file);

      ZipOutputStream zipped = new ZipOutputStream(outputStream);
      zipped.putNextEntry(new ZipEntry("session.csv"));
      Writer writer = new OutputStreamWriter(zipped);

      CsvWriter csvWriter = new CsvWriter(writer, ',');

      csvWriter.write("sensor:model");
      csvWriter.write("sensor:package");
      csvWriter.write("sensor:capability");
      csvWriter.write("Date");
      csvWriter.write("Time");
      csvWriter.write("Timestamp");
      csvWriter.write("geo:lat");
      csvWriter.write("geo:long");
      csvWriter.write("sensor:units");
      csvWriter.write("Value");
      csvWriter.endRecord();

      write(session).toWriter(csvWriter);

      csvWriter.flush();
      csvWriter.close();

      Uri uri = Uri.fromFile(file);
      if(Constants.isDevMode())
      {
        Log.i(Constants.TAG, "File path [" + uri + "]");
      }
      return uri;
    }
    finally
    {
      closeQuietly(outputStream);
    }
  }

  String fileName(String title)
  {
    StringBuilder result = new StringBuilder();
    if (!Strings.isNullOrEmpty(title))
    {
      try
      {
        Matcher matcher = Pattern.compile("([_\\-a-zA-Z0-9])*").matcher(title.toLowerCase());
        while(matcher.find())
        {
          result.append(matcher.group());
        }
      }
      catch (IllegalStateException ignore)
      {

      }
    }

    return result.length() > 0 ? result.append(".zip").toString() : SESSION_TEMP_FILE;
  }

  private SessionWriter write(Session session)
  {
    return new SessionWriter(session);
  }
}

class SessionWriter
{
  final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");
  final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");
  final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat(GsonProvider.ISO_8601);

  Session session;

  SessionWriter(Session session)
  {
    this.session = session;
  }

  void toWriter(CsvWriter writer) throws IOException
  {
    Iterable<MeasurementStream> streams = session.getActiveMeasurementStreams();
    for (MeasurementStream stream : streams)
    {
      for (Measurement measurement : stream.getMeasurements())
      {
        writer.write(stream.getSensorName());
        writer.write(stream.getPackageName());
        writer.write(stream.getMeasurementType());
        writer.write(DATE_FORMAT.format(measurement.getTime()));
        writer.write(TIME_FORMAT.format(measurement.getTime()));
        writer.write(TIMESTAMP_FORMAT.format(measurement.getTime()));
        writer.write(valueOf(measurement.getLongitude()));
        writer.write(valueOf(measurement.getLatitude()));
        writer.write(valueOf(stream.getUnit()));
        writer.write(valueOf(measurement.getValue()));
        writer.endRecord();
      }
    }
  }
}
