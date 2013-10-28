package pl.llp.aircasting.helper;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;

/**
 * Created with IntelliJ IDEA.
 * User: kustosz
 * Date: 10/28/13
 * Time: 9:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class BitmapViewHelper {

    private BitmapDrawable mView;
    private BitmapDrawable mViewHalfSize;
    private Rect mViewOriginalBounds;
    private Rect mViewCurrentBounds;
    private Rect mViewHalfOriginalBounds;
    private Rect mViewHalfCurrentBounds;

    public BitmapViewHelper(Resources res, View view, int x, int y) {
        int w = view.getWidth(),
            h = view.getHeight();

        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        view.draw(c);
        Bitmap hb = Bitmap.createScaledBitmap(b, w / 2, h / 2, false);

        mView = new BitmapDrawable(res, b);
        mViewHalfSize = new BitmapDrawable(res, hb);

        mViewOriginalBounds = new Rect(x - w / 2, y - h / 2, x + w / 2, y + h / 2);
        mViewCurrentBounds = new Rect(mViewOriginalBounds);

        mViewHalfOriginalBounds = new Rect(x - w / 4, y - h / 4, x + w / 4, y + h / 4);
        mViewHalfCurrentBounds = new Rect(mViewHalfOriginalBounds);

        mView.setBounds(mViewCurrentBounds);
        mViewHalfSize.setBounds(mViewHalfCurrentBounds);
    }

    public void draw(Canvas canvas, boolean halfSize) {
        if (halfSize) {
            mViewHalfSize.draw(canvas);
        } else {
            mView.draw(canvas);
        }
    }

    public void move(int deltaX, int deltaY) {
        mViewCurrentBounds.offsetTo(mViewOriginalBounds.left + deltaX, mViewOriginalBounds.top + deltaY);
        mViewHalfCurrentBounds.offsetTo(mViewHalfOriginalBounds.left + deltaX, mViewHalfOriginalBounds.top + deltaY);
        mView.setBounds(mViewCurrentBounds);
        mViewHalfSize.setBounds(mViewHalfCurrentBounds);
    }
}
