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

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import pl.llp.aircasting.R;
import pl.llp.aircasting.model.Session;
import pl.llp.aircasting.repository.SessionRepository;

public abstract class OpenSessionTask extends AsyncTask<Long, Void, Session> implements SessionRepository.ProgressListener {
    private ProgressDialog dialog;
    private Activity context;

    protected OpenSessionTask(Activity context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(context);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMessage(context.getString(R.string.loading));
        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    protected void onPostExecute(Session session) {
        dialog.dismiss();
    }

    @Override
    public void onSizeCalculated(int workSize) {
        dialog.setMax(workSize);
    }

    @Override
    public void onProgress(int progress) {
        dialog.setProgress(progress);
    }
}
