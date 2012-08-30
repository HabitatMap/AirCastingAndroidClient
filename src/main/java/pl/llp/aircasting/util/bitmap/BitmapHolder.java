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

import pl.llp.aircasting.util.Constants;

import android.graphics.Bitmap;
import com.google.common.collect.MapMaker;
import com.google.inject.Singleton;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Map;

@Singleton
public class BitmapHolder {

    Map<BitmapKey, Bitmap> cache = new MapMaker().concurrencyLevel(1).makeMap();
  private ReferenceQueue<Bitmap> bitmapReferenceQueue = new ReferenceQueue<Bitmap>();

  public Bitmap getBitmap(int width, int height, int index)
    {
       if(width == 0 || height == 0) return null;
       BitmapKey key = new BitmapKey(width, height, index);

       if(!cache.containsKey(key))
       {
         Bitmap bitmap = Bitmap.createBitmap(key.getWidth(), key.getHeight(), Bitmap.Config.ARGB_8888);
         new PhantomReference<Bitmap>(bitmap, bitmapReferenceQueue);

         cache.put(key, bitmap);
       }

       return cache.get(key);
    }

   public void release(int width, int height)
   {
      for (Map.Entry<BitmapKey, Bitmap> entry : cache.entrySet())
      {
         BitmapKey key = entry.getKey();
         if((key.getWidth() == width) && (key.getHeight() == height))
         {
            cache.remove(key);
         }
      }

     new Thread(new Runnable()
     {
       @Override
       public void run()
       {
         for (int i = 0; i < 5; i++)
         {
           try
           {
             while(bitmapReferenceQueue.poll() != null)
             {
               Reference<? extends Bitmap> ref = bitmapReferenceQueue.remove();
               Bitmap bitmap = ref.get();
               if(bitmap != null)
               {
                 bitmap.recycle();
               }
             }
             Thread.sleep(Constants.ONE_SECOND);
           }
           catch (InterruptedException ignored) { }
         }
       }
     }).start();
   }
}
