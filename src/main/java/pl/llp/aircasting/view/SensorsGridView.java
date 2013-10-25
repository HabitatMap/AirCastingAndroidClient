package pl.llp.aircasting.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
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
    private BitmapDrawable mHoverView;
    private View mCurrentItemView;
    private boolean mDragEnabled = false;
    List<View> specialAreas;

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

    private void init() {
        specialAreas = new ArrayList<View>();
        reset();
    }

    public void addSpecialArea(View view) {
        specialAreas.add(view);
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
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mDownX = (int) event.getX();
                mDownY = (int) event.getY();

                int position = pointToPosition(mDownX, mDownY);
                int num = position - getFirstVisiblePosition();

                mCurrentItemView = getChildAt(num);
                setHoverView(mCurrentItemView);

                if (!isDragEnabled()) {
                    return super.dispatchTouchEvent(event);
                }

                if (mCurrentItemView == null) {
                    return false;
                }

                mCurrentItemView.setVisibility(GONE);

                break;
            case MotionEvent.ACTION_MOVE:
                if (!isDragEnabled()) {
                    return super.dispatchTouchEvent(event);
                }

                int eventX = (int) event.getX(),
                    eventY = (int) event.getY(),
                    deltaX = eventX - mDownX,
                    deltaY = eventY - mDownY;

                if (mHoverView != null) {
                    mCurrentItemView.setVisibility(GONE);
                    mCellCurrentBounds.offsetTo(mCellOriginalBounds.left + deltaX, mCellOriginalBounds.top + deltaY);
                    mHoverView.setBounds(mCellCurrentBounds);
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                disableDrag();
                reset();
                break;
        }
        return false;
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        for (View view : specialAreas) {
            view.draw(canvas);
        }
        if (isDragEnabled() && mHoverView != null) {
            mHoverView.draw(canvas);
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
    }

    private void setHoverView(View view) {
        int w = view.getWidth(),
                h = view.getHeight(),
                top = view.getTop(),
                left = view.getLeft();

        Bitmap b = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        view.draw(c);

        BitmapDrawable drawable = new BitmapDrawable(getResources(), b);

        mCellOriginalBounds = new Rect(left, top, left + w, top + h);
        mCellCurrentBounds = new Rect(mCellOriginalBounds);

        drawable.setBounds(mCellCurrentBounds);

        mHoverView = drawable;
    }
}