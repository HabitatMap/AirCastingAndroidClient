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
package pl.llp.aircasting.util.bitmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import pl.llp.aircasting.util.http.Uploadable;

import java.io.ByteArrayOutputStream;

import static com.google.common.io.Closeables.closeQuietly;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 12/30/11
 * Time: 11:47 AM
 */
public class BitmapTransformer {
    private static final double IMAGE_MAX_SIZE = 1200;

    public Uploadable readScaledBitmap(String path) {
        BitmapFactory.Options options = getBitmapSize(path);
        int size = calculateScale(options);

        options = new BitmapFactory.Options();
        options.inSampleSize = size;

        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        byte[] bytes = toBytes(bitmap);

        bitmap.recycle();

        return new Uploadable(path, bytes);
    }

    private static byte[] toBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = null;

        try {
            stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);

            return stream.toByteArray();
        } finally {
            closeQuietly(stream);
        }
    }

    private static BitmapFactory.Options getBitmapSize(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, options);
        return options;
    }

    /**
     * @param options
     * @return Calculates a power of two size that will make the image close to
     *         IMAGE_MAX_SIZE
     */
    private static int calculateScale(BitmapFactory.Options options) {
        int scale = 1;

        if (options.outHeight > IMAGE_MAX_SIZE || options.outWidth > IMAGE_MAX_SIZE) {
            int size = Math.max(options.outHeight, options.outWidth);
            scale = (int) Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) size) / Math.log(0.5)));
        }

        return scale;
    }
}
