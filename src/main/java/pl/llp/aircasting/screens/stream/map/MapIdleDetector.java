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
package pl.llp.aircasting.screens.stream.map;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/3/11
 * Time: 4:01 PM
 */
public class MapIdleDetector implements AirCastingMapView.Listener {
    public static final int PERIOD = 100;

    private long idleTime;
    private MapIdleListener listener;
    private long lastMapMovement = System.currentTimeMillis();
    private Timer mapIdleTimer = new Timer(true);
    private boolean triggered = false;

    public static MapIdleDetector detectMapIdle(AirCastingMapView mapView, long idleTime, MapIdleListener listener) {
        return new MapIdleDetector(mapView, idleTime, listener);
    }

    public void stop() {
        mapIdleTimer.cancel();
    }

    private MapIdleDetector(AirCastingMapView mapView, long idleTime, MapIdleListener listener) {
        this.idleTime = idleTime;
        this.listener = listener;

        mapView.addListener(this);

        mapIdleTimer.schedule(new MapIdleTask(), 0, PERIOD);
    }

    @Override
    public void onMapViewChanged() {
        lastMapMovement = System.currentTimeMillis();
        triggered = false;
    }

    public interface MapIdleListener {
        public void onMapIdle();
    }

    private class MapIdleTask extends TimerTask {
        @Override
        public void run() {
            if (!triggered && System.currentTimeMillis() - lastMapMovement > idleTime) {
                listener.onMapIdle();
                triggered = true;
            }
        }
    }
}
