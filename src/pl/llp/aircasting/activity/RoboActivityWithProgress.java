package pl.llp.aircasting.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import pl.llp.aircasting.activity.task.SimpleProgressTask;
import roboguice.activity.RoboActivity;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 1/16/12
 * Time: 12:02 PM
 */
public abstract class RoboActivityWithProgress extends RoboActivity implements ActivityWithProgress {
    private int progressStyle;
    private ProgressDialog dialog;

    @Override
    public ProgressDialog showProgressDialog(int progressStyle) {
        this.progressStyle = progressStyle;

        showDialog(SPINNER_DIALOG);
        return dialog;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        this.dialog = SimpleProgressTask.prepareDialog(this, progressStyle);

        return dialog;
    }

    @Override
    public void hideProgressDialog() {
        removeDialog(SPINNER_DIALOG);
    }}
