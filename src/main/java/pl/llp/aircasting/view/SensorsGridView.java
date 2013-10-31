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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import com.google.inject.Inject;
import pl.llp.aircasting.R;
import pl.llp.aircasting.helper.BitmapViewHelper;
import pl.llp.aircasting.helper.ResourceHelper;

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
    private BitmapViewHelper mBitmapViewHelper;
    private View mCurrentItemView;
    private boolean mDragEnabled = false;
    private OnItemDoubleClickListener onItemDoubleClickListener;
    private OnItemSingleTapListener onItemSingleTapListener;
    List<ListenArea> listenAreas;

    public SensorsGridView(Context context) {
        super(context);
        init();
    }

    public SensorsGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SensorsGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public boolean isInListenArea() {
        for (ListenArea listenArea : listenAreas) {
            if (listenArea.isEntered()) {
                return true;
            }
        }
        return false;
    }

    private void init() {
        listenAreas = new ArrayList<ListenArea>();

        onItemDoubleClickListener = new OnItemDoubleClickListener() {
            @Override
            public void onItemDoubleClick(AdapterView<?> parent, View view, int position, long id) {
            }
        };

        onItemSingleTapListener = new OnItemSingleTapListener() {
            @Override
            public void onItemSingleTap(AdapterView<?> parent, View view, int position, long id) {
            }
        };

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

    public void registerListenArea(ViewGroup rootView, int id, OnDragListener listener) {
        listenAreas.add(new ListenArea(rootView, id, listener));
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

    public void setOnItemDoubleClickListener(OnItemDoubleClickListener listener) {
        onItemDoubleClickListener = listener;
    }

    public boolean dispatchDoubleClickEvent(MotionEvent event) {
        int position = pointToPosition((int) event.getX(), (int) event.getY());
        View itemView = getChildAt(position - getFirstVisiblePosition());
        if (itemView == null) {
            return false;
        }
        onItemDoubleClickListener.onItemDoubleClick(this, itemView, position, getAdapter().getItemId(position));
        return true;
    }

    public void setOnItemSingleTapListener(OnItemSingleTapListener listener) {
        onItemSingleTapListener = listener;
    }

    public boolean dispatchSingleTapEvent(MotionEvent event) {
        int position = pointToPosition((int) event.getX(), (int) event.getY());
        View itemView = getChildAt(position - getFirstVisiblePosition());
        if (itemView == null) {
            return false;
        }
        onItemSingleTapListener.onItemSingleTap(this, itemView, position, getAdapter().getItemId(position));
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
                mBitmapViewHelper = new BitmapViewHelper(getResources(), mCurrentItemView, mDownX, mDownY);

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

                if (mBitmapViewHelper != null) {
                    mCurrentItemView.setVisibility(GONE);
                    mBitmapViewHelper.move(deltaX, deltaY);
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
        if (isDragEnabled() && mBitmapViewHelper != null) {
            mBitmapViewHelper.draw(canvas, isInListenArea());
        }
    }

    private void reset() {
        if (mCurrentItemView != null) {
            mCurrentItemView.setVisibility(VISIBLE);
        }

        mCurrentItemView = null;
        mBitmapViewHelper = null;
    }

    public static abstract class OnDragListener {
        public void onEnter(View view) {
        }

        public void onLeave(View view) {
        }

        public void onDrop(View view) {
        }
    }

    public static class ListenArea {
        private ViewGroup parentView;
        private int viewId;
        private View view;
        private OnDragListener listener;
        private boolean entered;

        public ListenArea(ViewGroup parentView, int viewId, OnDragListener listener) {
            entered = false;
            this.parentView = parentView;
            this.viewId = viewId;
            this.listener = listener;
        }

        private void findView() {
            view = parentView.findViewById(viewId);
        }

        private boolean isPointInside(int x, int y) {
            return (x >= view.getLeft() && x <= view.getLeft() + view.getWidth() &&
                    y >= view.getTop() && y <= view.getTop() + view.getHeight());
        }

        public boolean isEntered() {
            return entered;
        }

        public void onMove(int x, int y, View view) {
            findView();
            if (isPointInside(x, y) && !entered) {
                entered = true;
                listener.onEnter(view);
            } else if (!isPointInside(x, y) && entered) {
                entered = false;
                listener.onLeave(view);
            }
        }

        public void onDrop(int x, int y, View view) {
            findView();
            entered = false;
            if (isPointInside(x, y)) {
                listener.onLeave(view);
                listener.onDrop(view);
            }
        }

        public void onMotionEvent(MotionEvent event) {
            findView();
            if (isPointInside((int) event.getX(), (int) event.getY())) {
                MotionEvent moved = MotionEvent.obtain(event);
                moved.offsetLocation(-view.getLeft(), -view.getTop());
                view.dispatchTouchEvent(moved);
            }
        }
    }

    public static abstract class OnItemDoubleClickListener {
        public abstract void onItemDoubleClick(AdapterView<?> parent, View view, int position, long id);
    }

    public static abstract class OnItemSingleTapListener {
        public abstract void onItemSingleTap(AdapterView<?> parent, View view, int position, long id);
    }
}