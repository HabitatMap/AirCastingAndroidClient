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
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.google.inject.Inject;
import pl.llp.aircasting.R;
import roboguice.inject.InjectView;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 11/29/11
 * Time: 12:32 PM
 */
public class ProfileActivity extends DialogActivity implements View.OnClickListener {
    @InjectView(R.id.sign_in) Button signIn;
    @InjectView(R.id.create_profile) Button createProfile;
    @InjectView(R.id.forgot_password) Button forgotPassword;

    @Inject Application context;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.profile);
        initDialogToolbar("Profile Options");

        signIn.setOnClickListener(this);
        createProfile.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.sign_in:
                startActivity(new Intent(context, SignInActivity.class));
                break;
            case R.id.create_profile:
                startActivity(new Intent(context, SignUpActivity.class));
                break;
            case R.id.forgot_password:
                startActivity(new Intent(context, ResetPasswordActivity.class));
                break;
        }

        finish();
    }
}
