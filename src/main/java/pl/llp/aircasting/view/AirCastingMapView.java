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
package pl.llp.aircasting.view;

import android.content.Context;
import android.util.AttributeSet;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/2/11
 * Time: 4:16 PM
 * <p/>
 * Adds functionality for listening to viewport change to the standard MapView
 */
public class AirCastingMapView extends MapView {
    private List<Listener> listeners = new ArrayList<Listener>();
    private int zoom;
    private GeoPoint center;

    @SuppressWarnings("UnusedDeclaration")
    public AirCastingMapView(Context context, String s) {
        super(context, s);
    }

    @SuppressWarnings("UnusedDeclaration")
    public AirCastingMapView(Context context, AttributeSet set) {
        super(context, set);
    }

    @SuppressWarnings("UnusedDeclaration")
    public AirCastingMapView(Context context, AttributeSet set, int i) {
        super(context, set, i);
    }

    private boolean compareAndSetNewCoords() {
        GeoPoint newCenter = getMapCenter();
        int newZoom = getZoomLevel();
        boolean result = (newCenter.equals(center)) && (newZoom == zoom);
        center = newCenter;
        zoom = newZoom;
        return result;
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        if (!compareAndSetNewCoords()) {
            notifyListeners();
        }
        super.onLayout(b, i, i1, i2, i3);
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (Listener listener : listeners) {
            listener.onMapViewChanged();
        }
    }

    public interface Listener {
        public void onMapViewChanged();
    }
}
