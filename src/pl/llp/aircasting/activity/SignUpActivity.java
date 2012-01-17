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

import android.app.Application;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.google.inject.Inject;
import pl.llp.aircasting.R;
import pl.llp.aircasting.activity.task.SimpleProgressTask;
import pl.llp.aircasting.api.data.UserInfo;
import pl.llp.aircasting.api.UsersDriver;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.util.http.HttpResult;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

import static pl.llp.aircasting.util.http.Status.*;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/14/11
 * Time: 5:11 PM
 */
public class SignUpActivity extends DialogActivity implements View.OnClickListener {
    @InjectView(R.id.ok) Button ok;

    @InjectView(R.id.email) EditText emailField;
    @InjectView(R.id.username) EditText usernameField;
    @InjectView(R.id.password) EditText passwordField;
    @InjectView(R.id.send_emails) CheckBox sendEmails;

    @InjectResource(R.string.email) String email;
    @InjectResource(R.string.username) String username;
    @InjectResource(R.string.password) String password;

    @Inject UsersDriver userDriver;
    @Inject SettingsHelper settingsHelper;

    @Inject Application context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        ok.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ok:
                createProfile();
                break;
        }
    }

    private void createProfile() {
        //noinspection unchecked
        new SimpleProgressTask<Void, Void, HttpResult<UserInfo>>(this) {
            @Override
            protected HttpResult<UserInfo> doInBackground(Void... voids) {
                HttpResult<UserInfo> result = userDriver.create(getEmail(), getUsername(),
                        getPassword(), sendEmails.isChecked());

                if (result.getStatus() == SUCCESS) {
                    settingsHelper.setAuthToken(result.getContent().getAuthentication_token());
                    settingsHelper.setUserLogin(result.getContent().getUsername());

                    finish();
                }

                return result;
            }

            @Override
            protected void onPostExecute(HttpResult<UserInfo> result) {
                super.onPostExecute(result);

                if (result.getStatus() == ERROR) {
                    Toast.makeText(context, R.string.unknown_error, Toast.LENGTH_LONG).show();
                } else if (result.getStatus() == FAILURE) {
                    toastErrors(result);
                }
            }
        }.execute();
    }

    private void toastErrors(HttpResult<UserInfo> result) {
        if (result.getContent().getEmail() != null) {
            Toast.makeText(context, email + " " + result.getContent().getEmail(), Toast.LENGTH_LONG).show();
        }
        if (result.getContent().getUsername() != null) {
            Toast.makeText(context, username + " " + result.getContent().getUsername(), Toast.LENGTH_LONG).show();
        }
        if (result.getContent().getPassword() != null) {
            Toast.makeText(context, password + " " + result.getContent().getPassword(), Toast.LENGTH_LONG).show();
        }
    }

    private String getPassword() {
        return this.passwordField.getText().toString();
    }

    private String getEmail() {
        return this.emailField.getText().toString();
    }

    private String getUsername() {
        return this.usernameField.getText().toString();
    }
}
