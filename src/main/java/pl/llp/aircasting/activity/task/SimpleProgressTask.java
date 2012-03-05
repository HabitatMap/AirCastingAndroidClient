/**
 AirCasting - Share your Air!
 Copyright (C) 2011-2012 HabitatMap, Inc.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 You can contact the authors by email at <info@habitatmap.org>
 */
package pl.llp.aircasting.activity.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.ActivityWithProgress;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/14/11
 * Time: 1:49 PM
 */
public abstract class SimpleProgressTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    protected ProgressDialog dialog;
    private int progressStyle;

    public static ProgressDialog prepareDialog(Context context, int progressStyle) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.working));
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(progressStyle);

        return progressDialog;
    }

    private ActivityWithProgress context;

    public SimpleProgressTask(ActivityWithProgress context) {
        this.context = context;
        this.progressStyle = ProgressDialog.STYLE_SPINNER;
    }

    public SimpleProgressTask(ActivityWithProgress context, int progressStyle) {
        this.context = context;
        this.progressStyle = progressStyle;
    }

    @Override
    protected void onPreExecute() {
        this.dialog = context.showProgressDialog(progressStyle, this);
    }

    @Override
    protected void onPostExecute(Result result) {
        context.hideProgressDialog();
    }

    public void setActivity(ActivityWithProgress activity) {
        this.context = activity;
    }
}
