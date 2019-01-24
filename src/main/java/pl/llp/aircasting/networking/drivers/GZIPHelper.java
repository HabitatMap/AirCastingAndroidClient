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
package pl.llp.aircasting.networking.drivers;

import android.util.Base64OutputStream;

import pl.llp.aircasting.model.FixedSessionsMeasurement;
import pl.llp.aircasting.model.Session;
//import pl.llp.aircasting.util.base64.Base64OutputStream;



import com.google.gson.Gson;
import com.google.inject.Inject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

public class GZIPHelper
{
  @Inject Gson gson;

  public byte[] zippedSession(Session session) throws IOException
  {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    Base64OutputStream base64OutputStream = new Base64OutputStream(byteStream, 0);
    GZIPOutputStream gzip = new GZIPOutputStream(base64OutputStream);
    OutputStreamWriter writer = new OutputStreamWriter(gzip);
    gson.toJson(session, session.getClass(), writer);

    writer.flush();
    gzip.finish();
    writer.close();

    return byteStream.toByteArray();
  }

  public byte[] zippedFixedSessionsMeasurement(FixedSessionsMeasurement fixedSessionsMeasurement) throws IOException
  {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    Base64OutputStream base64OutputStream = new Base64OutputStream(byteStream, 0);
    GZIPOutputStream gzip = new GZIPOutputStream(base64OutputStream);
    OutputStreamWriter writer = new OutputStreamWriter(gzip);
    gson.toJson(fixedSessionsMeasurement, fixedSessionsMeasurement.getClass(), writer);

    writer.flush();
    gzip.finish();
    writer.close();

    return byteStream.toByteArray();
  }
}
