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
package pl.llp.aircasting.screens.settings;

import pl.llp.aircasting.R;
import pl.llp.aircasting.screens.common.base.DialogActivity;
import pl.llp.aircasting.screens.common.helpers.SettingsHelper;

import android.app.Application;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.inject.Inject;
import roboguice.inject.InjectView;

public class BackendSettingsActivity extends DialogActivity implements View.OnClickListener
{
  @InjectView(R.id.ok) Button ok;

  @InjectView(R.id.backend_address_field) EditText addressField;
  @InjectView(R.id.backend_port_field) EditText portField;

  @Inject SettingsHelper settingsHelper;

  @Inject Application context;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.backend_settings);
    initDialogToolbar("Backend Settings");

    ok.setOnClickListener(this);
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    String backendURL = settingsHelper.getBackendURL();
    int backendPort = settingsHelper.getBackendPort();
    addressField.setText(backendURL);
    portField.setText(Integer.toString(backendPort));
  }

  @Override
  public void onClick(View view)
  {
    switch (view.getId())
    {
      case R.id.ok:
        settingsHelper.setBackendAddress(getBackendAddress());
        settingsHelper.setBackendPort(getBackendPort());
        finish();
    }
  }

  private String getBackendAddress()
  {
    return addressField.getText().toString();
  }

  public String getBackendPort()
  {
    return portField.getText().toString();
  }
}
