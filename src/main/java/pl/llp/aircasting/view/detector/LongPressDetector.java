package pl.llp.aircasting.view.detector;

import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 30/10/13
 * Time: 11:04
 * To change this template use File | Settings | File Templates.
 */
public class LongPressDetector {

    private OnLongPressListener listener;
    private MotionEvent initialEvent;
    private float downX;
    private float downY;
    private final Handler handler = new Handler();
    public static final long LONG_PRESS_TIMEOUT = 300;
    public static final double LONG_PRESS_RADIUS = 20;

    private Runnable notifyListener = new Runnable() {
        @Override
        public void run() {
            listener.onLongPress(initialEvent);
        }
    };

    private double distanceFromOrigin(MotionEvent event) {
        return Math.sqrt(Math.pow(downX - event.getX(), 2) + Math.pow(downY - event.getY(), 2));
    }

    public LongPressDetector(OnLongPressListener listener) {
        this.listener = listener;
    }

    public void onTouchEvent(MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                initialEvent = event;
                downX = event.getX();
                downY = event.getY();
                handler.postDelayed(notifyListener, LONG_PRESS_TIMEOUT);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                handler.removeCallbacks(notifyListener);
                break;
            case MotionEvent.ACTION_MOVE:
                if (distanceFromOrigin(event) > LONG_PRESS_RADIUS) {
                    handler.removeCallbacks(notifyListener);
                }
                break;
        }
    }

    public static interface OnLongPressListener {
        public void onLongPress(MotionEvent event);
    }
}
