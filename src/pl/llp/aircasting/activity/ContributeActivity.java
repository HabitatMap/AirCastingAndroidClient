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
package pl.llp.aircasting.activity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.google.inject.Inject;
import pl.llp.aircasting.R;
import pl.llp.aircasting.model.SessionManager;
import pl.llp.aircasting.repository.SessionRepository;
import roboguice.inject.InjectView;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/30/11
 * Time: 12:40 PM
 */
public class ContributeActivity extends DialogActivity implements View.OnClickListener {
    @Inject SessionManager sessionManager;

    @InjectView(R.id.yes) Button yes;
    @InjectView(R.id.no) Button no;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.contribute);

        yes.setOnClickListener(this);
        no.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        boolean contribute = view.getId() == R.id.yes;
        sessionManager.setContribute(contribute);

        saveSession();
    }

    private void saveSession() {
        //noinspection unchecked
        new SaveSessionTask().execute();
    }

    private class SaveSessionTask extends AsyncTask<Void, Void, Void> implements SessionRepository.ProgressListener {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(ContributeActivity.this);
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setMessage(getString(R.string.saving));
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            sessionManager.finishSession(this);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dialog.dismiss();
            finish();
        }

        @Override
        public void onSizeCalculated(final int workSize) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.setMax(workSize);
                }
            });
        }

        @Override
        public void onProgress(final int progress) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.setProgress(progress);
                }
            });
        }
    }
}
