/**
 * AirCasting - Share your Air!
 * Copyright (C) 2011-2012 HabitatMap, Inc.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * You can contact the authors by email at <info@habitatmap.org>
 */
package pl.llp.aircasting.screens.sessionRecord;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.gms.maps.model.LatLng;
import com.google.inject.Inject;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.screens.common.sessionState.ViewingSessionsManager;
import pl.llp.aircasting.screens.common.base.DialogActivity;
import pl.llp.aircasting.screens.sessions.LocationPickerActivity;
import roboguice.inject.InjectView;

import static pl.llp.aircasting.screens.sessions.LocationPickerActivity.LOCATION;

public class StartFixedSessionActivity extends DialogActivity implements View.OnClickListener {
    @InjectView(R.id.start_indoor_session) Button startIndoorSessionButton;
    @InjectView(R.id.start_outdoor_session) Button startOutdoorSessionButton;

    @InjectView(R.id.session_title) EditText sessionTitle;
    @InjectView(R.id.session_tags) EditText sessionTags;

    @Inject Application context;
    @Inject ViewingSessionsManager viewingSessionsManager;

    int PLACE_PICKER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.start_fixed_session);
        initDialogToolbar("Session Details");

        startIndoorSessionButton.setOnClickListener(this);
        startOutdoorSessionButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.start_indoor_session: {
                startFixedSession(true, null);
                break;
            }
            case R.id.start_outdoor_session: {
                runLocationPicker();
                break;
            }
        }
    }

    private void runLocationPicker() {
        startActivityForResult(new Intent(this, LocationPickerActivity.class), PLACE_PICKER_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                LatLng latLng = data.getParcelableExtra(LOCATION);

                startFixedSession(false, latLng);
            }
        }
    }

    private void startFixedSession(boolean isIndoor, LatLng latlng) {
        String title = sessionTitle.getText().toString();
        String tags = sessionTags.getText().toString();

        Intents.startDashboardActivity(this);
        viewingSessionsManager.startFixedSession(title, tags, isIndoor, latlng);
        airbeam2Configurator.sendFinalAb2Config();

        finish();
    }
}
