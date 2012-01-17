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
import com.google.common.cache.*;
import com.google.inject.Singleton;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/23/11
 * Time: 1:58 PM
 */
@Singleton
public class BitmapHolder {
    Cache<BitmapKey, Bitmap> cache =
            CacheBuilder.newBuilder()
                    .expireAfterAccess(30, TimeUnit.SECONDS)
                    .build(new CacheLoader<BitmapKey, Bitmap>() {
                        @Override
                        public Bitmap load(BitmapKey key) throws Exception {
                            return Bitmap.createBitmap(key.getWidth(), key.getHeight(), Bitmap.Config.ARGB_8888);
                        }
                    });

    public Bitmap getBitmap(int width, int height, int index) {
        try {
            if(width == 0 || height == 0) return null;

            BitmapKey key = new BitmapKey(width, height, index);
            return cache.get(key);
        } catch (ExecutionException e) {
            //Should never happen
            throw new RuntimeException(e);
        }
    }
}
