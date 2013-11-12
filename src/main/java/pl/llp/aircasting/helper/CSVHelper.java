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

import android.util.Log;
import pl.llp.aircasting.android.Logger;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.MeasurementStream;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.util.Constants;

import android.net.Uri;
import android.os.Environment;
import com.csvreader.CsvWriter;
import com.google.common.base.Strings;
import com.google.common.io.Closer;

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

import static java.lang.String.valueOf;

public class CSVHelper
{
  public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
  // Gmail app hack - it requires all file attachments to begin with /mnt/sdcard
  public static final String SESSION_ZIP_FILE = "aircasting_session_archive";
  public static final String SESSION_FALLBACK_FILE = "session_data";
  private static final int MINIMUM_SESSION_NAME_LENGTH = 2;

  private final String ZIP_EXTENSION = ".zip";
  private final String CSV_EXTENSION = ".csv";


  final Closer closer = Closer.create();

  public Uri prepareCSV(Session session) throws IOException
  {
    try
    {
      File storage = Environment.getExternalStorageDirectory();
      File dir = new File(storage, "aircasting_sessions");
      dir.mkdirs();
      File file = new File(dir, fileName(session.getTitle()) + ZIP_EXTENSION);
      OutputStream outputStream = new FileOutputStream(file);
      closer.register(outputStream);
      ZipOutputStream zippedOutputStream = new ZipOutputStream(outputStream);
      zippedOutputStream.putNextEntry(new ZipEntry(fileName(session.getTitle()) + CSV_EXTENSION));

      Writer writer = new OutputStreamWriter(zippedOutputStream);

      CsvWriter csvWriter = new CsvWriter(writer, ',');
      write(session).toWriter(csvWriter);

      csvWriter.flush();
      csvWriter.close();

      Uri uri = Uri.fromFile(file);
      if(Constants.isDevMode())
      {
        Logger.i("File path [" + uri + "]");
      }
      return uri;
    }
    finally
    {
      closer.close();
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

    return result.length() > MINIMUM_SESSION_NAME_LENGTH ? result.toString() : SESSION_FALLBACK_FILE;
  }

  private SessionWriter write(Session session)
  {
    return new SessionWriter(session);
  }
}

class SessionWriter
{
  final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat(CSVHelper.TIMESTAMP_FORMAT);

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
      writeSensorHeader(writer);
      writeSensor(stream, writer);

      writeMeasurementHeader(writer);

      for (Measurement measurement : stream.getMeasurements())
      {
        writeMeasurement(writer, measurement);
      }
    }
  }

  private void writeMeasurementHeader(CsvWriter writer) throws IOException
  {
    writer.write("Timestamp");
    writer.write("geo:lat");
    writer.write("geo:long");
    writer.write("Value");
    writer.endRecord();
  }

  private void writeMeasurement(CsvWriter writer, Measurement measurement) throws IOException
  {
    writer.write(TIMESTAMP_FORMAT.format(measurement.getTime()));
    writer.write(valueOf(measurement.getLongitude()));
    writer.write(valueOf(measurement.getLatitude()));
    writer.write(valueOf(measurement.getValue()));
    writer.endRecord();
  }

  private void writeSensor(MeasurementStream stream, CsvWriter writer) throws IOException
  {
    writer.write(stream.getSensorName());
    writer.write(stream.getPackageName());
    writer.write(stream.getMeasurementType());
    writer.write(valueOf(stream.getUnit()));
    writer.endRecord();
  }

  private void writeSensorHeader(CsvWriter writer) throws IOException
  {
    writer.write("sensor:model");
    writer.write("sensor:package");
    writer.write("sensor:capability");
    writer.write("sensor:units");
    writer.endRecord();
  }
}
