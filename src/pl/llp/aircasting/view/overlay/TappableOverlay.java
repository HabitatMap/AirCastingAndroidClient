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
package pl.llp.aircasting.view.overlay;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.inject.Inject;
import pl.llp.aircasting.event.TapEvent;
import roboguice.activity.RoboActivity;
import roboguice.event.EventManager;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/4/11
 * Time: 11:54 AM
 */
public class TappableOverlay extends Overlay {
    private static final String TAG = TappableOverlay.class.getSimpleName();

    @Inject EventManager eventManager;

    Context context;

    @Override
    public boolean onTap(GeoPoint point, MapView view) {
        Log.d(TAG, "Tap triggered");
        eventManager.fire(context, new TapEvent());
        return false;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}

