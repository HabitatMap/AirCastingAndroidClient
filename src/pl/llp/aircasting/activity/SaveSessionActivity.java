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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.inject.Inject;
import pl.llp.aircasting.R;
import pl.llp.aircasting.helper.MetadataHelper;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.SessionManager;
import roboguice.inject.InjectView;

/**
 * Created by IntelliJ IDEA.
 * User: obrok
 * Date: 10/6/11
 * Time: 12:49 PM
 */
public class SaveSessionActivity extends DialogActivity implements View.OnClickListener {
    @InjectView(R.id.save_button) Button saveButton;
    @InjectView(R.id.discard_button) Button discardButton;

    @InjectView(R.id.session_title) EditText sessionTitle;
    @InjectView(R.id.session_tags) EditText sessionTags;
    @InjectView(R.id.session_description) EditText sessionDescription;

    @Inject SessionManager sessionManager;
    @Inject SettingsHelper settingsHelper;
    @Inject MetadataHelper metadataHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager.pauseSession();

        setContentView(R.layout.session_details);

        saveButton.setOnClickListener(this);
        discardButton.setOnClickListener(this);

        populateTags();
    }

    @Override
    public void onBackPressed() {
        sessionManager.continueSession();
        finish();
    }

    private void populateTags() {
        sessionTags.setText(
                metadataHelper.getDataType()
                        + " " + metadataHelper.getInstrument()
                        + " " + metadataHelper.getOSVersion()
                        + " " + metadataHelper.getPhoneModel()
        );
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.save_button:
                fillSessionDetails();
                startActivity(new Intent(this, ContributeActivity.class));
                break;
            case R.id.discard_button:
                sessionManager.discardSession();
                break;
        }
        finish();
    }

    private void fillSessionDetails() {
        sessionManager.setTitle(sessionTitle.getText().toString());
        sessionManager.setTags(sessionTags.getText().toString());
        sessionManager.setDescription(sessionDescription.getText().toString());
    }
}
