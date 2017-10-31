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

import pl.llp.aircasting.Intents;
import pl.llp.aircasting.R;
import pl.llp.aircasting.helper.SettingsHelper;
import pl.llp.aircasting.model.CurrentSessionManager;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.inject.Inject;
import roboguice.inject.InjectView;

public class ContributeActivity extends DialogActivity
{
  @Inject CurrentSessionManager currentSessionManager;
  @Inject Context context;
  @Inject SettingsHelper settingsHelper;

  @InjectView(R.id.yes) Button yes;
  @InjectView(R.id.no) Button no;

  @Override
  protected void onResume()
  {
    super.onResume();
    if (!settingsHelper.hasCredentials())
    {
      Toast.makeText(context, R.string.account_reminder, Toast.LENGTH_LONG).show();
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.contribute);
    if(!getIntent().hasExtra(Intents.SESSION_ID))
    {
      throw new RuntimeException("Should have arrived here with a session id");
    }

    final long sessionId = getIntent().getLongExtra(Intents.SESSION_ID, 0);

    yes.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        currentSessionManager.setContribute(sessionId, true);
        saveSession(sessionId);
        finish();
      }
    });
    no.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        currentSessionManager.setContribute(sessionId, false);
        saveSession(sessionId);
        finish();
      }
    });
  }

  @Override
  public void onBackPressed()
  {
    currentSessionManager.continueSession();
    finish();
  }

  private void saveSession(long sessionId)
  {
    //noinspection unchecked
    currentSessionManager.finishSession(sessionId);
  }
}
