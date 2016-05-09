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
import com.google.inject.Inject;
import pl.llp.aircasting.R;
import pl.llp.aircasting.model.SessionManager;
import roboguice.inject.InjectView;

public class StartRealtimeSessionActivity extends DialogActivity implements View.OnClickListener
{
  @InjectView(R.id.start_session) Button startSessionButton;
  @InjectView(R.id.cancel) Button cancelButton;

  @InjectView(R.id.session_title) EditText sessionTitle;
  @InjectView(R.id.session_tags) EditText sessionTags;
  @InjectView(R.id.session_description) EditText sessionDescription;

  @Inject Application context;

  @Inject SessionManager sessionManager;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.start_realtime_session);

    startSessionButton.setOnClickListener(this);
    cancelButton.setOnClickListener(this);
  }

  @Override
  public void onClick(View view)
  {
    switch (view.getId()) {
      case R.id.start_session: {
        startRealtimeSession();
        break;
      }
    }

    finish();
  }

  private void startRealtimeSession() {
    String title = sessionTitle.getText().toString();
    String tags = sessionTags.getText().toString();
    String description = sessionDescription.getText().toString();

    sessionManager.startRealtimeSession(title, tags, description);
  }
}
