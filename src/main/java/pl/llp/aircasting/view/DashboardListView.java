/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.llp.aircasting.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import pl.llp.aircasting.activity.adapter.StreamAdapter;
import pl.llp.aircasting.model.MeasurementStream;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * The dynamic listview is an extension of listview that supports cell dragging
 * and swapping.
 *
 * This layout is in charge of positioning the hover cell in the correct location
 * on the screen in response to user touch events. It uses the position of the
 * hover cell to determine when two cells should be swapped. If two cells should
 * be swapped, all the corresponding data set and layout changes are handled here.
 *
 * If no cell is selected, all the touch events are passed down to the listview
 * and behave normally. If one of the items in the listview experiences a
 * long press event, the contents of its current visible state are captured as
 * a bitmap and its visibility is set to INVISIBLE. A hover cell is then created and
 * added to this layout as an overlaying BitmapDrawable above the listview. Once the
 * hover cell is translated some distance to signify an item swap, a data set change
 * accompanied by animation takes place. When the user releases the hover cell,
 * it animates into its corresponding position in the listview.
 *
 * When the hover cell is either above or below the bounds of the listview, this
 * listview also scrolls on its own so as to reveal additional content.
 */
public class DashboardListView extends ListView {

    private final int SMOOTH_SCROLL_AMOUNT_AT_EDGE = 15;
    private final int MOVE_DURATION = 150;
    private final int LINE_THICKNESS = 10;

    private int lastEventY = -1;
    private int lastEventX = -1;

    private int downY = -1;
    private int downX = -1;

    private int totalOffsetY = 0;
    private int totalOffsetX = 0;

    private boolean cellIsMobile = false;
    private boolean isMobileScrolling = false;
    private boolean swipeInProgress = false;
    private boolean swipeRightInProgress = false;
    private int smoothScrollAmountAtEdge = 0;

    private final int INVALID_ID = -1;
    private long aboveItemId = INVALID_ID;
    private long mobileItemId = INVALID_ID;
    private long belowItemId = INVALID_ID;

    private BitmapDrawable hoverCell;
    private Rect hoverCellCurrentBounds;
    private Rect hoverCellOriginalBounds;

    private final int INVALID_POINTER_ID = -1;
    private int activePointerId = INVALID_POINTER_ID;

    private boolean isWaitingForScrollFinish = false;
    private int scrollState = OnScrollListener.SCROLL_STATE_IDLE;
    private Context context;

    public DashboardListView(Context context) {
        super(context);
        init(context);
    }

    public DashboardListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public DashboardListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        this.context = context;
        setOnItemLongClickListener(onItemLongClickListener);

