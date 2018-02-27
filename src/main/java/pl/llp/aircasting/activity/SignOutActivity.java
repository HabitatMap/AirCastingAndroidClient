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

import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.task.SimpleProgressTask;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.storage.repository.SessionRepository;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.inject.Inject;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/23/11
 * Time: 5:31 PM
 */
public class SignOutActivity extends DialogActivity implements View.OnClickListener {
    @Inject SettingsHelper settingsHelper;
    @Inject SessionRepository sessionRepository;

    @InjectView(R.id.sign_out) Button signOut;
    @InjectView(R.id.summary) TextView summary;

    @InjectResource(R.string.profile_template) String profileTemplate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.sign_out);
        initDialogToolbar("Sign Out");

        signOut.setOnClickListener(this);

        String email = settingsHelper.getUserLogin();
        String text = String.format(profileTemplate, email);
        summary.setText(text);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_out:
                signOut();
                break;
        }
    }

    private void signOut() {
        //noinspection unchecked
        new SimpleProgressTask<Void, Void, Void>(this) {
            @Override
            protected Void doInBackground(Void... voids) {
                settingsHelper.removeCredentials();
                sessionRepository.deleteUploaded();
                sessionRepository.deleteLocationless();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                finish();
            }
        }.execute();
    }
}
