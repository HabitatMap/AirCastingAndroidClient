package pl.llp.aircasting.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import pl.llp.aircasting.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 24/10/13
 * Time: 14:57
 * To change this template use File | Settings | File Templates.
 */
public class SensorsGridView extends GridView {

    private int mDownX;
    private int mDownY;
    private Rect mCellOriginalBounds;
    private Rect mCellCurrentBounds;
    private Rect mCellHalfOriginalBounds;
    private Rect mCellHalfCurrentBounds;
    private Drawable mHoverView;
    private Drawable mHoverViewHalfSize;
    private View mCurrentItemView;
    private boolean mDisplayHalfSize = false;
    private boolean mDragEnabled = false;
    List<ListenArea> listenAreas;

    public SensorsGridView(Context context) {
        super(context);
        init();
    }

    public void displayFullSize() {
        mDisplayHalfSize = false;
    }

    public void displayHalfSize() {
        mDisplayHalfSize = true;
    }

    public void toggleDisplaySize() {
        mDisplayHalfSize = !mDisplayHalfSize;
    }

    public SensorsGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SensorsGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        listenAreas = new ArrayList<ListenArea>();
        reset();
    }

    private void notifyMove(int x, int y) {
        for (ListenArea listenArea : listenAreas) {
            listenArea.onMove(x, y, mCurrentItemView);
        }
    }

    private void notifyDrop(int x, int y) {
        for (ListenArea listenArea : listenAreas) {
            listenArea.onDrop(x, y, mCurrentItemView);
        }
    }

    private void notifyMotionEvent(MotionEvent event) {
        for (ListenArea listenArea : listenAreas) {
            listenArea.onMotionEvent(event);
        }
    }

    public void registerListenArea(View view, OnDragListener listener) {
        listenAreas.add(new ListenArea(view, listener));
    }

    public boolean isDragEnabled() {
        return mDragEnabled;
    }

    public void enableDrag() {
        mDragEnabled = true;
    }

    public void disableDrag() {
        mDragEnabled = false;
    }

    public boolean dispatchLongPressEvent(MotionEvent event) {
        int position = pointToPosition((int) event.getX(), (int) event.getY());
        View itemView = getChildAt(position - getFirstVisiblePosition());
        if (itemView == null) {
            return false;
        }
        getOnItemLongClickListener().onItemLongClick(this, itemView, position, getAdapter().getItemId(position));
        if (isDragEnabled() && mCurrentItemView != null) {
            mCurrentItemView.setVisibility(GONE);
        }
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                reset();
                mDownX = (int) event.getX();
                mDownY = (int) event.getY();

                int position = pointToPosition(mDownX, mDownY);

                if (position == INVALID_POSITION) {
                    notifyMotionEvent(event);
                    return super.dispatchTouchEvent(event);
                }

                int num = position - getFirstVisiblePosition();

                mCurrentItemView = getChildAt(num);
                setHoverView(mCurrentItemView);

                if (!isDragEnabled()) {
                    notifyMotionEvent(event);
                    return super.dispatchTouchEvent(event);
                }

                mCurrentItemView.setVisibility(GONE);

                break;
            case MotionEvent.ACTION_MOVE:

                if (!isDragEnabled()) {
                    notifyMotionEvent(event);
                    return super.dispatchTouchEvent(event);
                }

                int eventX = (int) event.getX(),
                    eventY = (int) event.getY(),
                    deltaX = eventX - mDownX,
                    deltaY = eventY - mDownY;

                if (mHoverView != null) {
                    mCurrentItemView.setVisibility(GONE);
                    mCellCurrentBounds.offsetTo(mCellOriginalBounds.left + deltaX, mCellOriginalBounds.top + deltaY);
                    mCellHalfCurrentBounds.offsetTo(mCellHalfOriginalBounds.left + deltaX, mCellHalfOriginalBounds.top + deltaY);
                    mHoverView.setBounds(mCellCurrentBounds);
                    mHoverViewHalfSize.setBounds(mCellHalfCurrentBounds);
                    notifyMove(eventX, eventY);
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (!isDragEnabled()) {
                    notifyMotionEvent(event);
                    return super.dispatchTouchEvent(event);
                }

                notifyDrop((int) event.getX(), (int) event.getY());
                disableDrag();
                reset();
                break;
        }
        return false;
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (isDragEnabled() && mHoverView != null) {
            Log.d("TADAM", String.valueOf(mDisplayHalfSize));
            if (mDisplayHalfSize) {
                mHoverViewHalfSize.draw(canvas);
            } else {
                mHoverView.draw(canvas);
            }
        }
    }

    private void reset() {
        if (mCurrentItemView != null) {
            mCurrentItemView.setVisibility(VISIBLE);
        }

        mCurrentItemView = null;
        mHoverView = null;
        mCellCurrentBounds = null;
        mCellOriginalBounds = null;
        mDisplayHalfSize = false;
    }

    private void setHoverView(View view) {
        int w = view.getWidth(),
            h = view.getHeight();

        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        view.draw(c);
        Bitmap hb = Bitmap.createScaledBitmap(b, w / 2, h / 2, false);

        mHoverView = new BitmapDrawable(getResources(), b);
        mHoverViewHalfSize = new BitmapDrawable(getResources(), hb);

        mCellOriginalBounds = new Rect(mDownX - w / 2, mDownY - h / 2, mDownX + w / 2, mDownY + h / 2);
        mCellCurrentBounds = new Rect(mCellOriginalBounds);

        mCellHalfOriginalBounds = new Rect(mDownX - w / 4, mDownY - h / 4, mDownX + w / 4, mDownY + h / 4);
        mCellHalfCurrentBounds = new Rect(mCellHalfOriginalBounds);

        mHoverView.setBounds(mCellCurrentBounds);
        mHoverViewHalfSize.setBounds(mCellHalfCurrentBounds);
    }

    public static abstract class OnDragListener {
        public abstract void onEnter(View view);

        public abstract void onLeave(View view);

        public abstract void onDrop(View view);
    }

    public static class ListenArea {
        private View view;
        private OnDragListener listener;
        private boolean entered;

        public ListenArea(View view, OnDragListener listener) {
            entered = false;
            this.view = view;
            this.listener = listener;
        }

        private boolean isPointInside(int x, int y) {
            return (x >= view.getLeft() && x <= view.getLeft() + view.getWidth() &&
                    y >= view.getTop() && y <= view.getTop() + view.getHeight());
        }

        public void onMove(int x, int y, View view) {
            if (isPointInside(x, y) && !entered) {
                entered = true;
                listener.onEnter(view);
            } else if (!isPointInside(x, y) && entered) {
                entered = false;
                listener.onLeave(view);
            }
        }

        public void onDrop(int x, int y, View view) {
            listener.onLeave(view);
            if (isPointInside(x, y)) {
                listener.onDrop(view);
            }
        }

        public void onMotionEvent(MotionEvent event) {
            if (isPointInside((int) event.getX(), (int) event.getY())) {
                MotionEvent moved = MotionEvent.obtain(event);
                moved.offsetLocation(-view.getLeft(), -view.getTop());
                view.dispatchTouchEvent(moved);
            }
        }
    }
}