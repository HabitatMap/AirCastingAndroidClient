package pl.llp.aircasting.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import pl.llp.aircasting.activity.task.SimpleProgressTask;
import roboguice.activity.RoboListActivity;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 1/17/12
 * Time: 3:19 PM
 */
public class RoboListActivityWithProgress extends RoboListActivity implements ActivityWithProgress {
    private int progressStyle;
    private ProgressDialog dialog;
    private SimpleProgressTask task;

    @Override
    public ProgressDialog showProgressDialog(int progressStyle, SimpleProgressTask task) {
        this.progressStyle = progressStyle;
        this.task = task;

        showDialog(SPINNER_DIALOG);
        return dialog;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        this.dialog = SimpleProgressTask.prepareDialog(this, progressStyle);

        return dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Object instance = getLastNonConfigurationInstance();
        if (instance != null) {
            ((SimpleProgressTask) instance).setActivity(this);
        }
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return task;
    }

    @Override
    public void hideProgressDialog() {
        dismissDialog(SPINNER_DIALOG);
        removeDialog(SPINNER_DIALOG);
    }
}
