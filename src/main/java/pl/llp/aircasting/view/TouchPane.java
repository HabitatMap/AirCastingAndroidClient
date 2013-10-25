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

import pl.llp.aircasting.event.ui.DoubleTapEvent;
import pl.llp.aircasting.event.ui.LongClickEvent;
import pl.llp.aircasting.event.ui.ScrollEvent;
import pl.llp.aircasting.event.ui.TapEvent;
import pl.llp.aircasting.guice.AirCastingApplication;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/4/11
 * Time: 1:12 PM
 */
public class TouchPane extends View implements GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener {
    GestureDetector gestureDetector = new GestureDetector(this);
    @Inject EventBus eventBus;

    @SuppressWarnings("UnusedDeclaration")
    public TouchPane(Context context) {
        super(context);
        initialize(context);
    }

    @SuppressWarnings("UnusedDeclaration")
    public TouchPane(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    @SuppressWarnings("UnusedDeclaration")
    public TouchPane(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialize(context);
    }

    private void initialize(Context context) {
        Context applicationContext = context.getApplicationContext();
        Injector injector = AirCastingApplication.class.cast(applicationContext).getInjector();
        injector.injectMembers(this);

        gestureDetector.setOnDoubleTapListener(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        eventBus.post(event);
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        eventBus.post(new TapEvent(event.getX(), event.getY()));
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        eventBus.post(new DoubleTapEvent());
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        return true;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent event) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent event, MotionEvent event1, float distanceX, float distanceY) {
        eventBus.post(new ScrollEvent(event, event1, distanceX, distanceY));
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        eventBus.post(new LongClickEvent(event));
    }

    @Override
    public boolean onFling(MotionEvent event, MotionEvent event1, float distanceX, float distanceY) {
        return true;
    }
}
