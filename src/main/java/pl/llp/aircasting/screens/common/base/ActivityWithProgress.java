package pl.llp.aircasting.screens.common.base;

import android.app.ProgressDialog;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 1/16/12
 * Time: 12:03 PM
 */
public interface ActivityWithProgress {
    public static final int SPINNER_DIALOG = 6355;

    public ProgressDialog showProgressDialog(int progressStyle, SimpleProgressTask task);

    public void hideProgressDialog();
}
