package pl.llp.aircasting.event.ui;

import android.view.MotionEvent;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 25/10/13
 * Time: 12:44
 * To change this template use File | Settings | File Templates.
 */
public class LongClickEvent {

    private MotionEvent event;

    public LongClickEvent(MotionEvent event) {
        super();
        this.event = event;
    }

    public MotionEvent getMotionEvent() {
        return event;
    }
}
