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
package pl.llp.aircasting.activity;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.inject.Inject;
import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.helper.ToastHelper;
import pl.llp.aircasting.model.ViewingSessionsManager;
import roboguice.inject.InjectView;

public class StartFixedSessionActivity extends DialogActivity implements View.OnClickListener {
    @InjectView(R.id.start_indoor_session) Button startIndoorSessionButton;
    @InjectView(R.id.start_outdoor_session) Button startOutdoorSessionButton;
    @InjectView(R.id.cancel) Button cancelButton;

    @InjectView(R.id.session_title) EditText sessionTitle;
    @InjectView(R.id.session_tags) EditText sessionTags;
    @InjectView(R.id.session_description) EditText sessionDescription;

    @Inject Application context;
    @Inject ViewingSessionsManager viewingSessionsManager;

    int PLACE_PICKER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.start_fixed_session);

        startIndoorSessionButton.setOnClickListener(this);
        startOutdoorSessionButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
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
            case R.id.cancel: {
                finish();
                break;
            }
        }
    }

    private void runLocationPicker() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            GooglePlayServicesUtil.getErrorDialog(e.getConnectionStatusCode(), this, 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            ToastHelper.show(context, R.string.google_play_services_not_available, Toast.LENGTH_LONG);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                LatLng latlng = place.getLatLng();



                startFixedSession(false, latlng);
            }
        }
    }

    private void startFixedSession(boolean isIndoor, LatLng latlng) {
        String title = sessionTitle.getText().toString();
        String tags = sessionTags.getText().toString();
        String description = sessionDescription.getText().toString();

        Intents.startDashboardActivity(this, true);
        viewingSessionsManager.startFixedSession(title, tags, description, isIndoor, latlng);
        airbeam2Configurator.sendFinalAb2Config();

        finish();
    }
}