        setOnScrollListener(scrollListener);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        smoothScrollAmountAtEdge = (int)(SMOOTH_SCROLL_AMOUNT_AT_EDGE / metrics.density);
    }

    /**
     * Listens for long clicks on any items in the listview. When a cell has
     * been selected, the hover cell is created and set up.
     */
    public AdapterView.OnItemLongClickListener onItemLongClickListener =
            new AdapterView.OnItemLongClickListener() {
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long id) {
                    getStreamAdapter().startReorder();
                    Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
                    vibrator.vibrate(50);

                    totalOffsetY = 0;
                    totalOffsetX = 0;

                    int position = pointToPosition(downX, downY);
                    int itemNum = position - getFirstVisiblePosition();

                    View selectedView = getChildAt(itemNum);
                    mobileItemId = getStreamAdapter().getItemId(position);
                    hoverCell = getAndAddHoverView(selectedView);
                    selectedView.setVisibility(INVISIBLE);

                    cellIsMobile = true;

                    updateNeighborViewsForID(mobileItemId);

                    return true;
                }
            };

    /**
     * Creates the hover cell with the appropriate bitmap and of appropriate
     * size. The hover cell's BitmapDrawable is drawn on top of the bitmap every
     * single time an invalidate call is made.
     */
    private BitmapDrawable getAndAddHoverView(View view) {
        int w = view.getWidth();
        int h = view.getHeight();
        int top = view.getTop();
        int left = view.getLeft();

        Bitmap b = getBitmapWithBorder(view);

        BitmapDrawable drawable = new BitmapDrawable(getResources(), b);

        hoverCellOriginalBounds = new Rect(left, top, left + w, top + h);
        hoverCellCurrentBounds = new Rect(hoverCellOriginalBounds);

        drawable.setBounds(hoverCellCurrentBounds);

        return drawable;
    }

    /** Draws a black border over the screenshot of the view passed in. */
    private Bitmap getBitmapWithBorder(View v) {
        Bitmap bitmap = getBitmapFromView(v);
        Canvas can = new Canvas(bitmap);

        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(LINE_THICKNESS);
        paint.setColor(Color.BLUE);

        can.drawBitmap(bitmap, 0, 0, null);
        can.drawRect(rect, paint);

        return bitmap;
    }

    /** Returns a bitmap showing a screenshot of the view passed in. */
    private Bitmap getBitmapFromView(View v) {
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas (bitmap);
        v.draw(canvas);
        return bitmap;
    }

    /**
     * Stores a reference to the views above and below the item currently
     * corresponding to the hover cell. It is important to note that if this
     * item is either at the top or bottom of the list, aboveItemId or belowItemId
     * may be invalid.
     */
    private void updateNeighborViewsForID(long itemID) {
        int position = getPositionForID(itemID);
        aboveItemId = getStreamAdapter().getItemId(position - 1);
        belowItemId = getStreamAdapter().getItemId(position + 1);
    }

    /** Retrieves the view in the list corresponding to itemID */
    public View getViewForID (long itemID) {
        int firstVisiblePosition = getFirstVisiblePosition();
        for(int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            int position = firstVisiblePosition + i;
            long id = getStreamAdapter().getItemId(position);
            if (id == itemID) {
                return v;
            }
        }
        return null;
    }

    /** Retrieves the position in the list corresponding to itemID */
    public int getPositionForID (long itemID) {
        View v = getViewForID(itemID);
        if (v == null) {
            return -1;
        } else {
            return getPositionForView(v);
        }
    }

    /**
     *  dispatchDraw gets invoked when all the child views are about to be drawn.
     *  By overriding this method, the hover cell (BitmapDrawable) can be drawn
     *  over the listview's items whenever the listview is redrawn.
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (hoverCell != null) {
            hoverCell.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent (MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                downX = (int)event.getX();
                downY = (int)event.getY();
                activePointerId = event.getPointerId(0);
                break;
            case MotionEvent.ACTION_MOVE:
                if (activePointerId == INVALID_POINTER_ID) {
                    break;
                }

                int pointerIndex = event.findPointerIndex(activePointerId);

                lastEventX = (int) event.getX(pointerIndex);
                lastEventY = (int) event.getY(pointerIndex);

                int deltaY = lastEventY - downY;
                int deltaX = lastEventX - downX;

                if (cellIsMobile) {
                    hoverCellCurrentBounds.offsetTo(hoverCellOriginalBounds.left + deltaX,
                            hoverCellOriginalBounds.top + deltaY + totalOffsetY);
                    hoverCell.setBounds(hoverCellCurrentBounds);

                    handleCellSwipe();
                    handleCellSwitch();

                    invalidate();
                    isMobileScrolling = false;
                    handleMobileCellScroll();

                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
                touchEventsEnded();
                getStreamAdapter().stopReorder();
                break;
            case MotionEvent.ACTION_CANCEL:
                touchEventsCancelled();
                getStreamAdapter().stopReorder();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                /* If a multitouch event took place and the original touch dictating
                 * the movement of the hover cell has ended, then the dragging event
                 * ends and the hover cell is animated to its corresponding position
                 * in the listview. */
                pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                        MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == activePointerId) {
                    touchEventsEnded();
                }
                getStreamAdapter().stopReorder();
                break;
            default:
                break;
        }

        return super.onTouchEvent(event);
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB_MR1)
    private void handleCellSwipe() {
        if (!swipeInProgress) {
            long deltaX = lastEventX - downX;
            long deltaXTotal = hoverCellOriginalBounds.left + totalOffsetX + deltaX;

            /**
             * We only want this to work after the cell has been moved by a
             * certain distance.
             */
            boolean swipedRight = deltaXTotal > (hoverCellOriginalBounds.left + hoverCell.getIntrinsicWidth() / 2);
            boolean swipedLeft = deltaXTotal < (hoverCellOriginalBounds.left - hoverCell.getIntrinsicWidth() / 2);
            
            final View mobileView = getViewForID(mobileItemId);

            Rect offScreenBounds = getOffScreenBounds(swipedLeft);
            ObjectAnimator hoverViewAnimator = ObjectAnimator.ofObject(hoverCell, "bounds",
                    sBoundEvaluator, offScreenBounds);

            if (swipedLeft) {
                swipeInProgress = true;
                getStreamAdapter().deleteStream(mobileView);
                mobileView.setVisibility(GONE);
                hoverViewAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        touchEventsEnded();
                    }
                });
                hoverViewAnimator.setDuration(MOVE_DURATION * 4);
                hoverViewAnimator.start();
            } else if (swipedRight) {
                swipeInProgress = true;
                swipeRightInProgress = true;

                mobileView.setVisibility(GONE);

                hoverViewAnimator.addListener(new AnimatorListenerAdapter() {
                    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB_MR1)
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        getStreamAdapter().clearStream(getPositionForView(mobileView));
                        touchEventsEnded();
                        mobileView.setVisibility(VISIBLE);
                    }
                });
                hoverViewAnimator.setDuration(MOVE_DURATION * 4);
                hoverViewAnimator.start();
            }
        }
    }

    private Rect getOffScreenBounds(boolean swipedLeft) {
        int topBound = hoverCellCurrentBounds.top;
        int bottomBound = hoverCellCurrentBounds.bottom;
        int leftBound = swipedLeft ? hoverCellCurrentBounds.left - hoverCellCurrentBounds.right : hoverCellCurrentBounds.right;
        int rightBound = swipedLeft ? hoverCellCurrentBounds.left : hoverCellCurrentBounds.left + hoverCellCurrentBounds.right;

        return new Rect(leftBound, topBound, rightBound, bottomBound);
    }

    /**
     * This method determines whether the hover cell has been shifted far enough
     * to invoke a cell swap. If so, then the respective cell swap candidate is
     * determined and the data set is changed. Upon posting a notification of the
     * data set change, a layout is invoked to place the cells in the right place.
     * Using a ViewTreeObserver and a corresponding OnPreDrawListener, we can
     * offset the cell being swapped to where it previously was and then animate it to
     * its new position.
     */
    private void handleCellSwitch() {
        if (swipeInProgress) {
            return;
        }

        final int deltaY = lastEventY - downY;
        final int deltaX = lastEventX - downX;
        int deltaYTotal = hoverCellOriginalBounds.top + totalOffsetY + deltaY;

        View belowView = getViewForID(belowItemId);
        View mobileView = getViewForID(mobileItemId);
        View aboveView = getViewForID(aboveItemId);

        boolean isBelow = (belowView != null) && (deltaYTotal > belowView.getTop());
        boolean isAbove = (aboveView != null) && (deltaYTotal < aboveView.getTop());

        if (isBelow || isAbove) {
            final long switchItemID = isBelow ? belowItemId : aboveItemId;
            View switchView = isBelow ? belowView : aboveView;
            final int originalItem = getPositionForView(mobileView);

            if (switchView == null) {
                updateNeighborViewsForID(mobileItemId);
                return;
            }

            swapElements(originalItem, getPositionForView(switchView));

            mobileItemId = getPositionForView(switchView);

            downY = lastEventY;

            final int switchViewStartTop = switchView.getTop();

            mobileView.setVisibility(View.VISIBLE);
            switchView.setVisibility(View.INVISIBLE);

            updateNeighborViewsForID(mobileItemId);

            final ViewTreeObserver observer = getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    observer.removeOnPreDrawListener(this);

                    View switchView = getViewForID(switchItemID);

                    totalOffsetY += deltaY;
                    totalOffsetX += deltaX;

                    int switchViewNewTop = switchView.getTop();
                    int delta = switchViewStartTop - switchViewNewTop;

                    switchView.setTranslationY(delta);

                    ObjectAnimator animator = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        animator = ObjectAnimator.ofFloat(switchView,
                                View.TRANSLATION_Y, 0);
                    }
                    animator.setDuration(MOVE_DURATION);
                    animator.start();

                    return true;
                }
            });
        }
    }

    private void swapElements(int indexOne, int indexTwo) {
        getStreamAdapter().swapPositions(indexOne, indexTwo);
    }

    /**
     * Resets all the appropriate fields to a default state while also animating
     * the hover cell back to its correct location.
     */
    private void touchEventsEnded () {
        final View mobileView = getViewForID(mobileItemId);
        if (!swipeInProgress && (cellIsMobile || isWaitingForScrollFinish)) {
            cellIsMobile = false;
            isWaitingForScrollFinish = false;
            isMobileScrolling = false;
            swipeInProgress = false;
            activePointerId = INVALID_POINTER_ID;

            // If the autoscroller has not completed scrolling, we need to wait for it to
            // finish in order to determine the final location of where the hover cell
            // should be animated to.
            if (scrollState != OnScrollListener.SCROLL_STATE_IDLE) {
                isWaitingForScrollFinish = true;
                return;
            }

            if (mobileView != null) {
                hoverCellCurrentBounds.offsetTo(hoverCellOriginalBounds.left, mobileView.getTop());
            }

            ObjectAnimator hoverViewAnimator = ObjectAnimator.ofObject(hoverCell, "bounds",
                    sBoundEvaluator, hoverCellCurrentBounds);
            hoverViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    invalidate();
                }
            });
            hoverViewAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setEnabled(false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    aboveItemId = INVALID_ID;
                    mobileItemId = INVALID_ID;
                    belowItemId = INVALID_ID;
                    if (mobileView != null) {
                        mobileView.setVisibility(VISIBLE);
                    }
                    hoverCell = null;
                    setEnabled(true);
                    invalidate();
                }
            });
            hoverViewAnimator.start();
        } else {
            touchEventsCancelled();
        }
    }

    /**
     * Resets all the appropriate fields to a default state.
     */
    private void touchEventsCancelled () {
        View mobileView = getViewForID(mobileItemId);
        if (cellIsMobile) {
            aboveItemId = INVALID_ID;
            mobileItemId = INVALID_ID;
            belowItemId = INVALID_ID;
            if (!swipeRightInProgress && (mobileView != null)) {
                mobileView.setVisibility(VISIBLE);
            }
            hoverCell = null;
            invalidate();
        }
        cellIsMobile = false;
        isMobileScrolling = false;
        swipeInProgress = false;
        swipeRightInProgress = false;
        activePointerId = INVALID_POINTER_ID;
    }

    /**
     * This TypeEvaluator is used to animate the BitmapDrawable back to its
     * final location when the user lifts his finger by modifying the
     * BitmapDrawable's bounds.
     */
    private final static TypeEvaluator<Rect> sBoundEvaluator = new TypeEvaluator<Rect>() {
        public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
            return new Rect(interpolate(startValue.left, endValue.left, fraction),
                    interpolate(startValue.top, endValue.top, fraction),
                    interpolate(startValue.right, endValue.right, fraction),
                    interpolate(startValue.bottom, endValue.bottom, fraction));
        }

        public int interpolate(int start, int end, float fraction) {
            return (int)(start + fraction * (end - start));
        }
    };

    /**
     *  Determines whether this listview is in a scrolling state invoked
     *  by the fact that the hover cell is out of the bounds of the listview;
     */
    private void handleMobileCellScroll() {
        isMobileScrolling = handleMobileCellScroll(hoverCellCurrentBounds);
    }

    /**
     * This method is in charge of determining if the hover cell is above
     * or below the bounds of the listview. If so, the listview does an appropriate
     * upward or downward smooth scroll so as to reveal new items.
     */
    public boolean handleMobileCellScroll(Rect r) {
        int offset = computeVerticalScrollOffset();
        int height = getHeight();
        int extent = computeVerticalScrollExtent();
        int range = computeVerticalScrollRange();
        int hoverViewTop = r.top;
        int hoverHeight = r.height();

        if (hoverViewTop <= 0 && offset > 0) {
            smoothScrollBy(-smoothScrollAmountAtEdge, 0);
            return true;
        }

        if (hoverViewTop + hoverHeight >= height && (offset + extent) < range) {
            smoothScrollBy(smoothScrollAmountAtEdge, 0);
            return true;
        }

        return false;
    }

    /**
     * This scroll listener is added to the listview in order to handle cell swapping
     * when the cell is either at the top or bottom edge of the listview. If the hover
     * cell is at either edge of the listview, the listview will begin scrolling. As
     * scrolling takes place, the listview continuously checks if new cells became visible
     * and determines whether they are potential candidates for a cell swap.
     */
    private AbsListView.OnScrollListener scrollListener = new AbsListView.OnScrollListener () {

        private int mPreviousFirstVisibleItem = -1;
        private int mPreviousVisibleItemCount = -1;
        private int mCurrentFirstVisibleItem;
        private int mCurrentVisibleItemCount;
        private int mCurrentScrollState;

        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                             int totalItemCount) {
            mCurrentFirstVisibleItem = firstVisibleItem;
            mCurrentVisibleItemCount = visibleItemCount;

            mPreviousFirstVisibleItem = (mPreviousFirstVisibleItem == -1) ? mCurrentFirstVisibleItem
                    : mPreviousFirstVisibleItem;
            mPreviousVisibleItemCount = (mPreviousVisibleItemCount == -1) ? mCurrentVisibleItemCount
                    : mPreviousVisibleItemCount;

            checkAndHandleFirstVisibleCellChange();
            checkAndHandleLastVisibleCellChange();

            mPreviousFirstVisibleItem = mCurrentFirstVisibleItem;
            mPreviousVisibleItemCount = mCurrentVisibleItemCount;
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            mCurrentScrollState = scrollState;
            scrollState = scrollState;
            isScrollCompleted();
        }

        /**
         * This method is in charge of invoking 1 of 2 actions. Firstly, if the listview
         * is in a state of scrolling invoked by the hover cell being outside the bounds
         * of the listview, then this scrolling event is continued. Secondly, if the hover
         * cell has already been released, this invokes the animation for the hover cell
         * to return to its correct position after the listview has entered an idle scroll
         * state.
         */
        private void isScrollCompleted() {
            if (mCurrentVisibleItemCount > 0 && mCurrentScrollState == SCROLL_STATE_IDLE) {
                if (cellIsMobile && isMobileScrolling) {
                    handleMobileCellScroll();
                } else if (isWaitingForScrollFinish) {
                    touchEventsEnded();
                }
            }
        }

        /**
         * Determines if the listview scrolled up enough to reveal a new cell at the
         * top of the list. If so, then the appropriate parameters are updated.
         */
        public void checkAndHandleFirstVisibleCellChange() {
            if (mCurrentFirstVisibleItem != mPreviousFirstVisibleItem) {
                if (cellIsMobile && mobileItemId != INVALID_ID) {
                    updateNeighborViewsForID(mobileItemId);
                    handleCellSwitch();
                }
            }
        }

        /**
         * Determines if the listview scrolled down enough to reveal a new cell at the
         * bottom of the list. If so, then the appropriate parameters are updated.
         */
        public void checkAndHandleLastVisibleCellChange() {
            int currentLastVisibleItem = mCurrentFirstVisibleItem + mCurrentVisibleItemCount;
            int previousLastVisibleItem = mPreviousFirstVisibleItem + mPreviousVisibleItemCount;
            if (currentLastVisibleItem != previousLastVisibleItem) {
                if (cellIsMobile && mobileItemId != INVALID_ID) {
                    updateNeighborViewsForID(mobileItemId);
                    handleCellSwitch();
                }
            }
        }
    };

    private StreamAdapter getStreamAdapter() {
        return (StreamAdapter) getAdapter();
    }
}