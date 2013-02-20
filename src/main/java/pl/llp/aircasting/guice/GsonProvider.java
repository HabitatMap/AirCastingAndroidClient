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
package pl.llp.aircasting.guice;

import pl.llp.aircasting.api.gson.LocalDateAdapter;
import pl.llp.aircasting.api.gson.MeasurementAdapter;
import pl.llp.aircasting.api.gson.NoteAdapter;
import pl.llp.aircasting.model.Measurement;
import pl.llp.aircasting.model.Note;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Provider;

import java.lang.reflect.Modifier;
import java.util.Date;

public class GsonProvider implements Provider<Gson>
{
  // 2012-01-29T10:22:33+0200
  public static final String ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

  NoteAdapter noteAdapter = new NoteAdapter();
  LocalDateAdapter localDateAdapter = new LocalDateAdapter();
  MeasurementAdapter measurementAdapter = new MeasurementAdapter();

  @Override
  public Gson get()
  {
    // All fields need to be explicitly marked with @Expose
    return new GsonBuilder()
        .excludeFieldsWithModifiers(Modifier.STATIC)
        .excludeFieldsWithoutExposeAnnotation()
        .setDateFormat(ISO_8601)
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter(Date.class, localDateAdapter)
        .registerTypeAdapter(Note.class, noteAdapter)
        .registerTypeAdapter(Measurement.class, measurementAdapter)
        .create();
  }
}
