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
import android.widget.EditText;
import android.widget.Toast;
import com.google.inject.Inject;
import pl.llp.aircasting.R;
import pl.llp.aircasting.api.UsersDriver;
import pl.llp.aircasting.activity.task.SimpleProgressTask;
import pl.llp.aircasting.helper.ToastHelper;
import pl.llp.aircasting.util.http.HttpResult;
import roboguice.inject.InjectView;

import static pl.llp.aircasting.util.http.Status.SUCCESS;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/29/11
 * Time: 12:38 PM
 */
public class ResetPasswordActivity extends DialogActivity implements View.OnClickListener {
    @InjectView(R.id.ok) Button ok;

    @InjectView(R.id.login) EditText loginField;

    @Inject UsersDriver userDriver;

    @Inject Application context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.reset_password);

        ok.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ok:
                resetPassword();
                break;
        }
    }

    private void resetPassword() {
        //noinspection unchecked
        new SimpleProgressTask<Void, Void, HttpResult<Void>>(this) {
            @Override
            protected HttpResult<Void> doInBackground(Void... voids) {
                return userDriver.resetPassword(getLogin());
            }

            @Override
            protected void onPostExecute(HttpResult<Void> result) {
                super.onPostExecute(result);

                if (result.getStatus() == SUCCESS) {
                    ToastHelper.show(context, R.string.reset_email_sent, Toast.LENGTH_LONG);
                    finish();
                } else {
                    ToastHelper.show(context, R.string.unknown_error, Toast.LENGTH_LONG);
                }
            }
        }.execute();
    }

    public String getLogin() {
        return loginField.getText().toString();
    }
}
