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
package pl.llp.aircasting.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import pl.llp.aircasting.R;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/14/11
 * Time: 1:49 PM
 */
public abstract class SimpleProgressTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    ProgressDialog dialog;
    private Context context;

    public SimpleProgressTask(Context context){
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(context);

        String message = context.getString(R.string.working);
        dialog.setMessage(message);
        dialog.setCancelable(false);

        dialog.show();
    }

    @Override
    protected void onPostExecute(Result result) {
        dialog.dismiss();
    }
}
