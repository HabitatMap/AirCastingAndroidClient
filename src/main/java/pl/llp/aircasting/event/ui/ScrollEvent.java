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
package pl.llp.aircasting.event.ui;

import android.view.MotionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/9/11
 * Time: 5:04 PM
 */
public class ScrollEvent {
    private MotionEvent event1, event2;
    private float distanceX, distanceY;

    public ScrollEvent(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
        this.event1 = event1;
        this.event2 = event2;
        this.distanceX = distanceX;
        this.distanceY = distanceY;
    }

    public MotionEvent getEvent1() {
        return event1;
    }

    public void setEvent1(MotionEvent event1) {
        this.event1 = event1;
    }

    public MotionEvent getEvent2() {
        return event2;
    }

    public void setEvent2(MotionEvent event2) {
        this.event2 = event2;
    }

    public float getDistanceX() {
        return distanceX;
    }

    public void setDistanceX(float distanceX) {
        this.distanceX = distanceX;
    }

    public float getDistanceY() {
        return distanceY;
    }

    public void setDistanceY(float distanceY) {
        this.distanceY = distanceY;
    }
}
