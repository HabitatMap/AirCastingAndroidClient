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

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.llp.aircasting.model.Note;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 12/30/11
 * Time: 4:22 PM
 */
@Singleton
public class PhotoHelper {
    private static final String TAG = PhotoHelper.class.getSimpleName();

    @Inject Context context;

    @Inject
    public void init() {
        // The ContentResolver injection provided by roboguice doesn't seem to work
        resolver = context.getContentResolver();
    }

    private ContentResolver resolver;

    public boolean photoExistsLocally(Note note) {
        String path = note.getPhotoPath();

        return photoExistsLocally(path);
    }

    private boolean photoExistsLocally(String path) {
        if (path == null) {
            return false;
        } else {
            File file = new File(path);

            return file.exists();
        }
    }

    public boolean photoExists(Note note) {
        String path = note.getPhotoPath();

        return photoExists(path);
    }

    public boolean photoExists(String path) {
        if (path == null) {
            return false;
        }

        Uri uri = Uri.parse(path);

        return "http".equals(uri.getScheme()) || photoExistsLocally(path);
    }
}
