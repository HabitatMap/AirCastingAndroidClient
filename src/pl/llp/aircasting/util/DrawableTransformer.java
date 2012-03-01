package pl.llp.aircasting.util;

import android.graphics.Point;
import android.graphics.drawable.Drawable;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 2/28/12
 * Time: 3:50 PM
 */
public final class DrawableTransformer {
    public static void centerAt(Drawable drawable, Point point) {
        drawable.setBounds(
                point.x - drawable.getIntrinsicWidth() / 2,
                point.y - drawable.getIntrinsicHeight() / 2,
                point.x + drawable.getIntrinsicWidth() / 2,
                point.y + drawable.getIntrinsicHeight() / 2
        );
    }

    public static void centerBottomAt(Drawable drawable, Point point) {
        drawable.setBounds(
                point.x - drawable.getIntrinsicWidth() / 2,
                point.y - drawable.getIntrinsicHeight(),
                point.x + drawable.getIntrinsicWidth() / 2,
                point.y
        );
    }
}
