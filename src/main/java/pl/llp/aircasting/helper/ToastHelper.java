package pl.llp.aircasting.helper;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by radek on 20/02/18.
 */
public class ToastHelper {
    private static Toast toast;

    public static void show(Context context, int text, int duration) {
        toast = Toast.makeText(context, text, duration);
        showAtMiddleOfScreen();
    }

    public static void showText(Context context, String text, int duration) {
        toast = Toast.makeText(context, text, duration);
        showAtMiddleOfScreen();
    }

    private static void showAtMiddleOfScreen() {
        toast.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }
}
