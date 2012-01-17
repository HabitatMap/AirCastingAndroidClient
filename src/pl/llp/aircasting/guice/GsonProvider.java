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

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import pl.llp.aircasting.gson.NoteAdapter;
import pl.llp.aircasting.model.Note;

import java.lang.reflect.Modifier;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/24/11
 * Time: 3:20 PM
 */
public class GsonProvider implements Provider<Gson> {
    @Inject GsonBuilder gsonBuilder;
    @Inject NoteAdapter noteAdapter;

    @Override
    public Gson get() {
        // All fields need to be explicitly marked with @Expose
        return gsonBuilder
                .excludeFieldsWithModifiers(Modifier.STATIC)
                .excludeFieldsWithoutExposeAnnotation()
                .setDateFormat("MMM d, yyyy h:m:s a z")
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(Note.class, noteAdapter)
                .create();
    }
}
